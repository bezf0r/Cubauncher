package ua.besf0r.kovadlo.account.microsoft

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

@Serializable
data class XboxLiveToken(
    @SerialName("IssueInstant") val issueInstant: String,
    @SerialName("NotAfter") val notAfter: String,
    @SerialName("Token") val token: String,
    @SerialName("DisplayClaims") val displayClaims: Map<String, JsonElement>
) {
    val userHash: String
        get() = (displayClaims["xui"]?.jsonArray?.get(0) as? JsonObject)
            ?.get("uhs")
            ?.jsonPrimitive
            ?.content
            ?: throw IllegalArgumentException("Invalid DisplayClaims structure")
}