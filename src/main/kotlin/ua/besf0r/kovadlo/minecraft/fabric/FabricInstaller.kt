package ua.besf0r.kovadlo.minecraft.fabric

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import ua.besf0r.kovadlo.*
import ua.besf0r.kovadlo.instance.Instance
import ua.besf0r.kovadlo.instance.InstanceManager
import ua.besf0r.kovadlo.minecraft.ModificationManager
import ua.besf0r.kovadlo.network.DownloadListener
import ua.besf0r.kovadlo.network.DownloadService
import ua.besf0r.kovadlo.network.file.FileManager.createDirectoryIfNotExists
import ua.besf0r.kovadlo.network.file.MavenUtil.createUrl
import ua.besf0r.kovadlo.settings.directories.WorkingDirs

class FabricInstaller(
    private val di: DI
) : ModificationManager {
    private val json = Json { ignoreUnknownKeys = true }

    private val LOADERS_URL = "https://meta.fabricmc.net/v2/versions/loader"

    private val instanceManager: InstanceManager = di.direct.instance()
    private val httpClient: HttpClient = di.direct.instance()
    private val workingDirs: WorkingDirs = di.direct.instance()

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

            di.direct.instance<DownloadService>().executeMultiple(libraries){ value: Long, size: Long ->
                progress.onProgress(value, size)
            }
            instanceManager.getMinecraftDir(instance).resolve("mods").createDirectoryIfNotExists()

            instance.mainClass = fabricProfile.mainClass
        }catch (e: Exception){
            di.logger().publish("launcher",e.stackTraceToString())
        }
    }

    private suspend fun fetchFabricProfile(compiledLoaderUrl: String) =
        json.decodeFromString<FabricInstallProfile>(
            httpClient.get(compiledLoaderUrl).bodyAsText()
        )

    private fun prepareLibraries(
        fabricInstallProfile: FabricInstallProfile,
        instance: Instance
    ): MutableList<DownloadService.DownloadFile> {
        val libraries = mutableListOf<DownloadService.DownloadFile>()
        fabricInstallProfile.libraries.forEach {
            val url = it.url + createUrl(it.name)
            val path = workingDirs.librariesDir.resolve(createUrl(it.name))

            libraries.add(
                DownloadService.DownloadFile(
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