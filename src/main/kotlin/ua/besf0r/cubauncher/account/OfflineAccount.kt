package ua.besf0r.cubauncher.account

import java.io.IOException
import java.util.*

class OfflineAccount(username: String) : Account() {
    init {
        this.username = username
    }

    @Throws(IOException::class)
    override fun authenticate() {
        uuid = UUID.randomUUID()
        accessToken = "-"
    }
}
