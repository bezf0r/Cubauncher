package ua.besf0r.kovadlo.minecraft.liteloader

import kotlinx.serialization.Serializable
import ua.besf0r.kovadlo.network.DownloadManager

object LiteLoaderVersionList {
    private const val MANIFEST_URL = "https://raw.githubusercontent.com/bezf0r/LiteLoader-repository/main/index.json"
    @Serializable
    data class Version(val inheritsFrom: String, val version: String)

    val versions = DownloadManager.downloadDataList<Version>(MANIFEST_URL)
}