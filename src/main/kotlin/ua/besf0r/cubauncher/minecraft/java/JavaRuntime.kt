package ua.besf0r.cubauncher.minecraft.java

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class JavaRuntime (
    var availability: Availability? = null,
    var manifest: Manifest? = null,
    var version: Version? = null
){
    @Serializable
    data class Availability (
        var group: Int = 0,
        var progress: Int = 0
    )
    @Serializable
    class Manifest (
        var sha1: String? = null,
        var size: Long = 0,
        var url: String? = null
    )
    @Serializable
    class Version (
        var name: String? = null,
        var released: String? = null
    )
}