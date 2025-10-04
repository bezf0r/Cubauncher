package ua.besf0r.kovadlo.minecraft.quilt

import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import ua.besf0r.kovadlo.*
import ua.besf0r.kovadlo.instance.Instance
import ua.besf0r.kovadlo.minecraft.ModificationManager
import ua.besf0r.kovadlo.network.DownloadListener
import ua.besf0r.kovadlo.network.DownloadService
import ua.besf0r.kovadlo.network.file.FileManager.createDirectoryIfNotExists
import ua.besf0r.kovadlo.network.file.MavenUtil.createUrl

class QuiltInstaller(
    private val di: DI
) : ModificationManager {
    private val json = Json { ignoreUnknownKeys = true }

    private val LOADERS_URL = "https://meta.quiltmc.org/v3/versions/loader"

    @Throws(Exception::class)
    override fun download(
        progress: DownloadListener,
        version: String,
        instance: Instance
    ): Unit = runBlocking {
        progress.onStageChanged("Завантажуємо Quilt($version)")
        try {
            val compiledLoaderUrl = "$LOADERS_URL/${instance.minecraftVersion}/$version/profile/json"

            val fabricProfile = json.decodeFromString<QuiltProfile>(
                di.httpClient().get(compiledLoaderUrl).bodyAsText()
            )

            val libraries = mutableListOf<DownloadService.DownloadFile>()
            fabricProfile.libraries.forEach {
                val url = it.url + createUrl(it.name)
                val path = di.workingDirs().librariesDir.resolve(createUrl(it.name))

                libraries.add(DownloadService.DownloadFile(url = url, saveAs = path))
                instance.customLibraries.add(path)
            }
            di.direct.instance<DownloadService>().executeMultiple(libraries){ value: Long, size: Long ->
                progress.onProgress(value, size)
            }
            di.instanceManager().getMinecraftDir(instance).resolve("mods")
                .createDirectoryIfNotExists()

            instance.mainClass = fabricProfile.mainClass
        }catch (e: Exception){
            di.logger().publish("launcher",e.stackTraceToString())
        }
    }
}