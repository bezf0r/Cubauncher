package ua.besf0r.cubauncher.minecraft.auth.microsoft

import kotlinx.serialization.SerialName

class DeviceCodeResponse {
    @SerialName("device_code")
    lateinit var deviceCode: String

    @SerialName("user_code")
    lateinit var userCode: String

    @SerialName("verification_uri")
    lateinit var verificationUri: String

    @SerialName("expires_in")
    var expiresIn = 0

    @SerialName("interval")
    var interval = 0

    @SerialName("message")
    lateinit var message: String

}
