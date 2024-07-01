package ua.besf0r.cubauncher.minecraft.liteloader

import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import ua.besf0r.cubauncher.*
import ua.besf0r.cubauncher.instance.Instance
import ua.besf0r.cubauncher.network.DownloadListener
import ua.besf0r.cubauncher.network.DownloadManager
import ua.besf0r.cubauncher.network.file.FilesManager.createDirectoryIfNotExists
import ua.besf0r.cubauncher.network.file.IOUtil
import ua.besf0r.cubauncher.network.file.MavenUtil

class LiteLoaderInstaller {
    private val json = Json { ignoreUnknownKeys = true }

    private val version = "https://raw.githubusercontent.com/MultiMC/meta-multimc/master/com.mumfrey.liteloader/"
    @Throws(Exception::class)
    fun download(
        progress: DownloadListener,
        liteLoaderVersion: String,
        instance: Instance
    ) = runBlocking {
        progress.onStageChanged("Завантажуємо LiteLoader")

        try {
            val compiledLoaderUrl = "$version/$liteLoaderVersion.json"

            val profileFile = versionsDir.resolve("liteloader-$liteLoaderVersion.json")
            DownloadManager(
                fileUrl = compiledLoaderUrl,
                saveAs = profileFile
            ).execute()

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