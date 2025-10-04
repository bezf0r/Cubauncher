package ua.besf0r.kovadlo.minecraft.forge

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*
import ua.besf0r.kovadlo.Logger
import ua.besf0r.kovadlo.logger

class ForgeVersionList(
    private val httpClient: HttpClient
) {
    @Serializable(ForgeDeserializer::class)
    data class Version(val versions: Map<String, List<String>>)

    object ForgeDeserializer : KSerializer<Version> {
        private fun convertEntries(set: Set<Map.Entry<String, JsonElement>>): Map<String, List<String>> {
            return set.associate { entry ->
                entry.key to entry.value.jsonArray.map { it.jsonPrimitive.content }
            }
        }
        override val descriptor = buildClassSerialDescriptor("VersionManifest")

        override fun deserialize(decoder: Decoder): Version {
            val jsonDecoder = decoder as JsonDecoder
            val element = jsonDecoder.decodeJsonElement()
            return Version(convertEntries(element.jsonObject.entries))
        }

        override fun serialize(encoder: Encoder, value: Version) {}
    }

    private val manifestUrl = "https://files.minecraftforge.net/net/minecraftforge/forge/maven-metadata.json"

    val versions: Version = try {
        runBlocking {
            val response = httpClient.get(manifestUrl).bodyAsText()
            Json.decodeFromString<Version>(response)
        }
    } catch (e: Exception) {
        Version(mapOf())
    }
}