package ua.besf0r.cubauncher.minecraft.quilt

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class QuiltProfile(
    @Transient
    val arguments: List<String>? = null,
    val id: String,
    val inheritsFrom: String,
    val libraries: List<Library>,
    val mainClass: String,
    val releaseTime: String,
    val time: String,
    val type: String
){
    @Serializable
    data class Library(
        val name: String,
        val url: String
    )
}