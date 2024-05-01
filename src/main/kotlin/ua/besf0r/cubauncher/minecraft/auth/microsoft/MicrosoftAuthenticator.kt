//package ua.besf0r.cubauncher.minecraft.auth.microsoft
//
//import kotlinx.serialization.encodeToString
//import kotlinx.serialization.json.Json
//import kotlinx.serialization.json.JsonObject
//import kotlinx.serialization.json.decodeFromJsonElement
//import okhttp3.*
//import okhttp3.Headers.Companion.headersOf
//import okhttp3.MediaType.Companion.toMediaTypeOrNull
//import ua.besf0r.cubauncher.network.HttpRequest
//import java.io.IOException
//import java.util.*
//import ua.besf0r.cubauncher.minecraft.auth.microsoft.XboxLiveAuthResponse as XboxLiveAuthResponse
//
//class MicrosoftAuthenticator(
//    private val httpClient: OkHttpClient,
//    private val listener: AuthListener,
//    var refreshToken: String,
//    private val refresh: Boolean
//) {
//    var expiresIn = 0
//
//    // DO NOT USE MY APPLICATION (CLIENT) ID!!! YOU MUST CREATE YOUR OWN APPLICATION!!!
//    @Throws(IOException::class)
//    fun authenticate(): MinecraftProfile? {
//        val deviceCodeResponse =
//            getDeviceCode("consumers", "9c9cf76c-53b0-430c-b205-8d26101bcdb4", "XboxLive.signin offline_access")
//        listener.onUserCodeReceived(deviceCodeResponse.userCode, deviceCodeResponse.verificationUri)
//        println("Code: " + deviceCodeResponse.userCode)
//        println("Url: " + deviceCodeResponse.verificationUri)
//        val microsoftOAuthCode: OAuthCodeResponse? = if (refresh) {
//            getMicrosoftOAuthCode(null)
//        } else {
//            getMicrosoftOAuthCode(deviceCodeResponse)
//        }
//        refreshToken = microsoftOAuthCode!!.refreshToken
//        val xboxLiveAuthResponse = authenticateWithXboxLive(microsoftOAuthCode)
//        val xstsAuthResponse = obtainXSTSToken(xboxLiveAuthResponse)
//        val minecraftAuthResponse = authenticateWithMinecraft(xstsAuthResponse)
//        expiresIn = minecraftAuthResponse.expiresIn
//        if (!checkGameOwnership(minecraftAuthResponse)) {
//            System.err.println("Account does not own Minecraft")
//            return null
//        }
//        return getProfile(minecraftAuthResponse)
//    }
//
//    @Throws(IOException::class)
//    private fun getDeviceCode(tenant: String, clientId: String, scope: String): DeviceCodeResponse {
//        val url = String.format("https://login.microsoftonline.com/%s/oauth2/v2.0/devicecode", tenant)
//        val requestBody = FormBody.Builder()
//            .add("client_id",clientId)
//            .add("scope",scope)
//            .build()
//
//        HttpRequest(httpClient).use { request ->
//            val json = request.asString(url, requestBody)
//            return Json.decodeFromString<DeviceCodeResponse>(json)
//        }
//    }
//
//    @Throws(IOException::class)
//    private fun getMicrosoftOAuthCode(deviceCodeResponse: DeviceCodeResponse?): OAuthCodeResponse? {
//        val url = "https://login.microsoftonline.com/consumers/oauth2/v2.0/token"
//        val token = if (refresh) refreshToken else deviceCodeResponse!!.deviceCode
//        val requestBody: RequestBody = if (refresh) {
//            FormBody.Builder()
//                .add("grant_type", "refresh_token")
//                .add("client_id", "9c9cf76c-53b0-430c-b205-8d26101bcdb4")
//                .add("refresh_token", token)
//                .build()
//
//        } else {
//            FormBody.Builder()
//                .add("grant_type","urn:ietf:params:oauth:grant-type:device_code")
//                .add("client_id","9c9cf76c-53b0-430c-b205-8d26101bcdb4")
//                .add("device_code",token)
//                .build()
//        }
//        while (true) {
//            HttpRequest(httpClient).use { request ->
//                val jsonObject = Json.decodeFromString<JsonObject>(request.asString(url, requestBody))
//                if (jsonObject.contains("error")) {
//                    val error = jsonObject["error"].toString()
//                    when (error) {
//                        "authorization_pending" -> try {
//                            Thread.sleep(deviceCodeResponse!!.interval * 1000L)
//                        } catch (e: InterruptedException) {
//                            throw RuntimeException(e)
//                        }
//
//                        "authorization_declined" -> return null
//                        "bad_verification_code" -> {
//                            println("Wrong verification/refresh code: $token")
//                            return null
//                        }
//
//                        "expired_token" -> {
//                            println("Device code expired")
//                            return null
//                        }
//                    }
//                } else {
//                    return Json.decodeFromJsonElement<OAuthCodeResponse>(jsonObject)
//                }
//            }
//        }
//    }
//
//    @Throws(IOException::class)
//    private fun authenticateWithXboxLive(oAuthCodeResponse: OAuthCodeResponse?): XboxLiveAuthResponse {
//        val url = "https://user.auth.xboxlive.com/user/authenticate"
//        val authRequest = XboxLiveAuthRequest()
//        val properties = XboxAuthProperties()
//        properties.authMethod = "RPS"
//        properties.siteName = "user.auth.xboxlive.com"
//        properties.rpsTicket = String.format("d=%s", oAuthCodeResponse!!.accessToken)
//        authRequest.properties = properties
//        authRequest.relyingParty = "http://auth.xboxlive.com"
//        authRequest.tokenType = "JWT"
//
//        val requestBody = RequestBody.create( "application/json".toMediaTypeOrNull(),Json.encodeToString(authRequest))
//        HttpRequest(httpClient).use { request ->
//            val json = request.asString(url, requestBody)
//            return Json.decodeFromString<XboxLiveAuthResponse>(json)
//        }
//    }
//
//    @Throws(IOException::class)
//    private fun obtainXSTSToken(authResponse: XboxLiveAuthResponse): XSTSAuthResponse? {
//        val url = "https://xsts.auth.xboxlive.com/xsts/authorize"
//        val tokenRequest = XSTSTokenRequest()
//        val properties = XSTSProperties()
//        properties.sandboxId = "RETAIL"
//        properties.userTokens = listOf(authResponse.token)
//        tokenRequest.properties = properties
//        tokenRequest.tokenType = "JWT"
//        tokenRequest.relyingParty = "rp://api.minecraftservices.com/"
//        val requestBody: RequestBody = RequestBody.create("application/json".toMediaTypeOrNull(),
//            Json.encodeToString(tokenRequest))
//
//        HttpRequest(httpClient).use { request ->
//            val json = request.asString(url, requestBody)
//            return if (request.code() == 401) {
//                val jsonObject = Json.decodeFromString<JsonObject>(
//                    json
//                )
//                val xErr = jsonObject["XErr"].toString()
//                val errorMsg = getXSTSErrorMessage(xErr)
//                println("Error obtaining XSTS token: $errorMsg ($xErr)")
//                null
//            } else {
//                Json.decodeFromString<XSTSAuthResponse>(json)
//            }
//        }
//    }
//
//    @Throws(IOException::class)
//    private fun authenticateWithMinecraft(authResponse: XSTSAuthResponse?): MinecraftAuthResponse {
//        listener.onMinecraftAuth()
//        val url = "https://api.minecraftservices.com/authentication/login_with_xbox"
//        val payload = String.format(
//            "{\"identityToken\": \"XBL3.0 x=%s;%s\"}",
//            authResponse!!.displayClaims.xui[0].uhs,
//            authResponse.token
//        )
//        val requestBody: RequestBody = RequestBody.create("application/json".toMediaTypeOrNull(),payload)
//        HttpRequest(httpClient).use { request ->
//            val json = request.asString(url, requestBody)
//            return Json.decodeFromString<MinecraftAuthResponse>(json)
//        }
//    }
//
//    @Throws(IOException::class)
//    private fun checkGameOwnership(mcResponse: MinecraftAuthResponse): Boolean {
//        listener.onCheckGameOwnership()
//        val url = "https://api.minecraftservices.com/entitlements/mcstore"
//        HttpRequest(httpClient).use { request ->
//            val json: String = request.asString(url, headersOf("Authorization", "Bearer " + mcResponse.accessToken))
//            val response = Json.decodeFromString<GameOwnershipResponse>(json)
//            return response.items.isNotEmpty()
//        }
//    }
//
//    @Throws(IOException::class)
//    private fun getProfile(mcResponse: MinecraftAuthResponse): MinecraftProfile {
//        val url = "https://api.minecraftservices.com/minecraft/profile"
//        HttpRequest(httpClient).use { request ->
//            val json: String = request.asString(url, headersOf("Authorization", "Bearer " + mcResponse.accessToken))
//            val profile = Json.decodeFromString<MinecraftProfile>(json)
//            profile.accessToken = mcResponse.accessToken
//            return profile
//        }
//    }
//
//    companion object {
//        private fun getXSTSErrorMessage(errorCode: String): String {
//            return when (errorCode) {
//                "2148916233" -> "The account doesn't have an Xbox account"
//                "2148916235" -> "The account is from a country where Xbox Live is not available/banned"
//                "2148916236", "2148916237" -> "The account needs adult verification on Xbox page"
//                "2148916238" -> "The account is a child (under 18) and cannot proceed unless the account is added to a Family by an adult"
//                else -> "unknown error"
//            }
//        }
//    }
//}
