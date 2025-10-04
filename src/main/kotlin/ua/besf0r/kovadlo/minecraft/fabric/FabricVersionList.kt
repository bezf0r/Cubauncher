package ua.besf0r.kovadlo.minecraft.fabric

import kotlinx.serialization.Serializable
import ua.besf0r.kovadlo.network.DownloadService

class FabricVersionList(
    private val downloadService: DownloadService
) {
    private val MANIFEST_URL = "https://meta.fabricmc.net/v2/versions/game"
    @Serializable
    data class Version(val version: String, val stable: Boolean)
    val minecraftVersions = downloadService.downloadDataList<Version>(MANIFEST_URL)

    private val LOADERS_URL = "https://meta.fabricmc.net/v2/versions/loader"
    @Serializable
    data class Loader(val version: String, val stable: Boolean)
    val loaderVersion = downloadService.downloadDataList<Loader>(LOADERS_URL)
}