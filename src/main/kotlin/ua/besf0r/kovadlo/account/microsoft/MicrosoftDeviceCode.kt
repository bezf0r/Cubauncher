package ua.besf0r.kovadlo.account.microsoft

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MicrosoftDeviceCode(
    @SerialName("device_code")
    val deviceCode: String,
    @SerialName("user_code")
    val userCode: String,
    @SerialName("verification_uri")
    val verificationUri: String,
    @SerialName("expires_in")
    val expiresIn: Int,
    @SerialName("interval")
    val interval: Int,
    @SerialName("message")
    val message: String,
)
