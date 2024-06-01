package ua.besf0r.cubauncher.instance

import kotlinx.serialization.*
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import kotlinx.serialization.json.Json
import ua.besf0r.cubauncher.minecraft.forge.ForgeProfile
import ua.besf0r.cubauncher.minecraft.version.MinecraftVersion
import ua.besf0r.cubauncher.network.file.IOUtil
import ua.besf0r.cubauncher.versionsDir
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.pathString

@Serializable
class Instance(
    var name: String, var minecraftVersion: String
) {
    var mainClass: String = "net.minecraft.client.main.Main"
    @Serializable(MinecraftVersionSerializer::class)
    var versionInfo: MinecraftVersion? = null

    var forge: ForgeProfile? = null
    @Serializable(PathListSerializer::class)
    var forgeLibraries: MutableList<Path> = mutableListOf()

    @Serializable(PathListSerializer::class)
    var fabricLibraries: MutableList<Path> = mutableListOf()
}
object PathListSerializer : KSerializer<MutableList<Path>> {
    override val descriptor: SerialDescriptor = ListSerializer(String.serializer()).descriptor

    override fun serialize(encoder: Encoder, value: MutableList<Path>) {
        encoder.encodeSerializableValue(ListSerializer(String.serializer()), value.map { it.toString() })
    }

    override fun deserialize(decoder: Decoder): MutableList<Path> {
        return decoder.decodeSerializableValue(ListSerializer(String.serializer()))
            .map { Paths.get(it) }.toMutableList()
    }
}
object MinecraftVersionSerializer : KSerializer<MinecraftVersion?> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("MinecraftVersion", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: MinecraftVersion?) {
        val jsonFile = value?.let {
            versionsDir.resolve(it.id!!).resolve("${it.id}.json")
        }
        encoder.encodeString(jsonFile?.pathString ?: "")
    }

    private val json = Json { ignoreUnknownKeys = true }

    override fun deserialize(decoder: Decoder): MinecraftVersion {
        val path = Path(decoder.decodeString())
        if (!path.exists()) return MinecraftVersion()
        return json.decodeFromString<MinecraftVersion>(IOUtil.readUtf8String(path))
    }
}