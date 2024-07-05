package ua.besf0r.kovadlo.minecraft.java

import kotlinx.serialization.Serializable

@Serializable
data class JavaRuntimeManifest (
    var files: Map<String, JreFile> = mapOf()
) {
    @Serializable
    data class JreFile(
        var type: String? = null,
        var executable: Boolean = false,
        var downloads: Map<String, Download>? = null
    ) {
        @Serializable
        data class Download (
            var sha1: String? = null,
            var size: Long = 0,
            var url: String? = null
        )
    }
}