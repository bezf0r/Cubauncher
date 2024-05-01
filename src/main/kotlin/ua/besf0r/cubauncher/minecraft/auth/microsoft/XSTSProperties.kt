package ua.besf0r.cubauncher.minecraft.auth.microsoft

import kotlinx.serialization.SerialName

class XSTSProperties {
    @SerialName("SandboxId")
    lateinit var sandboxId: String

    @SerialName("UserTokens")
    var userTokens: List<String> = listOf()

}
