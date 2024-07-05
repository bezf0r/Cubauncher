package ua.besf0r.kovadlo.minecraft.fabric

import kotlinx.serialization.Serializable

@Serializable
data class FabricInstallProfile(
    val arguments: Arguments,
    val id: String,
    val inheritsFrom: String,
    val libraries: List<Library>,
    val mainClass: String,
    val releaseTime: String,
    val time: String,
    val type: String
){
    @Serializable
    data class Arguments(
        val game: List<String>,
        val jvm: List<String>
    )
    @Serializable
    data class Library(
        val md5: String? = null,
        val name: String,
        val sha1: String? = null,
        val sha256: String? = null,
        val sha512: String? = null,
        val size: Int? = null,
        val url: String
    )
}