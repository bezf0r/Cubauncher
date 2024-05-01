package ua.besf0r.cubauncher.instance

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import ua.besf0r.cubauncher.minecraft.forge.ForgeProfile
import ua.besf0r.cubauncher.minecraft.version.MinecraftVersion
import ua.besf0r.cubauncher.util.IOUtils
import ua.besf0r.cubauncher.versionsDir
import java.nio.file.Path

private val json = Json {
    ignoreUnknownKeys = true
    isLenient = true
}

@Serializable
class Instance(
    var name: String, var minecraftVersion: String
) {
    var minimumMemory = 512
    var maximumMemory = 2048

    var versionInfo: MinecraftVersion? = null

    var forge: ForgeProfile? = null
    var forgeLibraries: MutableList<Path> = mutableListOf()
}
