package ua.besf0r.cubauncher.minecraft.quilt

import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import ua.besf0r.cubauncher.*
import ua.besf0r.cubauncher.instance.Instance
import ua.besf0r.cubauncher.network.DownloadListener
import ua.besf0r.cubauncher.network.DownloadManager
import ua.besf0r.cubauncher.network.file.FilesManager.createDirectoryIfNotExists
import ua.besf0r.cubauncher.network.file.MavenUtil.createUrl

class QuiltInstaller {
    private val json = Json { ignoreUnknownKeys = true }

    private val LOADERS_URL = "https://meta.quiltmc.org/v3/versions/loader"

    @Throws(Exception::class)
    fun download(
        progress: DownloadListener,
        quiltVersion: String,
        instance: Instance
    ) = runBlocking {
        progress.onStageChanged("Завантажуємо Quilt($quiltVersion)")
        try {
            val compiledLoaderUrl = "$LOADERS_URL/${instance.minecraftVersion}/$quiltVersion/profile/json"

            val fabricProfile = json.decodeFromString<QuiltProfile>(
                httpClient.get(compiledLoaderUrl).bodyAsText()
            )

            val libraries = mutableListOf<DownloadManager.DownloadFile>()
            fabricProfile.libraries.forEach {
                val url = it.url + createUrl(it.name)
                val path = librariesDir.resolve(createUrl(it.name))

                libraries.add(DownloadManager.DownloadFile(url = url, saveAs = path))
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