package ua.besf0r.kovadlo.instance

import androidx.compose.runtime.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import org.apache.commons.text.StringSubstitutor
import ua.besf0r.kovadlo.*
import ua.besf0r.kovadlo.account.Account
import ua.besf0r.kovadlo.account.MicrosoftAccount
import ua.besf0r.kovadlo.account.getByName
import ua.besf0r.kovadlo.account.microsoft.MicrosoftOAuthUtils
import ua.besf0r.kovadlo.minecraft.*
import ua.besf0r.kovadlo.minecraft.OperatingSystem.Companion.applyOnThisPlatform
import ua.besf0r.kovadlo.minecraft.minecraft.MinecraftVersion
import ua.besf0r.kovadlo.network.file.IOUtil
import ua.besf0r.kovadlo.network.file.MavenUtil
import java.io.*
import java.net.ConnectException
import java.nio.charset.StandardCharsets
import java.util.concurrent.Executors
import javax.swing.JFrame
import javax.swing.JOptionPane
import kotlin.io.path.notExists
import kotlin.io.path.pathString


class InstanceRunner(private val instance: Instance){
    fun run() {
        val dispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
        CoroutineScope(dispatcher).launch {
            val versionInfoFile = versionsDir.resolve(instance.minecraftVersion)
                .resolve("${instance.minecraftVersion}.json")
            val versionInfo = json.decodeFromString<MinecraftVersion>(IOUtil.readUtf8String(versionInfoFile))

            val arguments = getArguments(versionInfo)

            val exitCode = runGameProcess(arguments)
            Logger.publish("Minecraft сесію закінченно з кодом $exitCode")

            instanceManager.update(instance)
        }
    }

    private val json = Json { ignoreUnknownKeys = true }

    private fun generateClassPath(versionInfo: MinecraftVersion): List<String>{
        val classpath: MutableList<String> = mutableListOf()

        val version = versionInfo.id ?: throw NullPointerException("Сould not get version id").apply {
            Logger.publish(this.stackTraceToString())
        }

        val originalClientPath = versionsDir.resolve(version)
            .resolve("${version}.jar").toAbsolutePath()

        instance.forgeNewInstallProfile?.libraries?.forEach {
            val artifact = it.downloads.artifact
            val path = librariesDir.resolve(artifact.path)
            classpath.add(path.pathString)
        }
        instance.forgeOldIntallProfile?.libraries?.forEach {
            if (it.name == null) return@forEach
            classpath.add(librariesDir.resolve(MavenUtil.createUrl(it.name)).pathString)
        }

        instance.customLibraries.forEach { classpath.add(it.pathString) }

        versionInfo.libraries.mapNotNull {
            it.downloads?.artifact?.path?.let { path ->
                librariesDir.resolve(path).pathString
            }
        }.let(classpath::addAll)

        classpath.add(originalClientPath.toString())

        return classpath.distinct()
    }

    @Throws(IOException::class)
    private fun getArguments(versionInfo: MinecraftVersion): MutableList<String> {
        val instanceDir = instanceManager.getMinecraftDir(instance)
        val arguments = mutableListOf<String>()
        val natives = versionsDir.resolve(instance.minecraftVersion).resolve("natives")
        val classpath = generateClassPath(versionInfo)
        val account = validateAccount()

        val isNewFormat = versionInfo.minecraftArguments == null

        val args = mapOf(
            "launcher_name" to "Kovadlo($launcherVersion)",
            "launcher_version" to "1.0-beta",
            "natives_directory" to if (natives.notExists()) librariesDir else natives,
            "classpath" to classpath.joinToString(File.pathSeparator),
            "client" to "-",
            "auth_xuid" to "-",
            "auth_player_name" to account.username,
            "version_name" to versionInfo.id.toString(),
            "game_directory" to instanceDir.toAbsolutePath().pathString,
            "assets_root" to assetsDir.toAbsolutePath().pathString,
            "assets_index_name" to versionInfo.assets,
            "auth_uuid" to account.uuid,
            "auth_access_token" to account.accessToken,
            "user_type" to "msa",
            "version_type" to "Kovadlo",
            "user_properties" to "{}",
            "resolution_width" to "960",
            "resolution_height" to "540",

            // Forge additions
            "library_directory" to librariesDir.pathString,
            "classpath_separator" to File.pathSeparator
        )

        val substitutor = StringSubstitutor(args)

        arguments.add(OperatingSystem.getJavaPath(versionInfo))

        instance.forgeNewInstallProfile?.jvmArguments?.forEach {
            arguments.add(substitutor.replace(it).replace("\\","/"))
        }
        
        arguments.add("-Xms" + settingsManager.settings.minimumRam + "m")
        arguments.add("-Xmx" + settingsManager.settings.maximumRam + "m")

        arguments.add(substitutor.replace("-Djava.library.path=\${natives_directory}"))
        arguments.add("-DlibraryDirectory=${librariesDir.pathString}")

        jvmArguments(isNewFormat, versionInfo, arguments, substitutor)
        arguments.add(instance.mainClass)

        if (instance.forgeNewInstallProfile?.minecraftArguments == null &&
            instance.forgeOldIntallProfile?.minecraftArguments == null) {
            gameArguments(versionInfo, arguments, substitutor)
        }
        addModificatorsGameArguments(arguments, substitutor)

        listOf(
            "--demo",
            "--quickPlayPath", "\${quickPlayPath}",
            "--quickPlaySingleplayer", "\${quickPlaySingleplayer}",
            "--quickPlayMultiplayer", "\${quickPlayMultiplayer}",
            "--quickPlayRealms", "\${quickPlayRealms}"
        ).forEach { arguments.remove(it) }

        return arguments
    }

