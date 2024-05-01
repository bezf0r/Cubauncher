package ua.besf0r.cubauncher.minecraft.forge

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class ForgeProfile(
    val id: String? = null,
    val releaseTime: String? = null,
    val mainClass: String? = null,
    val libraries: List<Library>? = null,
    val type: String? = null,
    val inheritsFrom: String? = null,
    val arguments: ForgeArguments? = null,
    @Transient
    val _comment: Any = "",
    @Transient
    val time: Any = "",
    @Transient
    val logging: Any = "",
){
    @Serializable
    data class Library(
        val name: String,
        val downloads: Downloads
    )

    @Serializable
    data class Downloads(
        val artifact: Artifact
    )

    @Serializable
    data class Artifact(
        val path: String,
        val url: String,
        val sha1: String,
        val size: Int
    )
    @Serializable
    data class ForgeArguments(
        val game: List<String> = listOf(),
        val jvm: List<String> = listOf()
    )

    fun generateArguments(): List<String>{
        return listOf(
            "--launchTarget",
            "forge_client",
            "--fml.forgeVersion",
            id!!.removePrefix("${inheritsFrom!!}-forge"),
            "--fml.mcVersion",
            inheritsFrom,
            "--fml.forgeGroup",
            "net.minecraftforge"
        )
    }
}

