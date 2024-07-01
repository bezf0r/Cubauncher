package ua.besf0r.cubauncher.minecraft.liteloader

import kotlinx.serialization.Serializable
import ua.besf0r.cubauncher.network.DownloadManager

object LiteLoaderManifest {
    private const val MANIFEST_URL = "https://raw.githubusercontent.com/bezf0r/LiteLoader-repository/main/index.json"
    @Serializable
    data class Version(val inheritsFrom: String, val version: String)

    val versions = DownloadManager.downloadDataList<Version>(MANIFEST_URL)
}