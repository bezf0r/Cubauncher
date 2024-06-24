package ua.besf0r.cubauncher.minecraft.forge.oldprofile

import kotlinx.serialization.Serializable

@Serializable
data class OldForgeProfile (
    val install: Install? = null,
    val versionInfo: VersionInfo? = null
){
    @Serializable
    data class Install(
        val filePath: String,
        val logo: String,
        val minecraft: String,
        val mirrorList: String,
        val path: String,
        val profileName: String,
        val target: String,
        val version: String,
        val welcome: String
    )
    @Serializable
    data class VersionInfo(
        val assets: String? = null,
        val id: String? = null,
        val inheritsFrom: String? = null,
        val minecraft: String? = null,
        val jar: String? = null,
        val libraries: List<Library>? = null,
        val mainClass: String? = null,
        val minecraftArguments: String? = null,
        val minimumLauncherVersion: Int? = null,
        val releaseTime: String? = null,
        val time: String? = null,
        val type: String? = null
    ){
        @Serializable
        data class Library(
            val checksums: List<String> = listOf(),
            val clientreq: Boolean = false,
            val name: String? = null,
            val serverreq: Boolean = false,
            val url: String? = null
        )
    }
}