package ua.besf0r.cubauncher.minecraft.auth.microsoft

import kotlinx.serialization.SerialName

class XSTSTokenRequest {
    @SerialName("Properties")
    lateinit var properties: XSTSProperties

    @SerialName("RelyingParty")
    lateinit var relyingParty: String

    @SerialName("TokenType")
    lateinit var tokenType: String

}
