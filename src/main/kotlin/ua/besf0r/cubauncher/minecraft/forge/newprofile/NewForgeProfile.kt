package ua.besf0r.cubauncher.minecraft.forge.newprofile

import kotlinx.serialization.Serializable


//For Forge 1.12.2+
@Serializable
data class NewForgeProfile(
    val id: String? = null,
    val releaseTime: String? = null,
    val mainClass: String? = null,
    val libraries: List<Library>? = null,
    val type: String? = null,
    val inheritsFrom: String? = null,
    val arguments: ForgeArguments? = null,
    val minecraftArguments: String? = null
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

    val gameArguments = mutableListOf<String>().apply {
        addAll(arguments?.game ?: mutableListOf())
        minecraftArguments?.let { add(it) }
    }
    val jvmArguments =  arguments?.jvm?: mutableListOf()
}

