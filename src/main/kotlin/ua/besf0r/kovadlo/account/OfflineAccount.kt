package ua.besf0r.kovadlo.account

import kotlinx.serialization.Serializable
import java.util.*

@Serializable
class OfflineAccount(
    override var username: String,
    override var uuid: String = UUID.randomUUID().toString(),
    override var accessToken: String = "-",
    override var refreshToken: String = "-"
) : Account()

