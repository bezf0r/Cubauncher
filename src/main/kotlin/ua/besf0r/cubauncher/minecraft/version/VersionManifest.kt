package ua.besf0r.cubauncher.minecraft.version

import kotlinx.serialization.Serializable

class VersionManifest {
    @Serializable
    data class VersionManifest(
        val latest: Latest,
        val versions: List<Version>
    )
    @Serializable
    data class Version(
        val complianceLevel: Int,
        val id: String,
        val releaseTime: String,
        val sha1: String,
        val time: String,
        val type: String,
        val url: String
    )
    @Serializable
    data class Latest(
        val release: String,
        val snapshot: String
    )
}
