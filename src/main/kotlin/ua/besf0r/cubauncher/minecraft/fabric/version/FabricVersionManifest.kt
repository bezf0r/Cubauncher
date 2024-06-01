package ua.besf0r.cubauncher.minecraft.fabric.version

import kotlinx.serialization.Serializable
import ua.besf0r.cubauncher.network.DownloadManager.Companion.downloadDataList

object FabricVersionManifest {
    private const val MANIFEST_URL = "https://meta.fabricmc.net/v2/versions/game"
    @Serializable
    data class Version(val version: String, val stable: Boolean)
    val minecraftVersions = downloadDataList<Version>(MANIFEST_URL)


    private const val LOADERS_URL = "https://meta.fabricmc.net/v2/versions/loader"
    @Serializable
    data class Loader(val version: String, val stable: Boolean)
    val loaderVersion = downloadDataList<Loader>(LOADERS_URL)
}