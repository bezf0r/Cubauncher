package ua.besf0r.cubauncher.instance

import androidx.compose.runtime.*
import kotlinx.coroutines.*
import org.apache.commons.text.StringSubstitutor
import ua.besf0r.cubauncher.*
import ua.besf0r.cubauncher.account.Account
import ua.besf0r.cubauncher.account.MicrosoftAccount
import ua.besf0r.cubauncher.account.getByName
import ua.besf0r.cubauncher.account.microsoft.MicrosoftOAuthUtils
import ua.besf0r.cubauncher.minecraft.*
import ua.besf0r.cubauncher.minecraft.OperatingSystem.Companion.applyOnThisPlatform
import ua.besf0r.cubauncher.network.file.MavenUtil
import java.io.*
import java.nio.charset.StandardCharsets
import java.util.concurrent.Executors
import kotlin.io.path.notExists
import kotlin.io.path.pathString


class InstanceRunner(private val instance: Instance){
    fun run() {
        val dispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
        CoroutineScope(dispatcher).launch {
            val instanceManager = instanceManager
            val arguments = getArguments()

            val exitCode = runGameProcess(arguments)
            Logger.publish("Minecraft сесію закінченно з кодом $exitCode")

            instanceManager.update(instance)
        }
    }

    private fun generateClassPath(): List<String>{
        val classpath: MutableList<String> = mutableListOf()

        val version = instance.versionInfo!!.id ?: throw NullPointerException("Сould not get version id").apply {
            Logger.publish(this.stackTraceToString())
        }

        val originalClientPath = versionsDir.resolve(version)
            .resolve("${version}.jar").toAbsolutePath()

        instance.newForgeProfile?.libraries?.forEach {
            val artifact = it.downloads.artifact
            val path = librariesDir.resolve(artifact.path)
            classpath.add(path.pathString)
        }
        instance.oldForgeProfile?.libraries?.forEach {
            if (it.name == null) return@forEach
            classpath.add(librariesDir.resolve(MavenUtil.createUrl(it.name)).pathString)
        }

        instance.fabricLibraries.forEach { classpath.add(it.pathString) }
        instance.quiltLibraries.forEach { classpath.add(it.pathString)  }

        instance.versionInfo!!.libraries.mapNotNull {
            it.downloads?.artifact?.path?.let { path ->
                librariesDir.resolve(path).pathString
            }
        }.let(classpath::addAll)

        classpath.add(originalClientPath.toString())

        return classpath.distinct()
    }

    @Throws(IOException::class)
    private fun getArguments(): MutableList<String> {
        val instanceDir = instanceManager.getMinecraftDir(instance)
        val arguments = mutableListOf<String>()
        val natives = versionsDir.resolve(instance.minecraftVersion).resolve("natives")
        val classpath = generateClassPath()
        val account = validateAccount()

        val newFormat = instance.versionInfo?.minecraftArguments == null

        val args = mapOf(
            "launcher_name" to "Cubauncher(1.0-beta)",
            "launcher_version" to "1.0-beta",
            "natives_directory" to if (natives.notExists()) librariesDir else natives,
            "classpath" to classpath.joinToString(File.pathSeparator),
            "client" to "-",
            "auth_xuid" to "-",
            "auth_player_name" to account.username,
            "version_name" to instance.versionInfo!!.id.toString(),
            "game_directory" to instanceDir.toAbsolutePath().pathString,
            "assets_root" to assetsDir.toAbsolutePath().pathString,
            "assets_index_name" to instance.versionInfo!!.assets,
            "auth_uuid" to account.uuid,
            "auth_access_token" to account.accessToken,
            "user_type" to "msa",
            "version_type" to "Cubauncher",
            "user_properties" to "{}",
            "resolution_width" to "960",
            "resolution_height" to "540",

            // Forge additions
            "library_directory" to librariesDir.pathString,
            "classpath_separator" to File.pathSeparator
        )

        val substitutor = StringSubstitutor(args)

        arguments.add(OperatingSystem.getJavaPath(instance.versionInfo!!))

        instance.newForgeProfile?.jvmArguments?.forEach {
            arguments.add(substitutor.replace(it).replace("\\","/"))
        }
        
        arguments.add("-Xms" + settingsManager.settings.minimumRam + "m")
        arguments.add("-Xmx" + settingsManager.settings.maximumRam + "m")

        arguments.add(substitutor.replace("-Djava.library.path=\${natives_directory}"))
        arguments.add("-DlibraryDirectory=${librariesDir.pathString}")

        jvmArguments(newFormat, arguments, substitutor)
        arguments.add(instance.mainClass)

        if (instance.newForgeProfile?.minecraftArguments == null &&
            instance.oldForgeProfile?.minecraftArguments == null) {
            gameArguments(arguments, substitutor)
        }

        instance.newForgeProfile?.gameArguments?.forEach {
            arguments.add(substitutor.replace(it).replace("\\","/"))
        }
        instance.oldForgeProfile?.minecraftArguments?.let {
            arguments.add(substitutor.replace(it))
        }

        listOf(
            "--demo",
            "--quickPlayPath", "\${quickPlayPath}",
            "--quickPlaySingleplayer", "\${quickPlaySingleplayer}",
            "--quickPlayMultiplayer", "\${quickPlayMultiplayer}",
            "--quickPlayRealms", "\${quickPlayRealms}"
        ).forEach { arguments.remove(it) }

        return arguments
    }

    private fun jvmArguments(
        newFormat: Boolean,
        arguments: MutableList<String>,
        substitutor: StringSubstitutor
    ) {
        if (newFormat) {
            instance.versionInfo!!.arguments?.jvm?.forEach {
                if (it.rules.applyOnThisPlatform())
                    it.value.forEach { value ->
                        arguments.add(substitutor.replace(value))
                    }
            }
        } else {
            arguments.add(substitutor.replace("-Djava.library.path=\${natives_directory}"))

            arguments.add(substitutor.replace("-cp"))
            arguments.add(substitutor.replace("\${classpath}"))
        }
    }

    private fun gameArguments(
        arguments: MutableList<String>,
        substitutor: StringSubstitutor
    ) {
        instance.versionInfo!!.arguments?.game?.flatMap { it.value }?.forEach {
            arguments.add(substitutor.replace(it))
        }

        instance.versionInfo?.minecraftArguments?.split("\\s")?.forEach {
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


        if (account is MicrosoftAccount){
            val refreshed = MicrosoftOAuthUtils.refreshToken(account.refreshToken) ?: return@runBlocking account

            MicrosoftOAuthUtils.loginToMicrosoftAccount(refreshed) { refreshAccount ->
                accountsManager.deleteAccount(account.username)

                accountsManager.createAccount(refreshAccount)
                Logger.publish("Успішний вхід для користувача: ${refreshAccount.username}")
            }
        }
        return@runBlocking account
    }

    private fun runGameProcess(command: List<String>): Int {
        val process = Runtime.getRuntime().exec(
            command.joinToString(" "),
            null,
            instanceManager.getMinecraftDir(instance).toFile()
        )
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
