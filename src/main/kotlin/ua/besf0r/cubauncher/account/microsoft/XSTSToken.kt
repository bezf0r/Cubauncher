package ua.besf0r.cubauncher.account.microsoft

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class XSTSToken(
    @SerialName("IssueInstant") val issueInstant: String,
    @SerialName("NotAfter") val notAfter: String,
    @SerialName("Token") val token: String,
    @SerialName("DisplayClaims") val displayClaims: Map<String, JsonElement>
)