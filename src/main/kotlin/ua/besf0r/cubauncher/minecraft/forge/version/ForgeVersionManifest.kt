package ua.besf0r.cubauncher.minecraft.forge.version

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*
import ua.besf0r.cubauncher.httpClient

object ForgeVersionManifest {
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

    private const val manifestUrl = "https://files.minecraftforge.net/net/minecraftforge/forge/maven-metadata.json"

    private val requiredVersions = listOf(
        "1.12.2", "1.13.2", "1.14.2", "1.14.3", "1.14.4", "1.15",
        "1.15.1", "1.15.2", "1.16.1", "1.16.2", "1.16.3", "1.16.4", "1.16.5", "1.17.1", "1.18",
        "1.18.1", "1.18.2", "1.19", "1.19.1", "1.19.2", "1.19.3", "1.19.4", "1.20", "1.20.1",
        "1.20.2", "1.20.3", "1.20.4", "1.20.6"
    )

    val versions: VersionManifest = try {
        runBlocking {
            val response = httpClient.get(manifestUrl).bodyAsText()
            val decodedManifest = Json.decodeFromString<VersionManifest>(response)
            VersionManifest(decodedManifest.versions.filter { it.first in requiredVersions })
        }
    } catch (e: Exception) {
        println("Помилка при завантаженні версій forge: ${e.message}")
        VersionManifest(emptyList())
    }
}