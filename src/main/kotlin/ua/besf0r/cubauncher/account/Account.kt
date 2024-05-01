package ua.besf0r.cubauncher.account

import ua.besf0r.cubauncher.minecraft.auth.microsoft.AuthException
import java.io.IOException
import java.util.*

abstract class Account {
    lateinit var username: String
    lateinit var uuid: UUID
    lateinit var accessToken: String
    var isSelected: Boolean = false

    @Throws(IOException::class, AuthException::class)
    open fun authenticate() {
    }
}


