package ua.besf0r.cubauncher.minecraft.auth.microsoft

import kotlinx.serialization.SerialName

class OAuthCodeResponse {
    @SerialName("token_type")
    lateinit var tokenType: String

    @SerialName("scope")
    lateinit var scope: String

    @SerialName("expires_in")
    var expiresIn = 0

    @SerialName("access_token")
    lateinit var accessToken: String

    @SerialName("refresh_token")
    lateinit var refreshToken: String

    @SerialName("id_token")
    lateinit var idToken: String

}
