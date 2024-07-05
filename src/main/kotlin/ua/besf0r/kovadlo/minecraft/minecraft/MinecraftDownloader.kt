package ua.besf0r.kovadlo.minecraft.minecraft

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.*
import ua.besf0r.kovadlo.*
import ua.besf0r.kovadlo.instance.Instance
import ua.besf0r.kovadlo.minecraft.OperatingSystem
import ua.besf0r.kovadlo.minecraft.OperatingSystem.Companion.applyOnThisPlatform
import ua.besf0r.kovadlo.minecraft.java.JavaRuntime
import ua.besf0r.kovadlo.minecraft.java.JavaRuntimeManifest
import ua.besf0r.kovadlo.minecraft.minecraft.*
import ua.besf0r.kovadlo.network.DownloadListener
import ua.besf0r.kovadlo.network.DownloadManager
import ua.besf0r.kovadlo.network.file.FileManager.createDirectoryIfNotExists
import ua.besf0r.kovadlo.network.file.FileManager.createFileIfNotExists
import ua.besf0r.kovadlo.network.file.IOUtil
import java.io.IOException
import java.util.zip.ZipFile
import kotlin.io.path.*


class MinecraftDownloader(
    private var currentJob: CoroutineScope,
    private val minecraftDownloadListener: DownloadListener
) {
    private val resources = "https://resources.download.minecraft.net/"
    private val allRuntimeUrl = "https://launchermeta.mojang.com/v1/products/java-runtime/2ec0cc96c44e5a76b9c8b7c39df7210883d12871/all.json"

    private var currentInstance: Instance? = null

    @Throws(IOException::class)
    suspend fun downloadMinecraft(instance: Instance): Long {
        val startTime = System.currentTimeMillis()
        currentJob.async {
            currentInstance = instance

            val manifestRead = IOUtil.readUtf8String(versionsDir.resolve("version_manifest_v2.json"))
            val manifest = json.decodeFromString<VersionManifest.VersionManifest>(manifestRead)

            val versions = manifest.versions
            val version = versions.find { it.id == instance.minecraftVersion }!!

            saveClientJson(version)

            val versionInfoFile = versionsDir.resolve(instance.minecraftVersion)
                .resolve("${instance.minecraftVersion}.json")
            val versionInfo = json.decodeFromString<MinecraftVersion>(IOUtil.readUtf8String(versionInfoFile))

            downloadJava(versionInfo)
            downloadClient(version)
            downloadLibraries(version, versionInfo)
            downloadAssetIndexes(versionInfo)
            downloadAssetObjects(versionInfo)
        }.await()
        return startTime
    }

    @Throws(IOException::class)
    private fun saveClientJson(version: VersionManifest.Version) {
        if (!currentJob.isActive) return
        val jsonFile = versionsDir.resolve(version.id).resolve(version.id + ".json")

        DownloadManager(version.url, version.sha1, saveAs = jsonFile).downloadFile { _, _ -> }
    }

    private val json = Json { ignoreUnknownKeys = true }

    @Throws(IOException::class)
    private fun downloadClient(version: VersionManifest.Version) {
        if (!currentJob.isActive) return
        val jsonFile = versionsDir.resolve(version.id).resolve(version.id + ".json")
        val versionInfo: MinecraftVersion = json.decodeFromString(IOUtil.readUtf8String(jsonFile))

        val client = versionInfo.downloads?.client ?: return
        val jarFile = versionsDir.resolve(version.id).resolve(version.id + ".jar")
        minecraftDownloadListener.onProgress(0, 0)
        minecraftDownloadListener.onStageChanged("Завантаження клієнта...")

        DownloadManager(
            client.url,
            client.sha1,
            client.size,
            jarFile
        ).downloadFile { value: Long, size: Long ->
            minecraftDownloadListener.onProgress(value, size)
        }
    }

    @Throws(IOException::class)
    private fun downloadLibraries(
        version: VersionManifest.Version,
        versionInfo: MinecraftVersion
    ) {
        if (!currentJob.isActive) return

        minecraftDownloadListener.onProgress(0, 0)
        minecraftDownloadListener.onStageChanged("Завантаження бібліотек...")

        val filesForDownload = mutableListOf<DownloadManager.DownloadFile>()
        val natives = mutableListOf<Map.Entry<String, Artifact>>()

        versionInfo.libraries.forEach Library@{ library ->
            if (!currentJob.isActive) return@Library

            if (!library.rules.applyOnThisPlatform()) return@Library

            val downloads = library.downloads ?: return@Library
            val artifact = downloads.artifact

            if (artifact != null) {
                val jarFile = librariesDir.resolve(artifact.path ?: return@Library)
                if (artifact.url == null) return@Library

                filesForDownload.add(
                    DownloadManager.DownloadFile(
                        artifact.url!!, artifact.sha1, artifact.size!!.toLong(), jarFile
                    )
                )
            }
            val classifiers = downloads.classifiers
            if (classifiers != null) {
                val classifier = downloads.classifiers.entries.find {
                    it.key.contains(OperatingSystem.osName)
                }?: return@Library

                natives.add(classifier)

                val jarFile = librariesDir.resolve(classifier.value.path ?: return@Library)
                if (classifier.value.url == null) return@Library

                filesForDownload.add(
                    DownloadManager.DownloadFile(
                        classifier.value.url!!,
                        classifier.value.sha1,
                        classifier.value.size!!.toLong(),
                        jarFile
                    )
                )
            }
        }
        DownloadManager.executeMultiple(filesForDownload, currentJob) {value: Long, size: Long ->
            minecraftDownloadListener.onProgress(value, size)
        }

        if (!currentJob.isActive) return
        natives.forEach {
            val nativesDir = versionsDir.resolve(version.id).resolve("natives")
            val jarFile = librariesDir.resolve(it.value.path!!)

            nativesDir.createDirectoryIfNotExists()

            ZipFile(jarFile.toFile()).use {zip ->
                zip.entries().toList().forEach Entry@ { entry ->
                    if (!entry.name.endsWith(".dll", ignoreCase = true)) return@Entry

                    val filePath = nativesDir.resolve(entry.name)
                    if (filePath.exists()) return@Entry
                    filePath.createFileIfNotExists()

                    zip.getInputStream(entry).use { input ->
                        IOUtil.extractFile(input,filePath)
                    }
                }
                zip.close()
            }
        }
    }

    @Throws(IOException::class)
    private fun downloadAssetIndexes(
        versionInfo: MinecraftVersion
    ) {
        if (!currentJob.isActive) return
        minecraftDownloadListener.onProgress(0, 0)
        minecraftDownloadListener.onStageChanged("Завантаження індексу...")

        val indexesFolder = assetsDir.resolve("indexes")
        indexesFolder.createDirectoryIfNotExists()

        val assetIndex = versionInfo.assetIndex ?: return

        val assetId: String = assetIndex.id
        val indexFile = indexesFolder.resolve("$assetId.json")

        DownloadManager(
            assetIndex.url,
            assetIndex.sha1,
            assetIndex.size,
            indexFile
        ).downloadFile { value, size ->
            minecraftDownloadListener.onProgress(value, size)
        }
    }

    private fun downloadAssetObjects(
        versionInfo: MinecraftVersion
    ) {
        if (!currentJob.isActive) return

        minecraftDownloadListener.onProgress(0, 0)
        minecraftDownloadListener.onStageChanged("Завантаження активів...")

        val assetId: String = versionInfo.assetIndex?.id ?: return

        val index = json.decodeFromString<AssetsIndexes>(
            IOUtil.readUtf8String(
                assetsDir.resolve("indexes").resolve("$assetId.json")
            )
        )

        val assetsDownloadFolder = assetsDir.resolve("objects")
        assetsDownloadFolder.createDirectoryIfNotExists()

        val objects = index.objects

        val filesForDownload = mutableListOf<DownloadManager.DownloadFile>()

        objects.forEach { obj ->
            if (!currentJob.isActive) return
            val asset = obj.value

            if (asset.hash == null) return@forEach
            val prefix = asset.hash!!.substring(0, 2)
            val saveAs = if (index.virtual)
                assetsDir.resolve("virtual").resolve(versionInfo.id!!).resolve(obj.key)
            else
                assetsDir.resolve("objects").resolve(prefix).resolve(asset.hash!!)
            val url = resources + prefix + "/" + asset.hash

            filesForDownload.add(
                DownloadManager.DownloadFile(url, asset.hash, asset.size, saveAs)
            )
        }
        DownloadManager.executeMultiple(
            filesForDownload,currentJob,
            10
        ) { value: Long, size: Long ->
            minecraftDownloadListener.onProgress(value, size)
        }
    }

    private fun downloadJava(version: MinecraftVersion){
        if (!currentJob.isActive) return

        minecraftDownloadListener.onProgress(0, 0)
        minecraftDownloadListener.onStageChanged("Завантаження Java...")

        val runtimesFile = javaDir.resolve("runtimes.json")
        val jreOsName = OperatingSystem.jreOsName
        val javaKey = OperatingSystem.getJavaKey(version)

        if (javaDir.resolve("java-runtime-gamma")
            .resolve("bin")
            .resolve("java.exe").exists() && javaKey == "java-runtime-gamma" ) return

        DownloadManager(
            fileUrl = allRuntimeUrl,
            saveAs = runtimesFile
        ).downloadFile { _, _ ->  }

        val jsonObject = json.parseToJsonElement(
            IOUtil.readUtf8String(javaDir.resolve("runtimes.json"))
        ).jsonObject

        val osRuntime = jsonObject[jreOsName]?.jsonObject?.get(javaKey)
            ?: throw IllegalArgumentException("Expected runtime information not found for $jreOsName and $javaKey")

        val runtime = json.decodeFromString<List<JavaRuntime>>(osRuntime.jsonArray.toString())[0]
        val manifestFile = javaDir.resolve(javaKey).resolve("manifest.json")

        DownloadManager(
            fileUrl = runtime.manifest!!.url!!,
            saveAs = manifestFile
        ).downloadFile{ _, _ ->}

        val manifest = json.decodeFromString<JavaRuntimeManifest>(
            IOUtil.readUtf8String(manifestFile)
        )

        val files = mutableListOf<DownloadManager.DownloadFile>()
        manifest.files.forEach {
            val jreFile = it.value
            val saveAs = javaDir.resolve(javaKey).resolve(it.key)

            if (jreFile.type == "directory"){
                saveAs.createDirectoryIfNotExists()
            }else{
                val raw = jreFile.downloads!!["raw"]!!
                files.add(
                    DownloadManager.DownloadFile(raw.url!!, raw.sha1, raw.size, saveAs)
                )
            }
        }

        DownloadManager.executeMultiple(files,currentJob){ value: Long, size: Long ->
            minecraftDownloadListener.onProgress(value, size)
        }
    }

    fun cancel(instanceName: String) {
        currentJob.cancel()
        instanceManager.deleteInstance(instanceName)
    }
}
