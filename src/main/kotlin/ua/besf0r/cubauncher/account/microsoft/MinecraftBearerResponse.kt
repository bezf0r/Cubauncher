package ua.besf0r.cubauncher.account.microsoft

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class MinecraftBearerResponse(
    val username: String,
    @Transient
    val roles: List<Any> = listOf(),
    @Transient
    val metadata: Any = 1,
    @SerialName("access_token")
    val accessToken: String,
    @SerialName("token_type")
    val tokenType: String,
    @SerialName("expires_in")
    val expiresIn: Int
) {
    private val expires = (System.currentTimeMillis() / 1000) + expiresIn
    fun saveTokens(): MinecraftTokens {
        return MinecraftTokens(accessToken = accessToken, expires = expires)
    }
}
data class MinecraftTokens(
    val accessToken: String,
    val expires: Long,
)
