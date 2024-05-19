package ua.besf0r.cubauncher.account.microsoft

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

object AccountUtil {
    private const val GET_PROFILE_URL = "https://api.minecraftservices.com/minecraft/profile"

    private val client = HttpClient(CIO)

    private val json = Json { ignoreUnknownKeys = true }

    fun fetchMinecraftProfile(
        token: MinecraftTokens,
        onFetch: (MinecraftProfile) -> Unit
    ) {
        runBlocking {
            val response: HttpResponse = client.get(GET_PROFILE_URL) {
                ContentType.Application.Json
                header("Authorization", "Bearer ${token.accessToken}")
            }
            onFetch(json.decodeFromString(response.bodyAsText()))
        }


//        return when (response.status) {
//            HttpStatusCode.NotFound -> throw MinecraftNotPurchasedError()
//            HttpStatusCode.OK -> Json.decodeFromString(response.bodyAsText())
//            else -> throw MinecraftAPIException(response.status, response.bodyAsText())
//        }
    }
    @Serializable
    data class MinecraftProfile(
        val id: String,
        val name: String,
        val skins: List<MinecraftSkin>
    )

    @Serializable
    data class MinecraftSkin (
        var id: String,
        var state: String,
        var url: String,
        var variant: String
    )
}