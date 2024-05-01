package ua.besf0r.cubauncher.minecraft.auth.microsoft

import kotlinx.serialization.SerialName

class MinecraftAuthResponse {
    @SerialName("username")
    lateinit var username: String

    @SerialName("roles")
    var roles: List<Any> = listOf()

    @SerialName("access_token")
    lateinit var accessToken: String

    @SerialName("token_type")
    lateinit var tokenType: String

    @SerialName("expires_in")
    var expiresIn = 0

}
