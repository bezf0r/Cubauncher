package ua.besf0r.cubauncher.minecraft

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import ua.besf0r.cubauncher.instance.Instance
import ua.besf0r.cubauncher.instanceManager
import ua.besf0r.cubauncher.minecraft.version.*
import ua.besf0r.cubauncher.network.DownloadListener
import ua.besf0r.cubauncher.network.DownloadManager
import ua.besf0r.cubauncher.util.FileUtil
import ua.besf0r.cubauncher.util.IOUtils
import ua.besf0r.cubauncher.util.OsEnum
import java.io.IOException
import java.nio.file.Path

class MinecraftDownloader(
    private var currentJob: CoroutineScope,
    private val versionsDir: Path,
    private val assetsDir: Path,
    private val librariesDir: Path,
    private val minecraftDownloadListener: DownloadListener
) {
    private val resources = "https://resources.download.minecraft.net/"


    private var currentInstance: Instance? = null
    @Throws(IOException::class)
    suspend fun downloadMinecraft(instance: Instance) {
        currentJob.async {
            currentInstance = instance

            FileUtil.createDirectoryIfNotExists(versionsDir.resolve(instance.minecraftVersion))

            val manifestRead = IOUtils.readUtf8String(
                versionsDir.resolve("version_manifest_v2.json")
            )
            val manifest: VersionManifest.VersionManifest = json.decodeFromString(manifestRead)

            val versions = manifest.versions

            val version = versions.find { it.id == instance.minecraftVersion }!!

            val startTime = System.currentTimeMillis()

            saveClientJson(version)
            downloadClient(version)
            downloadLibraries()
            downloadAssetIndexes()
            downloadAssetObjects()

            val endTime = System.currentTimeMillis()
            val duration = ((endTime - startTime).toInt())

            println("ЧАС - $duration")
        }.await()
    }

    @Throws(IOException::class)
    private fun saveClientJson(version: VersionManifest.Version) {
        if (!currentJob.isActive) return
        val jsonFile = versionsDir.resolve(version.id).resolve(version.id + ".json")
        DownloadManager(version.url,version.sha1, 0,jsonFile).execute { _, _ -> }

        currentInstance!!.versionInfo = json.decodeFromString<MinecraftVersion>(
            IOUtils.readUtf8String(jsonFile))
    }

    private val json = Json { ignoreUnknownKeys = true }

    @Throws(IOException::class)
    private fun downloadClient(version: VersionManifest.Version) {
        if (!currentJob.isActive) return
        val jsonFile = versionsDir.resolve(version.id).resolve(version.id + ".json")
        val versionInfo: MinecraftVersion = json.decodeFromString(IOUtils.readUtf8String(jsonFile))

        val client = versionInfo.downloads?.client ?: return
        val jarFile = versionsDir.resolve(version.id).resolve(version.id + ".jar")
        minecraftDownloadListener.onProgress(0, 0)
        minecraftDownloadListener.onStageChanged("Завантаження клієнта...")

        DownloadManager(client.url, client.sha1,client.size, jarFile)
            .execute{ value:Long, size:Long ->
                minecraftDownloadListener.onProgress(value,size)
        }
    }

    @Throws(IOException::class)
    private fun downloadLibraries(): List<Library> {
        if (!currentJob.isActive) return listOf()
        val nativeLibraries = mutableListOf<Library>()

        minecraftDownloadListener.onProgress(0, 0)
        minecraftDownloadListener.onStageChanged("Завантаження бібліотек...")

        val versionInfo = currentInstance!!.versionInfo?: return emptyList()

        versionInfo.libraries.forEach Library@ { library ->
            if (!currentJob.isActive) return@Library
            if (library.rules != null) {
                library.rules.forEach {
                    if (it.os?.name != OsEnum.osName) return@Library
//                    if (it.os.arch != EnumOS.arch) return@Library
                }
            }
            val downloads = library.downloads ?: return@Library
            val artifact = downloads.artifact ?: return@Library

            val jarFile = librariesDir.resolve(artifact.path?: return@Library)

            DownloadManager(artifact.url,artifact.sha1,artifact.size!!.toLong(),jarFile)
                .execute { value, size ->
                    minecraftDownloadListener.onProgress(value,size)
            }
        }

        return nativeLibraries
    }
    @Throws(IOException::class)
    private fun downloadAssetIndexes() {
        if (!currentJob.isActive) return
        minecraftDownloadListener.onProgress(0, 0)
        minecraftDownloadListener.onStageChanged("Завантаження індексу...")

        val indexesFolder = assetsDir.resolve("indexes")
        FileUtil.createDirectoryIfNotExists(indexesFolder)

        val assetIndex = currentInstance!!.versionInfo?.assetIndex ?: return

        val assetId: String = assetIndex.id
        val indexFile = indexesFolder.resolve("$assetId.json")

        DownloadManager(assetIndex.url,assetIndex.sha1,assetIndex.size,indexFile)
            .execute { value, size ->
                minecraftDownloadListener.onProgress(value,size)
        }
    }
    private fun downloadAssetObjects(){
        if (!currentJob.isActive) return

        minecraftDownloadListener.onProgress(0, 0)
        minecraftDownloadListener.onStageChanged("Завантаження активів...")

        val assetId: String = currentInstance!!.versionInfo?.assetIndex?.id ?: return

        val index = json.decodeFromString<AssetsIndexes>(
            IOUtils.readUtf8String(
                assetsDir.resolve("indexes").resolve("$assetId.json")
            )
        )

        val assetsDownloadFolder = assetsDir.resolve("objects")
        FileUtil.createDirectoryIfNotExists(assetsDownloadFolder)

        val objects = index.objects

        var objectsCount = 0

        objects.forEach { obj ->
            if (!currentJob.isActive) return

            val asset = obj.value

            val prefix = asset.hash.substring(0, 2)

            val saveAs = if (index.virtual) {
                assetsDir.resolve("virtual").resolve(currentInstance!!.versionInfo!!.id!!).resolve(obj.key)
            } else {
                assetsDir.resolve("objects").resolve(prefix).resolve(asset.hash)
            }

            val url = resources + prefix + "/" + asset.hash

            DownloadManager(url,asset.hash,asset.size,saveAs).execute { _,_ ->
                minecraftDownloadListener.onProgress(objectsCount.toLong(), objects.count().toLong())
            }
            objectsCount++
        }
    }
    fun cancel(instanceName: String) {
        currentJob.cancel()
        instanceManager.deleteInstance(instanceName)
    }
}
