package ua.besf0r.kovadlo.minecraft.fabric

import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import ua.besf0r.kovadlo.*
import ua.besf0r.kovadlo.instance.Instance
import ua.besf0r.kovadlo.minecraft.ModificationManager
import ua.besf0r.kovadlo.network.DownloadListener
import ua.besf0r.kovadlo.network.DownloadManager
import ua.besf0r.kovadlo.network.file.FileManager.createDirectoryIfNotExists
import ua.besf0r.kovadlo.network.file.MavenUtil.createUrl

class FabricInstaller : ModificationManager {
    private val json = Json { ignoreUnknownKeys = true }

    private val LOADERS_URL = "https://meta.fabricmc.net/v2/versions/loader"

    @Throws(Exception::class)
    override fun download(
        progress: DownloadListener,
        version: String,
        instance: Instance
    ): Unit = runBlocking {
        progress.onStageChanged("Завантажуємо Fabric($version)")

        try {
            val compiledLoaderUrl = "$LOADERS_URL/${instance.minecraftVersion}/$version/profile/json"

            val fabricProfile = fetchFabricProfile(compiledLoaderUrl)
            val libraries = prepareLibraries(fabricProfile, instance)

            DownloadManager.executeMultiple(libraries){ value: Long, size: Long ->
                progress.onProgress(value, size)
            }
            instanceManager.getMinecraftDir(instance).resolve("mods").createDirectoryIfNotExists()

            instance.mainClass = fabricProfile.mainClass
        }catch (e: Exception){
            Logger.publish(e.stackTraceToString())
        }
    }

    private suspend fun fetchFabricProfile(compiledLoaderUrl: String) =
        json.decodeFromString<FabricInstallProfile>(
            httpClient.get(compiledLoaderUrl).bodyAsText()
        )

    private fun prepareLibraries(
        fabricInstallProfile: FabricInstallProfile,
        instance: Instance
    ): MutableList<DownloadManager.DownloadFile> {
        val libraries = mutableListOf<DownloadManager.DownloadFile>()
        fabricInstallProfile.libraries.forEach {
            val url = it.url + createUrl(it.name)
            val path = librariesDir.resolve(createUrl(it.name))

            libraries.add(
                DownloadManager.DownloadFile(
                    url = url,
                    sha1 = it.sha1,
                    saveAs = path,
                    declaredSize = (it.size ?: 0).toLong()
                )
            )
            instance.customLibraries.add(path)
        }
        return libraries
    }
}