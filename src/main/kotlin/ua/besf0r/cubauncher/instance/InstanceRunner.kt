package ua.besf0r.cubauncher.instance

import androidx.compose.runtime.*
import kotlinx.coroutines.*
import org.apache.commons.text.StringSubstitutor
import ua.besf0r.cubauncher.*
import ua.besf0r.cubauncher.account.Account
import ua.besf0r.cubauncher.minecraft.*
import ua.besf0r.cubauncher.minecraft.os.OperatingSystem
import java.io.*
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import kotlin.io.path.pathString


class InstanceRunner(
    private val account: Account, private val instance: Instance
){

    @OptIn(DelicateCoroutinesApi::class)
    @Composable
    fun run() {
        GlobalScope.launch(Dispatchers.IO) {
            account.authenticate()
            val instanceManager = instanceManager

            val arguments = getArguments(librariesDir)

            val exitCode = runGameProcess(arguments)
            println("Minecraft process finished with exit code $exitCode")

            instanceManager.update(instance)
        }
    }

    private fun generateClassPath(): List<String>{
        val classpath: MutableList<String> = mutableListOf()

        val version = instance.versionInfo!!.id

        val originalClientPath = versionsDir.resolve(version).resolve(
            "${version}.jar"
        ).toAbsolutePath()

        classpath.add(originalClientPath.toString())

        instance.forgeLibraries.forEach {
            classpath.add(it.pathString)
        }

        instance.versionInfo!!.libraries.mapNotNull {
            it.downloads?.artifact?.path?.let { path ->
                librariesDir.resolve(path).pathString
            }
        }.let(classpath::addAll)

        return classpath.distinct()
    }

    @Throws(IOException::class)
    private fun getArguments(
        nativesDir: Path
    ): MutableList<String> {

        val instanceDir = instanceManager.getMinecraftDir(instance)

        val assetsDir: Path = assetsDir
        val arguments = mutableListOf<String>()

        arguments.add(OperatingSystem.javaType)

        val classpath = generateClassPath()

        val args = mapOf(
            "natives_directory" to nativesDir.pathString,
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
            "auth_uuid" to account.uuid.toString(),
            "auth_access_token" to account.accessToken,
            "user_type" to "msa",
            "version_type" to "Cubauncher",
            "user_properties" to "{}",
            "resolution_width" to "960",
            "resolution_height" to "540"
        )

        val substitutor = StringSubstitutor(args)

        arguments.add("-Xms" + settingsManager.settings!!.minimumRam + "m")
        arguments.add("-Xmx" + settingsManager.settings!!.maximumRam + "m")

        arguments.add(substitutor.replace("-Djava.library.path=\${natives_directory}"))
        arguments.add("-DlibraryDirectory=${nativesDir.pathString}")

        arguments.add(substitutor.replace("-cp"))
        arguments.add(substitutor.replace("\${classpath}"))

        if (instance.forge != null) {
            arguments.add(instance.forge!!.mainClass!!)
        }else {
            arguments.add(instance.versionInfo!!.mainClass!!)
        }

        instance.versionInfo!!.arguments?.game?.flatMap { it.value }?.forEach {
            arguments.add(substitutor.replace(it))
        }

        instance.forge?.generateArguments()?.let(arguments::addAll)

        listOf(
            "--demo",
            "--quickPlayPath",
            "\${quickPlayPath}",
            "--quickPlaySingleplayer",
            "\${quickPlaySingleplayer}",
            "--quickPlayMultiplayer",
            "\${quickPlayMultiplayer}",
            "--quickPlayRealms",
            "\${quickPlayRealms}"
        ).forEach { arguments.remove(it) }

        return arguments
    }

    private fun runGameProcess(command: List<String>): Int {
        val process = ProcessBuilder(command).start()

        BufferedReader(InputStreamReader(process.inputStream, StandardCharsets.UTF_8)).use { reader ->
            var line: String?
            while ((reader.readLine().also { line = it }) != null) {
                println(line)
            }
        }

        return process.waitFor()
    }
}
