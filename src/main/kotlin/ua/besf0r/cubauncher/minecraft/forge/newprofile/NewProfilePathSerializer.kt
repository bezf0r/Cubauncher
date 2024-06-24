package ua.besf0r.cubauncher.minecraft.forge.newprofile

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import ua.besf0r.cubauncher.network.file.IOUtil
import ua.besf0r.cubauncher.versionsDir
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.pathString

object NewProfilePathSerializer : KSerializer<NewForgeProfile?> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("NewForgeProfile", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: NewForgeProfile?) {
        val jsonFile = value?.let {
            versionsDir.resolve(it.id!!).resolve("${it.id}.json")
        }
        encoder.encodeString(jsonFile?.pathString ?: "")
    }

    private val json = Json { ignoreUnknownKeys = true }

    override fun deserialize(decoder: Decoder): NewForgeProfile {
        val path = Path(decoder.decodeString())
        if (!path.exists()) return NewForgeProfile()
        return json.decodeFromString<NewForgeProfile>(IOUtil.readUtf8String(path))
    }
}