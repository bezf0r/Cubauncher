package ua.besf0r.cubauncher.minecraft.auth.microsoft

data class MinecraftProfile (
    var id: String,
    var name: String,
    var accessToken: String,
    var skins: List<MinecraftSkin> = listOf()
)
