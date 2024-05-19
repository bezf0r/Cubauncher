package ua.besf0r.cubauncher.account

import java.io.IOException
import java.util.*

class OfflineAccount(
    override var username: String,
    override var uuid: String = UUID.randomUUID().toString(),
    override var accessToken: String = "-"
) : Account()

