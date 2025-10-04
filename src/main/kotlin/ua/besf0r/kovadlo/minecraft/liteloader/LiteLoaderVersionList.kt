package ua.besf0r.kovadlo.minecraft.liteloader

import kotlinx.serialization.Serializable
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import ua.besf0r.kovadlo.network.DownloadManager
import ua.besf0r.kovadlo.network.DownloadService

class LiteLoaderVersionList(
    private val downloadService: DownloadService
) {
    private val MANIFEST_URL = "https://raw.githubusercontent.com/bezf0r/LiteLoader-repository/main/index.json"
    @Serializable
    data class Version(val inheritsFrom: String, val version: String)

    val versions = downloadService.downloadDataList<Version>(MANIFEST_URL)
}