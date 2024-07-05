package ua.besf0r.kovadlo.minecraft.liteloader

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import ua.besf0r.kovadlo.network.file.IOUtil
import ua.besf0r.kovadlo.versionsDir
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.pathString

@Serializable
data class LiteLoaderProfile(
    @SerialName("+tweakers")
    val tweakers: List<String>? = null,
    val formatVersion: Int? = null,
    val libraries: List<Library>? = null,
    val mainClass: String? = null,
    val name: String? = null,
    val order: Int? = null,
    val releaseTime: String? = null,
    val requires: List<Require>? = null,
    val type: String? = null,
    val uid: String? = null,
    val version: String? = null
){
    @Serializable
    data class Library(
        @SerialName("MMC-hint")
        val hint: String? = null,
        val name: String,
        val url: String? = null
    )
    @Serializable
    data class Require(
        val equals: String? = null,
        val uid: String? = null
    )
}
object LiteLoaderPathSerializer : KSerializer<LiteLoaderProfile?> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("LiteLoaderProfile", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: LiteLoaderProfile?) {
        val jsonFile = value?.let {
            versionsDir.resolve("liteloader-${it.version}.json")
        }
        encoder.encodeString(jsonFile?.pathString ?: "")
    }

    private val json = Json { ignoreUnknownKeys = true }

    override fun deserialize(decoder: Decoder): LiteLoaderProfile {
        val path = Path(decoder.decodeString())
        if (!path.exists()) return LiteLoaderProfile()
        return json.decodeFromString<LiteLoaderProfile>(IOUtil.readUtf8String(path))
    }
}