package ua.besf0r.kovadlo.minecraft.optifine

import kotlinx.serialization.Serializable
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import ua.besf0r.kovadlo.network.DownloadManager
import ua.besf0r.kovadlo.network.DownloadService

class OptiFineVersionList(
    private val downloadService: DownloadService
) {
    private val MANIFEST_URL = "https://raw.githubusercontent.com/bezf0r/OptiFine-repository/main/optifine.json"
    @Serializable
    data class Version(val filename: String, val mcversion: String, val patch: String, val type: String)
    val versions = downloadService.downloadDataList<Version>(MANIFEST_URL)
}