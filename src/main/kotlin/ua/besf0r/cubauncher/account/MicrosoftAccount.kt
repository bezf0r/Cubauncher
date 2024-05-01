//package ua.besf0r.cubauncher.accounts
//
//import ua.besf0r.cubauncher.accountsManager
//import ua.besf0r.cubauncher.httpClient
//import ua.besf0r.cubauncher.minecraft.auth.microsoft.AuthException
//import ua.besf0r.cubauncher.minecraft.auth.microsoft.AuthListener
//import ua.besf0r.cubauncher.minecraft.auth.microsoft.MicrosoftAuthenticator
//import java.io.IOException
//import java.time.OffsetDateTime
//import java.time.temporal.ChronoUnit
//import java.util.*
//
//class MicrosoftAccount : Account() {
//    private lateinit var refreshToken: String
//    private var expiresIn = 0
//    private var loggedInAt: OffsetDateTime? = null
//    private fun needToRefresh(): Boolean {
//        val between = ChronoUnit.SECONDS.between(loggedInAt, OffsetDateTime.now())
//        return between >= expiresIn
//    }
//
//    @Throws(IOException::class, AuthException::class)
//    override fun authenticate() {
//        if (needToRefresh()) {
//            val authListener: AuthListener = object : AuthListener {
//                override fun onUserCodeReceived(userCode: String, verificationUri: String) {}
//
//                override fun onMinecraftAuth() {}
//                override fun onCheckGameOwnership() {}
//                override fun onGettingSkin() {}
//                override fun onFinish() {}
//            }
//            val authenticator = MicrosoftAuthenticator(
//                httpClient,
//                authListener, refreshToken, true
//            )
//            val profile = authenticator.authenticate()
//            accessToken = profile!!.accessToken
//            uuid = UUID.fromString(
//                profile.id.replaceFirst(
//                    "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)".toRegex(),
//                    "$1-$2-$3-$4-$5"
//                )
//            )
//            val oldUsername = username
//            username = profile.name
//            refreshToken = authenticator.refreshToken
//            loggedInAt = OffsetDateTime.now()
//            expiresIn = authenticator.expiresIn
//            accountsManager.save()
//        }
//    }
//}
