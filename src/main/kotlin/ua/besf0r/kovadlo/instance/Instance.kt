package ua.besf0r.kovadlo.instance

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import ua.besf0r.kovadlo.minecraft.forge.newprofile.ForgeNewInstallProfile
import ua.besf0r.kovadlo.minecraft.forge.newprofile.NewProfilePathSerializer
import ua.besf0r.kovadlo.minecraft.forge.oldprofile.ForgeOldIntallProfile
import ua.besf0r.kovadlo.minecraft.forge.oldprofile.OldProfilePathSerializer
import ua.besf0r.kovadlo.minecraft.liteloader.LiteLoaderPathSerializer
import ua.besf0r.kovadlo.minecraft.liteloader.LiteLoaderProfile
import java.nio.file.Path
import java.nio.file.Paths

@Serializable
class Instance(
    var name: String, var minecraftVersion: String
) {
    var mainClass: String = "net.minecraft.client.main.Main"

    var forgeOldIntallProfile: ForgeOldIntallProfile.VersionInfo? = null
    var forgeNewInstallProfile: ForgeNewInstallProfile? = null

    var liteLoaderProfile: LiteLoaderProfile? = null

    @Serializable(PathListSerializer::class)
    var customLibraries: MutableList<Path> = mutableListOf()
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
