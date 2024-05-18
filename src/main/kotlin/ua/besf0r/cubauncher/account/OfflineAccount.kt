package ua.besf0r.cubauncher.account

import java.io.IOException
import java.util.*

class OfflineAccount(override var username: String) : Account() {
    @Throws(IOException::class)
    override fun authenticate() {
        uuid = UUID.randomUUID()
        accessToken = "-"
    }
}
