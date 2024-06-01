package ua.besf0r.cubauncher.minecraft.fabric

import kotlinx.serialization.Serializable

@Serializable
data class FabricProfile(
    val intermediary: Intermediary,
    val loader: Loader,
    val launcherMeta: LauncherMeta
){
    @Serializable
    data class Intermediary(
        val maven: String,
        val stable: Boolean,
        val version: String
    )
    @Serializable
    data class Loader(
        val build: Int,
        val maven: String,
        val separator: String,
        val stable: Boolean,
        val version: String
    )
    @Serializable
    data class LauncherMeta(
        val mainClass: MainClass,
        val min_java_version: Int,
        val version: Int,
        val libraries: Libraries
    ){
        @Serializable
        data class MainClass(
            val client: String,
            val server: String
        )
        @Serializable
        data class Libraries(
            val client: List<Common>,
            val common: List<Common>,
            val development: List<Common>,
            val server: List<Common>
        )
        @Serializable
        data class Common(
            val md5: String,
            val name: String,
            val sha1: String,
            val sha256: String,
            val sha512: String,
            val size: Int,
            val url: String
        )
    }
}