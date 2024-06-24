package ua.besf0r.cubauncher.account.microsoft

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.*
import kotlinx.coroutines.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import ua.besf0r.cubauncher.Logger
import ua.besf0r.cubauncher.account.Account
import ua.besf0r.cubauncher.account.MicrosoftAccount
import java.util.concurrent.TimeoutException

object MicrosoftOAuthUtils {
    private const val CLIENT_ID = "feb3836f-0333-4185-8eb9-4cbf0498f947"
    private const val TENANT = "consumers"

    private const val DEVICE_CODE_URL = "https://login.microsoftonline.com/$TENANT/oauth2/v2.0/devicecode"
    private const val TOKEN_CHECK_URL = "https://login.microsoftonline.com/$TENANT/oauth2/v2.0/token"
    private const val XBOX_LIVE_AUTH_URL = "https://user.auth.xboxlive.com/user/authenticate"
    private const val XSTS_URL = "https://xsts.auth.xboxlive.com/xsts/authorize"
    private const val LOGIN_WITH_XBOX_URL = "https://api.minecraftservices.com/authentication/login_with_xbox"
    private const val MAX_CHECK_TIME = 900

    private val client = HttpClient(CIO)

    var currentCallBack: MicrosoftDeviceCode? = null

    @Throws(TimeoutException::class)
    fun obtainDeviceCodeAsync(
        deviceCodeCallback: (MicrosoftDeviceCode) -> Unit,
        errorCallback: (Throwable) -> Unit,
        successCallback: (AuthenticationResponse) -> Unit
    ) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val deviceCodeDeferred = async { obtainDeviceCode() }
            val deviceCode = deviceCodeDeferred.await() ?: return@launch

            Logger.publish("Код авторизації Microsoft: ${deviceCode.userCode}")
            deviceCodeCallback(deviceCode)
            currentCallBack = deviceCode

            val start = System.currentTimeMillis() / 1000

