package ua.besf0r.kovadlo.account

import kotlinx.serialization.Serializable

@Serializable
class MicrosoftAccount (
    override var username: String,
    override var uuid: String,
    override var accessToken: String,
    override var refreshToken: String
) : Account()