    private fun addModificatorsGameArguments(
        arguments: MutableList<String>,
        substitutor: StringSubstitutor
    ) {
        instance.forgeNewInstallProfile?.gameArguments?.forEach {
            arguments.add(substitutor.replace(it).replace("\\", "/"))
        }
        instance.forgeOldIntallProfile?.minecraftArguments?.let {
            arguments.add(substitutor.replace(it))
        }
        instance.liteLoaderProfile?.tweakers?.forEach {
            arguments.add("--tweakClass $it")
        }
    }

    private fun jvmArguments(
        newFormat: Boolean,
        versionInfo: MinecraftVersion,
        arguments: MutableList<String>,
        substitutor: StringSubstitutor
    ) {
        if (newFormat) {
            versionInfo.arguments?.jvm?.forEach {
                if (it.rules.applyOnThisPlatform())
                    it.value.forEach { value ->
                        arguments.add("\"${substitutor.replace(value)}\"")
                    }
            }
        } else {
            arguments.add(substitutor.replace("-Djava.library.path=\${natives_directory}"))

            arguments.add(substitutor.replace("-cp"))
            arguments.add(substitutor.replace("\${classpath}"))
        }
    }

    private fun gameArguments(
        versionInfo: MinecraftVersion,
        arguments: MutableList<String>,
        substitutor: StringSubstitutor
    ) {
        versionInfo.arguments?.game?.flatMap { it.value }?.forEach {
            arguments.add(substitutor.replace(it))
        }

        versionInfo.minecraftArguments?.split("\\s")?.forEach {
            arguments.add(substitutor.replace(it))
        }
    }

    private fun validateAccount(): Account = runBlocking{
        val settings = settingsManager.settings

        val account = accountsManager.accounts
            .getByName(settings.selectedAccount ?: "")
        if (account == null) throw NullPointerException("Selected account is null, please select account").apply {
            Logger.publish(this.stackTraceToString())
        }
        try {
            if (account is MicrosoftAccount) {
                val refreshed = MicrosoftOAuthUtils.refreshToken(account.refreshToken) ?: return@runBlocking account

                MicrosoftOAuthUtils.loginToMicrosoftAccount(refreshed) { refreshAccount ->
                    accountsManager.deleteAccount(account.username)

                    accountsManager.createAccount(refreshAccount)
                    Logger.publish("Успішний вхід для користувача: ${refreshAccount.username}")
                }
            }
        }catch (e: ConnectException){
            JOptionPane.showMessageDialog(
                JFrame(),
                "Не вдалося авторизуватися до Microsoft через проблеми з підключенням," +
                        "ваш ліцензійний аккаунт буде переведено до оффлайн.",
                "Kovadlo ERROR",
                JOptionPane.ERROR_MESSAGE
            )
        }
        return@runBlocking account
    }

    private fun runGameProcess(command: List<String>): Int {
        val process = ProcessBuilder(command)
            .directory(instanceManager.getMinecraftDir(instance).toFile()).start()
        Logger.publish(command.joinToString(" "))

        BufferedReader(
            InputStreamReader(process.inputStream, StandardCharsets.UTF_8)
        ).use { reader ->
            var line: String?
            while ((reader.readLine().also { line = it }) != null) {
                Logger.publish("[${instance.name}] $line")
            }
        }
        return process.waitFor()
    }
}
