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
import java.io.*
import java.nio.charset.StandardCharsets
import java.util.concurrent.Executors
import kotlin.io.path.pathString


class InstanceRunner(private val instance: Instance){

    fun run() {
        val dispatcher = Executors.newFixedThreadPool(1).asCoroutineDispatcher()
        CoroutineScope(dispatcher).launch{
            val instanceManager = instanceManager
            val arguments = getArguments()

            val exitCode = runGameProcess(arguments)
            println("Minecraft process finished with exit code $exitCode")

            instanceManager.update(instance)
        }
    }

    private fun generateClassPath(): List<String>{
        val classpath: MutableList<String> = mutableListOf()

        val version = instance.versionInfo!!.id ?: throw Exception("Failed to get version id")

        val originalClientPath = versionsDir.resolve(version)
            .resolve("${version}.jar").toAbsolutePath()

        instance.forgeLibraries.forEach {
            classpath.add(it.pathString)
        }
        instance.fabricLibraries.forEach {
            classpath.add(it.pathString)
        }

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

        arguments.add(OperatingSystem.getJavaPath(instance.versionInfo!!))

        val classpath = generateClassPath()

        val account = validateAccount()

        val args = mapOf(
            "launcher_name" to "Cubauncher(1.0-beta)",
            "launcher_version" to "1.0-beta",
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
            "resolution_height" to "540"
        )

        val substitutor = StringSubstitutor(args)

        arguments.add("-Xms" + settingsManager.settings.minimumRam + "m")
        arguments.add("-Xmx" + settingsManager.settings.maximumRam + "m")

        arguments.add("-XX:HeapDumpPath=MojangTricksIntelDriversForPerformance_javaw.exe_minecraft.exe.heapdump")

        arguments.add("-Djava.library.path=$natives")
        arguments.add("-DlibraryDirectory=${librariesDir.pathString}")

        arguments.add(substitutor.replace("-cp"))
        arguments.add(substitutor.replace("\${classpath}"))
        arguments.add(instance.mainClass)


        instance.versionInfo!!.arguments?.game?.flatMap { it.value }?.forEach {
            arguments.add(substitutor.replace(it))
        }

        instance.versionInfo?.minecraftArguments?.split("\\s")?.forEach {
            arguments.add(substitutor.replace(it))
        }

        instance.forge?.generateArguments()?.let(arguments::addAll)

        listOf(
            "--demo",
            "--quickPlayPath", "\${quickPlayPath}",
            "--quickPlaySingleplayer", "\${quickPlaySingleplayer}",
            "--quickPlayMultiplayer", "\${quickPlayMultiplayer}",
            "--quickPlayRealms", "\${quickPlayRealms}"
        ).forEach { arguments.remove(it) }

        return arguments
    }

    private fun validateAccount(): Account = runBlocking{
        val settings = settingsManager.settings

        val account = accountsManager.accounts
            .getByName(settings.selectedAccount ?: "")

        if (account == null) throw NullPointerException("Selected account is null, please select account")


        if (account is MicrosoftAccount){
            val refreshed = MicrosoftOAuthUtils.refreshToken(account.refreshToken) ?: return@runBlocking account

            MicrosoftOAuthUtils.loginToMicrosoftAccount(refreshed) { refreshAccount ->
                accountsManager.deleteAccount(account.username)

                accountsManager.createAccount(refreshAccount)
                println("Успішний вхід для користувача: ${refreshAccount.username}")
            }
        }
        return@runBlocking account
    }

    private fun runGameProcess(command: List<String>): Int {
        val process = Runtime.getRuntime().exec(command.joinToString(" "))

        BufferedReader(InputStreamReader(process.inputStream, StandardCharsets.UTF_8)).use { reader ->
            var line: String?
            while ((reader.readLine().also { line = it }) != null) {
                println(line)
            }
        }

        return process.waitFor()
    }
}
