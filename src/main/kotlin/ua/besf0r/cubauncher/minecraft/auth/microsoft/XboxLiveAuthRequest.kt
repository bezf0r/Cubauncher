package ua.besf0r.cubauncher.minecraft.auth.microsoft

import kotlinx.serialization.SerialName

class XboxLiveAuthRequest {
    @SerialName("Properties")
    lateinit var properties: XboxAuthProperties

    @SerialName("RelyingParty")
    lateinit var relyingParty: String

    @SerialName("TokenType")
    lateinit var tokenType: String
}
