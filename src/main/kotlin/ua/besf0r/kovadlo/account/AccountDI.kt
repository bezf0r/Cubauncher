package ua.besf0r.kovadlo.account

import io.ktor.client.*
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton
import ua.besf0r.kovadlo.Logger
import ua.besf0r.kovadlo.account.microsoft.MicrosoftOAuthUtils

object AccountDI {
    fun accountsModule() = DI.Module("authOfAccount"){
        bind<MicrosoftOAuthUtils>() with singleton {
            MicrosoftOAuthUtils(instance(),instance(), instance())
        }
    }
}