package ua.besf0r.cubauncher.minecraft.forge

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.utils.io.errors.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.*
import ua.besf0r.cubauncher.instance.Instance
import ua.besf0r.cubauncher.instanceManager
import ua.besf0r.cubauncher.librariesDir
import ua.besf0r.cubauncher.network.DownloadListener
import ua.besf0r.cubauncher.network.DownloadManager
import ua.besf0r.cubauncher.minecraft.OperatingSystem
import ua.besf0r.cubauncher.network.file.FilesManager.createDirectoryIfNotExists
import ua.besf0r.cubauncher.network.file.FilesManager.createFileIfNotExists
import ua.besf0r.cubauncher.workDir
import java.io.*
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.util.zip.ZipFile
import kotlin.io.path.deleteIfExists


class ForgeDownloader {
    private val json = Json { ignoreUnknownKeys = true }

    private val installer = "https://maven.minecraftforge.net/net/minecraftforge/forge/"

    @Throws(IOException::class)
    fun download(
        progress: DownloadListener,
        forgeVersion: String,
        instance: Instance
    ) {
        progress.onStageChanged("Завантажуємо Forge($forgeVersion)")

        val installerUrl = installer + "${forgeVersion}/forge-${forgeVersion}-installer.jar"

        val installerPath = workDir.resolve("forge-installer-${forgeVersion}.jar")
        val installerLog = workDir.resolve("forge-installer-${forgeVersion}.jar.log")

        val profiles = workDir.resolve("launcher_profiles.json")
        profiles.createFileIfNotExists()

        runBlocking {
            DownloadManager(installerUrl, saveAs = installerPath).execute { value, size ->
                progress.onProgress(value, size)
            }
        }

        var forgeProfile: ForgeProfile? = null

        val libraries: MutableList<Path> = mutableListOf()

        val zipFile = ZipFile(installerPath.toFile())
        zipFile.use { zip ->
            val entry = zip.getEntry("version.json")
            if (entry != null) {
                val stream = zip.getInputStream(entry).readBytes().decodeToString()
                val installProfile = json.decodeFromString<ForgeProfile>(stream)
                forgeProfile = installProfile
            }
        }

        zipFile.close()

        forgeProfile?.libraries?.forEach {
            val artifact = it.downloads.artifact
            val path = librariesDir.resolve(artifact.path)
            libraries.add(path)
        }

        val process = ProcessBuilder(
            listOf(
                OperatingSystem.javaType,
                "-jar",
                installerPath.toFile().name,
                "--installClient"
            )
        ).directory(workDir.toFile()).start()

        BufferedReader(InputStreamReader(process.inputStream, StandardCharsets.UTF_8)).use { reader ->
            var line: String?
            while ((reader.readLine().also { line = it }) != null) {
                println(line)
            }
        }
        process.waitFor()

        val mods = instanceManager.getMinecraftDir(instance).resolve("mods")
        mods.createDirectoryIfNotExists()

        profiles.deleteIfExists()
        installerLog.deleteIfExists()
        installerPath.deleteIfExists()

        instance.forge = forgeProfile
        instance.forgeLibraries = libraries

        instance.mainClass = forgeProfile?.mainClass.toString()
    }
}