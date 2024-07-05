package ua.besf0r.kovadlo.minecraft.optifine

import kotlinx.serialization.Serializable
import ua.besf0r.kovadlo.network.DownloadManager

object OptiFineVersionList {
    private const val MANIFEST_URL = "https://raw.githubusercontent.com/bezf0r/OptiFine-repository/main/optifine.json"
    @Serializable
    data class Version(val filename: String, val mcversion: String, val patch: String, val type: String)
    val versions = DownloadManager.downloadDataList<Version>(MANIFEST_URL)
}