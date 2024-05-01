package ua.besf0r.cubauncher.minecraft

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.serialization.json.Json
import ua.besf0r.cubauncher.instance.Instance
import ua.besf0r.cubauncher.minecraft.version.*
import ua.besf0r.cubauncher.network.FileDownloader
import ua.besf0r.cubauncher.util.FileUtil
import ua.besf0r.cubauncher.util.IOUtils
import ua.besf0r.cubauncher.util.OsEnum
import java.io.IOException
import java.nio.file.Path

class MinecraftDownloader(
    private val versionsDir: Path,
    private val assetsDir: Path,
    private val librariesDir: Path,
    private val instance: Instance,
    private val minecraftDownloadListener: MinecraftDownloadListener
) {
    private val resources = "https://resources.download.minecraft.net/"

    @Synchronized
    @Throws(IOException::class)
    fun downloadMinecraft(versionId: String) {
        FileUtil.createDirectoryIfNotExists(versionsDir.resolve(versionId))

        val manifestRead = IOUtils.readUtf8String(
            versionsDir.resolve("version_manifest_v2.json"))
        val manifest: VersionManifest.VersionManifest = json.decodeFromString(manifestRead)

        val versions = manifest.versions

        val version = versions.find { it.id == versionId }!!

        saveClientJson(version)

        println("Downloading client")
        val versionInfo = downloadClient(version)
        println("Downloaded client")

        println("Downloading libraries")
        downloadLibraries()
        println("Downloaded libraries")

        println("Downloading assets indexes")
        downloadAssetIndexes()
        println("Downloaded assets indexes")

        println("Downloading assets objects")
        downloadAssetObjects()
        println("Downloaded assets objects")

        return versionInfo
    }

    @Throws(IOException::class)
    private fun saveClientJson(version: VersionManifest.Version) {
        val jsonFile = versionsDir.resolve(version.id).resolve(version.id + ".json")
        FileDownloader(version.url,version.sha1, 0,jsonFile).execute { _, _ -> }

        instance.versionInfo = json.decodeFromString<MinecraftVersion>(
            IOUtils.readUtf8String(jsonFile))
    }

    private val json = Json { ignoreUnknownKeys = true }

    @Throws(IOException::class)
    private fun downloadClient(version: VersionManifest.Version) {
        val jsonFile = versionsDir.resolve(version.id).resolve(version.id + ".json")
        val versionInfo: MinecraftVersion = json.decodeFromString(IOUtils.readUtf8String(jsonFile))

        val client = versionInfo.downloads?.client ?: return
        println("Downloading " + version.id + ".jar")
        val jarFile = versionsDir.resolve(version.id).resolve(version.id + ".jar")
        minecraftDownloadListener.onProgress(0, 0)
        minecraftDownloadListener.onStageChanged("Downloading client")

        FileDownloader(client.url, client.sha1,client.size, jarFile)
            .execute{ value:Long, size:Long ->
                minecraftDownloadListener.onProgress(value,size)
        }
    }

    @Throws(IOException::class)
    private fun downloadLibraries(): List<Library> {
        val nativeLibraries = mutableListOf<Library>()

        minecraftDownloadListener.onProgress(0, 0)
        minecraftDownloadListener.onStageChanged("Downloading libraries")

        val versionInfo = instance.versionInfo?: return emptyList()

        versionInfo.libraries.forEach Library@ { library ->
            if (library.rules != null) {
                library.rules.forEach {
                    if (it.os?.name != OsEnum.osName) return@Library
//                    if (it.os.arch != EnumOS.arch) return@Library
                }
            }

            val downloads = library.downloads
            val artifact = downloads?.artifact

            val jarFile = librariesDir.resolve(artifact?.path!!)

            FileDownloader(artifact.url!!,artifact.sha1,artifact.size!!.toLong(),jarFile)
                .execute { value, size ->
                    minecraftDownloadListener.onProgress(value,size)
            }
        }

        return nativeLibraries
    }
    @Throws(IOException::class)
    private fun downloadAssetIndexes() {
        minecraftDownloadListener.onProgress(0, 0)
        minecraftDownloadListener.onStageChanged("Downloading asset indexes")

        val indexesFolder = assetsDir.resolve("indexes")
        FileUtil.createDirectoryIfNotExists(indexesFolder)

        val assetIndex = instance.versionInfo?.assetIndex ?: return

        val assetId: String = assetIndex.id
        val indexFile = indexesFolder.resolve("$assetId.json")

        FileDownloader(assetIndex.url,assetIndex.sha1,assetIndex.size,indexFile)
            .execute { value, size ->
                minecraftDownloadListener.onProgress(value,size)
        }
    }
    private fun downloadAssetObjects(){
        minecraftDownloadListener.onProgress(0, 0)
        minecraftDownloadListener.onStageChanged("Downloading asset objects")

        val assetId: String = instance.versionInfo?.assetIndex?.id ?: return

        val index = json.decodeFromString<AssetsIndexes>(
            IOUtils.readUtf8String(
                assetsDir.resolve("indexes").resolve("$assetId.json")
            )
        )

        val assetsDownloadFolder = assetsDir.resolve("objects")
        FileUtil.createDirectoryIfNotExists(assetsDownloadFolder)

        val objects = index.objects
        objects.forEach { obj ->
            val asset = obj.value

            val prefix = asset.hash.substring(0, 2)

            val saveAs = if (index.virtual) {
                assetsDir.resolve("virtual").resolve(instance.versionInfo!!.id!!).resolve(obj.key)
            } else {
                assetsDir.resolve("objects").resolve(prefix).resolve(asset.hash)
            }

            val url = resources + prefix + "/" + asset.hash

            FileDownloader(url,asset.hash,asset.size,saveAs).execute { value, size ->
                minecraftDownloadListener.onProgress(value, size)
            }
        }
    }
}
