package ua.besf0r.kovadlo.minecraft.liteloader

import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import ua.besf0r.kovadlo.*
import ua.besf0r.kovadlo.instance.Instance
import ua.besf0r.kovadlo.minecraft.ModificationManager
import ua.besf0r.kovadlo.network.DownloadListener
import ua.besf0r.kovadlo.network.DownloadManager
import ua.besf0r.kovadlo.network.file.FileManager.createDirectoryIfNotExists
import ua.besf0r.kovadlo.network.file.IOUtil
import ua.besf0r.kovadlo.network.file.MavenUtil

class LiteLoaderInstaller : ModificationManager {
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

            val profileFile = versionsDir.resolve("liteloader-$version.json")
            DownloadManager(
                fileUrl = compiledLoaderUrl,
                saveAs = profileFile
            ).downloadFile()

            val liteLoaderProfile = json.decodeFromString<LiteLoaderProfile>(IOUtil.readUtf8String(profileFile))

            val libraries = mutableListOf<DownloadManager.DownloadFile>()
            liteLoaderProfile.libraries?.forEach {
                val url = (it.url ?: "https://libraries.minecraft.net/") + MavenUtil.createUrl(it.name)
                val path = librariesDir.resolve(MavenUtil.createUrl(it.name))

                libraries.add(DownloadManager.DownloadFile(url = url, saveAs = path))
                instance.customLibraries.add(path)
            }
            DownloadManager.executeMultiple(libraries){ value: Long, size: Long ->
                progress.onProgress(value, size)
            }
            instanceManager.getMinecraftDir(instance).resolve("mods")
                .createDirectoryIfNotExists()

            liteLoaderProfile.mainClass?.let { instance.mainClass = it }
            instance.liteLoaderProfile = liteLoaderProfile
        }catch (e: Exception){
            Logger.publish(e.stackTraceToString())
        }
    }
}