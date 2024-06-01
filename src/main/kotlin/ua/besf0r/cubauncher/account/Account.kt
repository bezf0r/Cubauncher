package ua.besf0r.cubauncher.account

import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

@Serializable
sealed class Account {
    abstract var username: String
    abstract var uuid: String
    abstract var accessToken: String
    abstract var refreshToken: String

    companion object {
        val accountModule = SerializersModule {
            polymorphic(Account::class) {
                subclass(OfflineAccount::class, OfflineAccount.serializer())
                subclass(MicrosoftAccount::class, MicrosoftAccount.serializer())
            }
        }
    }
}


