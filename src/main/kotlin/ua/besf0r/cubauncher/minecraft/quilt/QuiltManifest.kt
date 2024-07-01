package ua.besf0r.cubauncher.minecraft.quilt

import kotlinx.serialization.Serializable
import ua.besf0r.cubauncher.network.DownloadManager.Companion.downloadDataList

object QuiltManifest {
    private const val MANIFEST_URL = "https://meta.quiltmc.org/v3/versions/game"
    @Serializable
    data class Version(val version: String, val stable: Boolean)
    val minecraftVersions = downloadDataList<Version>(MANIFEST_URL)

    private const val LOADERS_URL = "https://meta.quiltmc.org/v3/versions/loader"
    @Serializable
    data class Loader(val version: String, val stable: Boolean = true)
    val loaderVersion = downloadDataList<Loader>(LOADERS_URL)
}