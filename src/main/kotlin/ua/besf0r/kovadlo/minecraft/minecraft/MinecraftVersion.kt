package ua.besf0r.kovadlo.minecraft.minecraft

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*

@Serializable
data class MinecraftVersion (
    var arguments: MinecraftArguments? = null,
    var minecraftArguments: String? = null,
    var assetIndex: AssetIndex? = null,
    var assets: String? = null,
    var complianceLevel: Int? = null,
    var downloads: ClientDownloads? = null,
    var id: String?= null,
    var javaVersion: JavaVersion ?= null,
    var libraries: List<Library> = listOf(),
    var releaseTime: String?= null,
    var time: String?= null,
    var type: String? = null,
    var mainClass: String? = null,
)
@Serializable
data class MinecraftArguments(
    val game: List<Argument> = listOf(),
    val jvm: List<Argument> = listOf()
)
@Serializable
data class ClientDownloads (
    var client: ClientDownload
)
@Serializable
data class ClientDownload (
    val sha1: String,
    val url: String,
    val size: Long = 0
)

@Serializable(ArgumentsDeserializer::class)
data class Argument (
    var value: List<String> = mutableListOf(),
    var rules: List<Rule> = mutableListOf()
)
object ArgumentsDeserializer : KSerializer<Argument> {
    override val descriptor = buildClassSerialDescriptor("Argument")

    override fun deserialize(decoder: Decoder): Argument {
        val jsonDecoder = decoder as JsonDecoder
        val element = jsonDecoder.decodeJsonElement()
        return if (element is JsonPrimitive) Argument(listOf(element.content))
        else {
            val obj = element.jsonObject
            val rules = obj["rules"]?.jsonArray
                ?.map { jsonDecoder.json.decodeFromJsonElement(Rule.serializer(), it.jsonObject) }
            val valueJson = obj["value"]
            val value =
                if (valueJson is JsonArray) valueJson.map { it.jsonPrimitive.content }
                else listOf(valueJson!!.jsonPrimitive.content)
            Argument(value,rules!!)
        }
    }

    override fun serialize(encoder: Encoder, value: Argument) {}
}
@Serializable
class Rule {
    val action: Action? = null

    val os: Os? = null

    val features: Map<String, Boolean>? = null

    enum class Action(val s: String) {
        @SerialName("allow")
        ALLOW("allow"),
        @SerialName("disallow")
        DISALLOW("disallow");
    }
    @Serializable
    data class Os (
        var name : String? = null,
        var version: String? = null,
        var arch : String? = null
    )
}


@Serializable
data class AssetIndex(
    var id: String,
    var sha1: String,
    var size: Long = 0,
    var totalSize: Long = 0,
    var url: String
)

@Serializable
data class AssetsIndexes(
    @SerialName("map_to_resources")
    var mapToResources: Boolean = false,
    var virtual: Boolean = false,
    var objects: Map<String, AssetObject> = mapOf()
)
@Serializable
data class AssetObject (
    var size: Long = 0,
    var hash: String? = null
)

@Serializable
data class Artifact (
    var path : String? = null,
    var sha1 : String? = null,
    var size : Int?    = null,
    var url  : String? = null
)
@Serializable
data class JavaVersion (
    var component : String? = null,
    var majorVersion : Int? = null
)
@Serializable
class Library {
    val name: String? = null
    val downloads: Downloads? = null
    val natives: Map<String, String>? = null
    val rules: List<Rule>? = null
    val extract: Extract? = null

    @Serializable
    data class Downloads (
        val artifact: Artifact? = null,
        val classifiers: Map<String, Artifact>? = null
    )
    @Serializable
    data class Extract (
        var exclude: List<String> = listOf()
    )
}