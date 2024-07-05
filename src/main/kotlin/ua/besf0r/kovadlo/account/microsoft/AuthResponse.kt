package ua.besf0r.kovadlo.account.microsoft

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class AuthenticationResponse(
    @SerialName("token_type")
    var tokenType: String,
    @SerialName("scope")
    var scope: String,
    @SerialName("expires_in")
    var expiresIn: Int,
    @SerialName("access_token")
    var accessToken: String,
    @SerialName("refresh_token")
    var refreshToken: String
) {
    private val expires: Long = (System.currentTimeMillis() / 1000L) + expiresIn

    fun saveTokens(): MicrosoftTokens {
        return MicrosoftTokens(accessToken = accessToken, refreshToken = refreshToken, expires = expires)
    }

    data class MicrosoftTokens(
        val accessToken: String,
        val refreshToken: String,
        val expires: Long,
    )
}