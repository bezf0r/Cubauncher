package ua.besf0r.cubauncher.minecraft.forge

import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*
import ua.besf0r.cubauncher.Logger
import ua.besf0r.cubauncher.httpClient

object ForgeManifest {
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

    val versions: VersionManifest = try {
        runBlocking {
            val response = httpClient.get(manifestUrl).bodyAsText()
            val decodedManifest = Json.decodeFromString<VersionManifest>(response)
            VersionManifest(decodedManifest.versions)
        }
    } catch (e: Exception) {
        Logger.publish("Помилка при завантаженні версій forge: ${e.stackTraceToString()}")
        VersionManifest(emptyList())
    }
}