            suspend fun checkToken() {
                try {
                    val responseDeferred = async { checkDeviceCode(deviceCode) }
                    val response = responseDeferred.await()
                    val time = System.currentTimeMillis() / 1000
                    if (time > start + MAX_CHECK_TIME) {
                        throw TimeoutException("Час вийшов,не вдалося успішно авторизуватися: ${deviceCode.userCode}").apply {
                            Logger.publish(stackTraceToString())
                        }
                    }

                    if (response == null) {
                        delay(deviceCode.interval * 1000L)
                        checkToken()
                    } else {
                        Logger.publish("Код (${deviceCode.userCode}) успішно авторизований, заходимо в аккаунт ...")
                        successCallback(response)
                    }
                } catch (exception: Throwable) {
                    errorCallback(exception)
                }
            }
            checkToken()
        } catch (exception: Throwable) {
            errorCallback(exception)

        }
    }

    private val json = Json { ignoreUnknownKeys = true }

    @OptIn(InternalAPI::class)
    private suspend fun obtainDeviceCode(): MicrosoftDeviceCode? {
        val response: HttpResponse = client.post(DEVICE_CODE_URL) {
            contentType(ContentType.Application.Json)
            body = MultiPartFormDataContent(formData {
                append("client_id", CLIENT_ID)
                append("scope","XboxLive.signin offline_access")
            })
        }

        if (response.status != HttpStatusCode.OK) {
            println(response.bodyAsText())
            return null
        }

        return json.decodeFromString(response.bodyAsText())
    }

    @OptIn(InternalAPI::class)
    suspend fun checkDeviceCode(deviceCode: MicrosoftDeviceCode): AuthenticationResponse? {
        val response: HttpResponse = client.post(TOKEN_CHECK_URL) {
            contentType(ContentType.Application.Json)
            body = MultiPartFormDataContent(formData {
                append("grant_type","urn:ietf:params:oauth:grant-type:device_code")
                append("client_id", CLIENT_ID)
                append("device_code", deviceCode.deviceCode)
            })
        }

        if (response.status != HttpStatusCode.OK) {
            return null
        }

        return json.decodeFromString(response.bodyAsText())
    }

    @OptIn(InternalAPI::class)
    suspend fun refreshToken(refreshToken: String): AuthenticationResponse? {
        val response: HttpResponse = client.post(TOKEN_CHECK_URL) {
            contentType(ContentType.Application.Json)
            body = MultiPartFormDataContent(formData {
                append("client_id", CLIENT_ID)
                append("grant_type", "refresh_token")
                append("scope", "XboxLive.signin offline_access")
                append("refresh_token", refreshToken)
            })
        }

        if (response.status != HttpStatusCode.OK) {
            println(response.bodyAsText())
            return null
        }

        return json.decodeFromString(response.bodyAsText())
    }

   fun loginToMicrosoftAccount(
        response: AuthenticationResponse,
        onLogin: (Account) -> Unit
    ) {
       runBlocking {
           Logger.publish("Авторизація в Microsoft аккаунт...")

           val msaTokens = response.saveTokens()
           val xboxLiveToken = getXboxLiveToken(msaTokens) ?: return@runBlocking
           val xstsToken = getXSTSToken(xboxLiveToken) ?: return@runBlocking

           val minecraftToken = getMinecraftBearerAccessToken(xboxLiveToken, xstsToken)?.saveTokens() ?: return@runBlocking

           AccountUtil.fetchMinecraftProfile(minecraftToken) { profile ->
               val account = MicrosoftAccount(
                   uuid = profile.id,
                   username = profile.name,
                   accessToken = minecraftToken.accessToken,
                   refreshToken = msaTokens.refreshToken
               )
               onLogin(account)
           }
       }
    }

    @OptIn(InternalAPI::class)
    suspend fun getXboxLiveToken(msaTokens: AuthenticationResponse.MicrosoftTokens): XboxLiveToken? {
        val response: HttpResponse = client.post(XBOX_LIVE_AUTH_URL) {
            contentType(ContentType.Application.Json)
            body = """
                {
                    "Properties": {
                        "AuthMethod": "RPS",
                        "SiteName": "user.auth.xboxlive.com",
                        "RpsTicket": "d=${msaTokens.accessToken}"
                    },
                    "RelyingParty": "http://auth.xboxlive.com",
                    "TokenType": "JWT"
                }
            """.trimIndent()
        }

        if (response.status != HttpStatusCode.OK) {
            println(response.bodyAsText())
            return null
        }

        return json.decodeFromString(response.bodyAsText())
    }

    @OptIn(InternalAPI::class)
    suspend fun getXSTSToken(xBoxLiveToken: XboxLiveToken): XSTSToken? {
        val response: HttpResponse = client.post(XSTS_URL) {
            contentType(ContentType.Application.Json)
            body = """
                {
                    "Properties": {
                        "SandboxId": "RETAIL",
                        "UserTokens": [
                            "${xBoxLiveToken.token}"
                        ]
                    },
                    "RelyingParty": "rp://api.minecraftservices.com/",
                    "TokenType": "JWT"
                }
            """.trimIndent()
        }

        if (response.status != HttpStatusCode.OK) {
            val error: XboxAPIError = response.body<XboxAPIError>()
            val errorMessage = when (error.error) {
                2148916233 -> "You don't have an Xbox account!"
                2148916235 -> "Xbox Live is banned in your country!"
                2148916236, 2148916237 -> "Your account needs adult verification (South Korea)"
                2148916238 -> "This account is a child account!"
                else -> error.message ?: "Unknown"
            }
            println(errorMessage)
            return null
        }

        return json.decodeFromString(response.bodyAsText())
    }

    @OptIn(InternalAPI::class)
    suspend fun getMinecraftBearerAccessToken(
        xBoxLiveToken: XboxLiveToken,
        xstsToken: XSTSToken
    ): MinecraftBearerResponse? {
        val response: HttpResponse = client.post(LOGIN_WITH_XBOX_URL) {
            contentType(ContentType.Application.Json)
            body = """
                {
                   "identityToken" : "XBL3.0 x=${xBoxLiveToken.userHash};${xstsToken.token}",
                   "ensureLegacyEnabled" : true
                }
            """.trimIndent()
        }

        if (response.status != HttpStatusCode.OK) {
            println(response.bodyAsText())
            return null
        }

        return json.decodeFromString(response.bodyAsText())
    }
}
@Serializable
data class XboxAPIError(
    @SerialName("Identity") val identity: Int,
    @SerialName("XErr") val error: Long,
    @SerialName("Message") val message: String?,
    @SerialName("Redirect") val redirect: String?,
)