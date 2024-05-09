package ua.besf0r.cubauncher.minecraft.forge

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.utils.io.errors.*
import kotlinx.coroutines.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*
import ua.besf0r.cubauncher.httpClient
import ua.besf0r.cubauncher.instance.Instance
import ua.besf0r.cubauncher.librariesDir
import ua.besf0r.cubauncher.minecraft.MinecraftDownloadListener
import ua.besf0r.cubauncher.network.FileDownloader
import ua.besf0r.cubauncher.util.FileUtil
import ua.besf0r.cubauncher.util.OsEnum
import ua.besf0r.cubauncher.workDir
import java.io.*
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.util.zip.ZipException
import java.util.zip.ZipFile
import kotlin.io.path.deleteIfExists
import kotlin.io.path.name


class ForgeDownloader {
    @Serializable(ForgeDeserializer::class)
    data class VersionManifest(val versions: List<Pair<String, List<String>>>)

    object ForgeDeserializer : KSerializer<VersionManifest> {
        private fun convertEntries(set: Set<Map.Entry<String, JsonElement>>): List<Pair<String, List<String>>> {
            return set.map { entry ->
                entry.key to entry.value.jsonArray.map { it.jsonPrimitive.content }
            }
        }
        override val descriptor = buildClassSerialDescriptor("VersionManifest")

        override fun deserialize(decoder: Decoder): VersionManifest {
            val jsonDecoder = decoder as JsonDecoder
            val element = jsonDecoder.decodeJsonElement()
            return VersionManifest(convertEntries(element.jsonObject.entries))
        }

        override fun serialize(encoder: Encoder, value: VersionManifest) {}
    }

    companion object {
        private const val manifestUrl = "https://files.minecraftforge.net/net/minecraftforge/forge/maven-metadata.json"

        val versions: VersionManifest = runBlocking {
            withContext(Dispatchers.IO) {
                Json.decodeFromString<VersionManifest>(httpClient.get(manifestUrl).bodyAsText())
            }
        }
    }

    private val json = Json { ignoreUnknownKeys = true }

    @OptIn(DelicateCoroutinesApi::class)
    @Throws(IOException::class)
    fun download(
        progress: MinecraftDownloadListener,
        forgeVersion: String,
        instance: Instance
    ) {
        progress.onStageChanged("Downloading Forge($forgeVersion)")

        val installerUrl = "https://maven.minecraftforge.net/net/minecraftforge/forge/" +
                "${forgeVersion}/forge-${forgeVersion}-installer.jar"

        val installerPath = workDir.resolve("forge-installer-${forgeVersion}.jar")
        val installerLog = workDir.resolve("forge-installer-${forgeVersion}.jar.log")

        FileDownloader(installerUrl, null, 0, installerPath)
            .execute { value, size -> progress.onProgress(value, size) }

        var forgeProfile: ForgeProfile? = null

        val libraries: MutableList<Path> = mutableListOf()

        val zipFile = ZipFile(installerPath.toFile())
        zipFile.use { zipFile ->
            val entry = zipFile.getEntry("version.json")
            if (entry != null) {
                val stream = zipFile.getInputStream(entry).readBytes().decodeToString()
                val installProfile = json.decodeFromString<ForgeProfile>(stream)
                forgeProfile = installProfile
            }
        }

        zipFile.close()

        forgeProfile?.libraries?.forEach {
            val artifact = it.downloads.artifact
            val path = librariesDir.resolve(artifact.path)
            libraries.add(path)
        }

        val profiles = workDir.resolve("launcher_profiles.json")
        FileUtil.createFileIfNotExists(profiles)

        val process = ProcessBuilder(
            listOf(
                OsEnum.javaType,
                "-jar",
                installerPath.toFile().name,
                "--installClient"
            )
        ).inheritIO().directory(workDir.toFile()).start()

        GlobalScope.async {
            val inputStream: InputStream = process.inputStream

            val reader = BufferedReader(InputStreamReader(inputStream, StandardCharsets.UTF_8))
            var line: String
            while (reader.readLine().also { line = it } != null) {
                println(line)
            }
        }
        process.waitFor()

        profiles.deleteIfExists()
        installerLog.deleteIfExists()
//        installerPath.deleteIfExists()

        instance.forge = forgeProfile
        instance.forgeLibraries = libraries
    }
}