package ua.besf0r.cubauncher.minecraft.fabric

import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import ua.besf0r.cubauncher.*
import ua.besf0r.cubauncher.instance.Instance
import ua.besf0r.cubauncher.minecraft.OperatingSystem
import ua.besf0r.cubauncher.network.DownloadListener
import ua.besf0r.cubauncher.network.DownloadManager
import ua.besf0r.cubauncher.network.DownloadManager.Companion.downloadDataList
import ua.besf0r.cubauncher.network.file.FilesManager.createDirectoryIfNotExists
import ua.besf0r.cubauncher.network.file.MavenUtil.createUrl
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.deleteIfExists

class FabricInstaller {
    private val json = Json { ignoreUnknownKeys = true }

    private val LOADERS_URL = "https://meta.fabricmc.net/v2/versions/loader"

    @Throws(Exception::class)
    fun download(
        progress: DownloadListener,
        fabricVersion: String,
        instance: Instance
    ) = runBlocking {
        progress.onStageChanged("Завантажуємо Fabric($fabricVersion)")

        try {
            val compiledLoaderUrl = "$LOADERS_URL/${instance.minecraftVersion}/$fabricVersion/profile/json"

            val fabricProfile = json.decodeFromString<FabricProfile>(
                httpClient.get(compiledLoaderUrl).bodyAsText()
            )
            val libraries = mutableListOf<DownloadManager.DownloadFile>()
            fabricProfile.libraries.forEach {
                val url = it.url + createUrl(it.name)
                val path = librariesDir.resolve(createUrl(it.name))

                libraries.add(DownloadManager.DownloadFile(
                    url = url,
                    sha1 = it.sha1,
                    saveAs = path,
                    declaredSize = (it.size ?: 0).toLong()
                ))
                instance.quiltLibraries.add(path)
            }
            DownloadManager.executeMultiple(libraries){ value: Long, size: Long ->
                progress.onProgress(value, size)
            }
            instanceManager.getMinecraftDir(instance).resolve("mods")
                .createDirectoryIfNotExists()

            instance.mainClass = fabricProfile.mainClass
        }catch (e: Exception){
            Logger.publish(e.stackTraceToString())
        }
    }
}