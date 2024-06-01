package ua.besf0r.cubauncher.account

import kotlinx.serialization.Serializable
import java.util.*

@Serializable
class MicrosoftAccount (
    override var username: String,
    override var uuid: String,
    override var accessToken: String,
    override var refreshToken: String
) : Account()