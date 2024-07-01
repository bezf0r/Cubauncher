package ua.besf0r.cubauncher.minecraft

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.*
import ua.besf0r.cubauncher.*
import ua.besf0r.cubauncher.instance.Instance
import ua.besf0r.cubauncher.minecraft.OperatingSystem.Companion.applyOnThisPlatform
import ua.besf0r.cubauncher.minecraft.java.JavaRuntime
import ua.besf0r.cubauncher.minecraft.java.JavaRuntimeManifest
import ua.besf0r.cubauncher.minecraft.version.*
import ua.besf0r.cubauncher.network.DownloadListener
import ua.besf0r.cubauncher.network.DownloadManager
import ua.besf0r.cubauncher.network.file.FilesManager.createDirectoryIfNotExists
import ua.besf0r.cubauncher.network.file.FilesManager.createFileIfNotExists
import ua.besf0r.cubauncher.network.file.IOUtil
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
    suspend fun downloadMinecraft(instance: Instance) {
        currentJob.async {
            currentInstance = instance

            versionsDir.resolve(instance.minecraftVersion).createDirectoryIfNotExists()

            val manifestRead = IOUtil.readUtf8String(
                versionsDir.resolve("version_manifest_v2.json")
            )
            val manifest = json.decodeFromString<VersionManifest.VersionManifest>(manifestRead)

            val versions = manifest.versions
            val version = versions.find { it.id == instance.minecraftVersion }!!

            val startTime = System.currentTimeMillis()

            saveClientJson(version)
            downloadJava(instance.versionInfo!!)
            downloadClient(version)
            downloadLibraries(version)
            downloadAssetIndexes()
            downloadAssetObjects()

            val endTime = System.currentTimeMillis()
            val duration = ((endTime - startTime).toInt())

            Logger.publish("Час завантаження - ${(duration.toDouble() / 60000.0)} хв")
        }.await()
    }

    @Throws(IOException::class)
    private fun saveClientJson(version: VersionManifest.Version) {
        if (!currentJob.isActive) return
        val jsonFile = versionsDir.resolve(version.id).resolve(version.id + ".json")

        DownloadManager(version.url, version.sha1, saveAs = jsonFile).execute { _, _ -> }

        currentInstance!!.versionInfo = json.decodeFromString<MinecraftVersion>(IOUtil.readUtf8String(jsonFile))
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
        ).execute { value: Long, size: Long ->
            minecraftDownloadListener.onProgress(value, size)
        }
    }

    @Throws(IOException::class)
    private fun downloadLibraries(version: VersionManifest.Version) {
        if (!currentJob.isActive) return

        minecraftDownloadListener.onProgress(0, 0)
        minecraftDownloadListener.onStageChanged("Завантаження бібліотек...")

        val versionInfo = currentInstance!!.versionInfo ?: return

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
        DownloadManager.executeMultiple(filesForDownload) {value: Long, size: Long ->
            minecraftDownloadListener.onProgress(value, size)
        }

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
    private fun downloadAssetIndexes() {
        if (!currentJob.isActive) return
        minecraftDownloadListener.onProgress(0, 0)
        minecraftDownloadListener.onStageChanged("Завантаження індексу...")

        val indexesFolder = assetsDir.resolve("indexes")
        indexesFolder.createDirectoryIfNotExists()

        val assetIndex = currentInstance!!.versionInfo?.assetIndex ?: return

        val assetId: String = assetIndex.id
        val indexFile = indexesFolder.resolve("$assetId.json")

        DownloadManager(
            assetIndex.url,
            assetIndex.sha1,
            assetIndex.size,
            indexFile
        ).execute { value, size ->
            minecraftDownloadListener.onProgress(value, size)
        }
    }

    private fun downloadAssetObjects() {
        if (!currentJob.isActive) return

        minecraftDownloadListener.onProgress(0, 0)
        minecraftDownloadListener.onStageChanged("Завантаження активів...")

        val assetId: String = currentInstance!!.versionInfo?.assetIndex?.id ?: return

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

            val prefix = asset.hash.substring(0, 2)

            val saveAs = if (index.virtual) {
                assetsDir.resolve("virtual").resolve(currentInstance!!.versionInfo!!.id!!).resolve(obj.key)
            } else {
                assetsDir.resolve("objects").resolve(prefix).resolve(asset.hash)
            }

            val url = resources + prefix + "/" + asset.hash

            filesForDownload.add(
                DownloadManager.DownloadFile(url, asset.hash, asset.size, saveAs)
            )
        }
        DownloadManager.executeMultiple(filesForDownload) { value: Long, size: Long ->
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

        if (javaKey == "java-runtime-gamma") return

        DownloadManager(
            fileUrl = allRuntimeUrl,
            saveAs = runtimesFile
        ).execute(false) { _, _ ->  }

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
        ).execute(false){_,_ ->}

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

        DownloadManager.executeMultiple(files){ value: Long, size: Long ->
            minecraftDownloadListener.onProgress(value, size)
        }
    }

    fun cancel(instanceName: String) {
        currentJob.cancel()
        instanceManager.deleteInstance(instanceName)
    }
}
