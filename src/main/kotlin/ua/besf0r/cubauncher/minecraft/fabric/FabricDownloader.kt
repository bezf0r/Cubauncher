package ua.besf0r.cubauncher.minecraft.fabric

import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import ua.besf0r.cubauncher.httpClient
import ua.besf0r.cubauncher.instance.Instance
import ua.besf0r.cubauncher.instanceManager
import ua.besf0r.cubauncher.librariesDir
import ua.besf0r.cubauncher.minecraft.OperatingSystem
import ua.besf0r.cubauncher.network.DownloadListener
import ua.besf0r.cubauncher.network.DownloadManager
import ua.besf0r.cubauncher.network.DownloadManager.Companion.downloadDataList
import ua.besf0r.cubauncher.network.file.FilesManager.createDirectoryIfNotExists
import ua.besf0r.cubauncher.workDir
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import kotlin.io.path.absolutePathString
import kotlin.io.path.deleteIfExists

class FabricDownloader {
    private val json = Json { ignoreUnknownKeys = true }

    private val INSTALLERS_URL = "https://meta.fabricmc.net/v2/versions/installer"
    private val LOADERS_URL = "https://meta.fabricmc.net/v2/versions/loader"

    @Serializable
    data class Installer(val url: String, val version: String, val stable: Boolean)

    @Throws(IOException::class)
    fun download(
        progress: DownloadListener,
        fabricVersion: String,
        instance: Instance
    )= runBlocking{
        progress.onStageChanged("Завантажуємо Fabric($fabricVersion)")

        val installer = downloadDataList<Installer>(INSTALLERS_URL).first { it.stable }

        val installerPath = workDir.resolve("forge-installer-${fabricVersion}.jar")

        runBlocking {
            DownloadManager(installer.url, saveAs = installerPath).execute { value, size ->
                progress.onProgress(value, size)
            }
        }
        val process = ProcessBuilder(
            listOf(
                OperatingSystem.javaType, "-jar",
                installerPath.absolutePathString(), "client",
                "-dir", workDir.absolutePathString(),
                "-mcversion",instance.minecraftVersion,
                "-loader", fabricVersion
            )
        ).directory(workDir.toFile()).start()

        BufferedReader(InputStreamReader(process.inputStream, StandardCharsets.UTF_8)).use { reader ->
            var line: String?
            while ((reader.readLine().also { line = it }) != null) {
                println(line)
            }
        }
        process.waitFor()
        val compiledLoaderUrl = "$LOADERS_URL/${instance.minecraftVersion}/$fabricVersion"

        val fabricProfile = json.decodeFromString<FabricProfile>(
            httpClient.get(compiledLoaderUrl).bodyAsText()
        )
        fabricProfile.launcherMeta.libraries.common.forEach {
            instance.fabricLibraries.add(
                librariesDir.resolve(createUrl(it.name))
            )
        }
        instance.fabricLibraries.add(librariesDir.resolve(
            createUrl("net.fabricmc:intermediary:${instance.minecraftVersion}")
        ))
        instance.fabricLibraries.add(librariesDir.resolve(
            createUrl("net.fabricmc:fabric-loader:${fabricVersion}")
        ))

        val mods = instanceManager.getMinecraftDir(instance).resolve("mods")
        mods.createDirectoryIfNotExists()

        installerPath.deleteIfExists()

        instance.mainClass = fabricProfile.launcherMeta.mainClass.client
    }
    private fun createUrl(name: String): String {
        val parts = name.split(":")
        if (parts.size != 3) {
            throw IllegalArgumentException("Name must be in the format 'group:name:version'")
        }

        val (group, artifact, version) = parts
        val groupPath = group.replace('.', '/')

        return "$groupPath/$artifact/$version/$artifact-$version.jar"
    }
}