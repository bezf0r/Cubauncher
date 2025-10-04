package ua.besf0r.kovadlo.minecraft.liteloader

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import ua.besf0r.kovadlo.*
import ua.besf0r.kovadlo.instance.Instance
import ua.besf0r.kovadlo.minecraft.ModificationManager
import ua.besf0r.kovadlo.network.DownloadListener
import ua.besf0r.kovadlo.network.DownloadManager
import ua.besf0r.kovadlo.network.DownloadService
import ua.besf0r.kovadlo.network.file.FileManager.createDirectoryIfNotExists
import ua.besf0r.kovadlo.network.file.IOUtil
import ua.besf0r.kovadlo.network.file.MavenUtil

class LiteLoaderInstaller(
    private val di: DI
) : ModificationManager {
    private val json = Json { ignoreUnknownKeys = true }

    private val version = "https://raw.githubusercontent.com/MultiMC/meta-multimc/master/com.mumfrey.liteloader/"
    @Throws(Exception::class)
    override fun download(
        progress: DownloadListener,
        version: String,
        instance: Instance
    ): Unit = runBlocking {
        progress.onStageChanged("Завантажуємо LiteLoader")

        try {
            val compiledLoaderUrl = "${this@LiteLoaderInstaller.version}/$version.json"

            val profileFile = di.workingDirs().versionsDir.resolve("liteloader-$version.json")
            DownloadManager(
                di.direct.instance(),
                di.direct.instance(),
                fileUrl = compiledLoaderUrl,
                saveAs = profileFile
            ).downloadFile()

            val liteLoaderProfile = json.decodeFromString<LiteLoaderProfile>(IOUtil.readUtf8String(profileFile))

            val libraries = mutableListOf<DownloadService.DownloadFile>()
            liteLoaderProfile.libraries?.forEach {
                val url = (it.url ?: "https://libraries.minecraft.net/") + MavenUtil.createUrl(it.name)
                val path = di.workingDirs().librariesDir.resolve(MavenUtil.createUrl(it.name))

                libraries.add(DownloadService.DownloadFile(url = url, saveAs = path))
                instance.customLibraries.add(path)
            }
            di.direct.instance<DownloadService>().executeMultiple(libraries){ value: Long, size: Long ->
                progress.onProgress(value, size)
            }
            di.instanceManager().getMinecraftDir(instance).resolve("mods")
                .createDirectoryIfNotExists()

            liteLoaderProfile.mainClass?.let { instance.mainClass = it }
            instance.liteLoaderProfile = liteLoaderProfile
        }catch (e: Exception){
            di.logger().publish("launcher",e.stackTraceToString())
        }
    }
}