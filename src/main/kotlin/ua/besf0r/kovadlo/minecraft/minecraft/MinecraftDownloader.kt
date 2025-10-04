package ua.besf0r.kovadlo.minecraft.minecraft

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.*
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import ua.besf0r.kovadlo.*
import ua.besf0r.kovadlo.instance.Instance
import ua.besf0r.kovadlo.minecraft.OperatingSystem
import ua.besf0r.kovadlo.minecraft.OperatingSystem.Companion.applyOnThisPlatform
import ua.besf0r.kovadlo.minecraft.java.JavaRuntime
import ua.besf0r.kovadlo.minecraft.java.JavaRuntimeManifest
import ua.besf0r.kovadlo.minecraft.minecraft.*
import ua.besf0r.kovadlo.network.DownloadListener
import ua.besf0r.kovadlo.network.DownloadManager
import ua.besf0r.kovadlo.network.DownloadService
import ua.besf0r.kovadlo.network.file.FileManager.createDirectoryIfNotExists
import ua.besf0r.kovadlo.network.file.FileManager.createFileIfNotExists
import ua.besf0r.kovadlo.network.file.IOUtil
import java.io.IOException
import java.util.zip.ZipFile
import kotlin.io.path.*


class MinecraftDownloader(
    private val di: DI,
    private val minecraftDownloadListener: DownloadListener
) {
    private val resources = "https://resources.download.minecraft.net/"
    private val allRuntimeUrl = "https://launchermeta.mojang.com/v1/products/java-runtime/2ec0cc96c44e5a76b9c8b7c39df7210883d12871/all.json"

    private var currentInstance: Instance? = null
    private var currentJob: Job? = null

    private val json = Json { ignoreUnknownKeys = true }

    @Throws(IOException::class)
    suspend fun downloadMinecraft(instance: Instance): Long {
        val startTime = System.currentTimeMillis()

        currentJob = di.coroutine().launch {
            currentInstance = instance

            val versions = di.direct.instance<MinecraftVersionList>().versions
            val version = versions.find { it.id == instance.minecraftVersion }!!

            saveClientJson(version)

            val versionInfoFile = di.workingDirs().versionsDir.resolve(instance.minecraftVersion)
                .resolve("${instance.minecraftVersion}.json")
            val versionInfo = json.decodeFromString<MinecraftVersion>(IOUtil.readUtf8String(versionInfoFile))

            ensureActive()
            downloadJava(versionInfo)

            ensureActive()
            downloadClient(version)

            ensureActive()
            downloadLibraries(version, versionInfo)

            ensureActive()
            downloadAssetIndexes(versionInfo)

            ensureActive()
            downloadAssetObjects(versionInfo)
        }

        currentJob?.join()
        return startTime
    }

    @Throws(IOException::class)
    private suspend fun saveClientJson(
        version: VersionManifest.Version
    ) {
        val jsonFile = di.workingDirs().versionsDir.resolve(version.id).resolve(version.id + ".json")

        DownloadManager(
            di.direct.instance<HttpClient>(),
            di.direct.instance<Logger>(),
            version.url,
            jsonFile,
            version.sha1,
        ).downloadFile { _, _ -> }
    }

    @Throws(IOException::class)
    private suspend fun downloadClient(version: VersionManifest.Version) {
        val jsonFile = di.workingDirs().versionsDir.resolve(version.id).resolve(version.id + ".json")
        val versionInfo: MinecraftVersion = json.decodeFromString(IOUtil.readUtf8String(jsonFile))

        val client = versionInfo.downloads?.client ?: return
        val jarFile = di.workingDirs().versionsDir.resolve(version.id).resolve(version.id + ".jar")
        minecraftDownloadListener.onProgress(0, 0)
        minecraftDownloadListener.onStageChanged("Завантаження клієнта...")

        DownloadManager(
            di.direct.instance(),
            di.direct.instance(),
            client.url, jarFile, client.sha1,
            client.size
        ).downloadFile { value: Long, size: Long ->
            minecraftDownloadListener.onProgress(value, size)
        }
    }

    @Throws(IOException::class)
    private suspend fun downloadLibraries(
        version: VersionManifest.Version,
        versionInfo: MinecraftVersion
    ) {

        minecraftDownloadListener.onProgress(0, 0)
        minecraftDownloadListener.onStageChanged("Завантаження бібліотек...")

        val filesForDownload = mutableListOf<DownloadService.DownloadFile>()
        val natives = mutableListOf<Map.Entry<String, Artifact>>()

        versionInfo.libraries.forEach Library@{ library ->
            if (!library.rules.applyOnThisPlatform()) return@Library

            val downloads = library.downloads ?: return@Library
            val artifact = downloads.artifact

            if (artifact != null) {
                val jarFile = di.workingDirs().librariesDir.resolve(artifact.path ?: return@Library)
                if (artifact.url == null) return@Library

                filesForDownload.add(
                    DownloadService.DownloadFile(
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

                val jarFile = di.workingDirs().librariesDir.resolve(classifier.value.path ?: return@Library)
                if (classifier.value.url == null) return@Library

                filesForDownload.add(
                    DownloadService.DownloadFile(
                        classifier.value.url!!,
                        classifier.value.sha1,
                        classifier.value.size!!.toLong(),
                        jarFile
                    )
                )
            }
        }
        di.direct.instance<DownloadService>().executeMultiple(filesForDownload) {value: Long, size: Long ->
            minecraftDownloadListener.onProgress(value, size)
        }

        natives.forEach {
            val nativesDir = di.workingDirs().versionsDir.resolve(version.id).resolve("natives")
            val jarFile = di.workingDirs().librariesDir.resolve(it.value.path!!)

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
    private suspend fun downloadAssetIndexes(
        versionInfo: MinecraftVersion
    ) {
        minecraftDownloadListener.onProgress(0, 0)
        minecraftDownloadListener.onStageChanged("Завантаження індексу...")

        val indexesFolder = di.workingDirs().assetsDir.resolve("indexes")
        indexesFolder.createDirectoryIfNotExists()

        val assetIndex = versionInfo.assetIndex ?: return

        val assetId: String = assetIndex.id
        val indexFile = indexesFolder.resolve("$assetId.json")

        DownloadManager(
            di.direct.instance<HttpClient>(),
            di.direct.instance(),
            assetIndex.url,
            indexFile,
            assetIndex.sha1,
            assetIndex.size
        ).downloadFile { value, size ->
            minecraftDownloadListener.onProgress(value, size)
        }
    }

    private suspend fun downloadAssetObjects(
        versionInfo: MinecraftVersion
    ) {
        minecraftDownloadListener.onProgress(0, 0)
        minecraftDownloadListener.onStageChanged("Завантаження активів...")

        val assetId: String = versionInfo.assetIndex?.id ?: return

        val index = json.decodeFromString<AssetsIndexes>(
            IOUtil.readUtf8String(
                di.workingDirs().assetsDir.resolve("indexes").resolve("$assetId.json")
            )
        )

        val assetsDownloadFolder = di.workingDirs().assetsDir.resolve("objects")
        assetsDownloadFolder.createDirectoryIfNotExists()

        val objects = index.objects

        val filesForDownload = mutableListOf<DownloadService.DownloadFile>()

        objects.forEach { obj ->
            val asset = obj.value

            if (asset.hash == null) return@forEach
            val prefix = asset.hash!!.substring(0, 2)
            val saveAs = if (index.virtual)
                di.workingDirs().assetsDir.resolve("virtual").resolve(versionInfo.id!!).resolve(obj.key)
            else
                di.workingDirs().assetsDir.resolve("objects").resolve(prefix).resolve(asset.hash!!)
            val url = resources + prefix + "/" + asset.hash

            filesForDownload.add(
                DownloadService.DownloadFile(url, asset.hash, asset.size, saveAs)
            )
        }
        di.direct.instance<DownloadService>().executeMultiple(
            filesForDownload
        ) { value: Long, size: Long ->
            minecraftDownloadListener.onProgress(value, size)
        }
    }

    private suspend fun downloadJava(version: MinecraftVersion) {
        minecraftDownloadListener.onProgress(0, 0)
        minecraftDownloadListener.onStageChanged("Завантаження Java...")

        val runtimesFile = di.workingDirs().javaDir.resolve("runtimes.json")
        val jreOsName = OperatingSystem.jreOsName
        val javaKey = OperatingSystem.getJavaKey(version)

        if (di.workingDirs().javaDir.resolve("java-runtime-gamma")
            .resolve("bin")
            .resolve("java.exe").exists() && javaKey == "java-runtime-gamma" ) return

        DownloadManager(
            di.direct.instance<HttpClient>(),
            di.direct.instance(),
            fileUrl = allRuntimeUrl,
            saveAs = runtimesFile
        ).downloadFile { _, _ -> }

        val jsonObject = json.parseToJsonElement(
                IOUtil.readUtf8String(di.workingDirs().javaDir.resolve("runtimes.json"))
            ).jsonObject

        val osRuntime = jsonObject[jreOsName]?.jsonObject?.get(javaKey)
            ?: throw IllegalArgumentException("Expected runtime information not found for $jreOsName and $javaKey")

        val runtime = json.decodeFromString<List<JavaRuntime>>(osRuntime.jsonArray.toString())[0]
        val manifestFile = di.workingDirs().javaDir.resolve(javaKey).resolve("manifest.json")

        DownloadManager(
            di.direct.instance<HttpClient>(),
            di.direct.instance(),
            fileUrl = runtime.manifest!!.url!!,
            saveAs = manifestFile
        ).downloadFile { _, _ -> }

        val manifest = json.decodeFromString<JavaRuntimeManifest>(
            IOUtil.readUtf8String(manifestFile)
        )

        val files = mutableListOf<DownloadService.DownloadFile>()
        manifest.files.forEach {
            val jreFile = it.value
            val saveAs = di.workingDirs().javaDir.resolve(javaKey).resolve(it.key)

            if (jreFile.type == "directory"){
                saveAs.createDirectoryIfNotExists()
            }else{
                val raw = jreFile.downloads!!["raw"]!!
                files.add(
                    DownloadService.DownloadFile(raw.url!!, raw.sha1, raw.size, saveAs)
                )
            }
        }

        di.direct.instance<DownloadService>().executeMultiple(files){ value: Long, size: Long ->
            minecraftDownloadListener.onProgress(value, size)
        }
    }

    fun cancel(instanceName: String) {
        currentJob?.cancel(CancellationException("User cancelled download"))
        currentJob = null
        di.instanceManager().deleteInstance(instanceName)
    }
}
