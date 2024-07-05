package ua.besf0r.kovadlo.minecraft.forge.oldprofile

import kotlinx.serialization.KSerializer
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

object OldProfilePathSerializer : KSerializer<ForgeOldIntallProfile.VersionInfo?> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("VersionInfo", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: ForgeOldIntallProfile.VersionInfo?) {
        val jsonFile = value?.let {
            versionsDir.resolve(it.id!!).resolve("${it.id}.json")
        }
        encoder.encodeString(jsonFile?.pathString ?: "")
    }

    private val json = Json { ignoreUnknownKeys = true }

    override fun deserialize(decoder: Decoder): ForgeOldIntallProfile.VersionInfo {
        val path = Path(decoder.decodeString())
        if (!path.exists()) return ForgeOldIntallProfile.VersionInfo()
        return json.decodeFromString<ForgeOldIntallProfile.VersionInfo>(IOUtil.readUtf8String(path))
    }
}