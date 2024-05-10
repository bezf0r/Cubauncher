package ua.besf0r.cubauncher.minecraft.auth.microsoft

import kotlinx.serialization.SerialName

class XboxLiveAuthResponse {
    @SerialName("IssueInstant")
    lateinit var issueInstant: String

    @SerialName("NotAfter")
    lateinit var notAfter: String

    @SerialName("Token")
    lateinit var token: String

    @SerialName("DisplayClaims")
    lateinit var displayClaims: DisplayClaims

    class UserHash {
        lateinit var uhs: String
    }

    class DisplayClaims {
        lateinit var xui: List<UserHash>

    }
}