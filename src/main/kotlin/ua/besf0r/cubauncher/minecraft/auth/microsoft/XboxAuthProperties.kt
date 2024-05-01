package ua.besf0r.cubauncher.minecraft.auth.microsoft

import kotlinx.serialization.SerialName

class XboxAuthProperties {
    @SerialName("AuthMethod")
    lateinit var authMethod: String

    @SerialName("SiteName")
    lateinit var siteName: String

    @SerialName("RpsTicket")
    lateinit var rpsTicket: String
}
