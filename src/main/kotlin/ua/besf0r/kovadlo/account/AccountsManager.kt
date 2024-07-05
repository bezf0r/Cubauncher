package ua.besf0r.kovadlo.account

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.skiko.MainUIDispatcher
import ua.besf0r.kovadlo.network.file.FileManager.createFileIfNotExists
import ua.besf0r.kovadlo.network.file.IOUtil
import java.io.IOException
import java.nio.file.Path

class AccountsManager(workDir: Path) {
    private val accountsFile: Path = workDir.resolve("accounts.json")
    var accounts: MutableList<Account> = mutableListOf()

    private val json = Json {
        serializersModule = Account.accountModule
        ignoreUnknownKeys = true
    }
    @Throws(IOException::class)
    fun loadAccounts() {
        accountsFile.createFileIfNotExists()
        try {
            val loadedAccounts = Json.decodeFromString<List<Account>>(
                IOUtil.readUtf8String(accountsFile)
            )
            accounts.addAll(loadedAccounts)
        }catch (_: Exception){ }
    }

    fun createAccount(account: Account) {
        if (accounts.getByName(account.username) != null) return

        accounts.add(account)
        AccountsUpdateEvent.publish(accounts)
        save()
    }

    fun deleteAccount(nickname: String) {
        if (accounts.getByName(nickname) == null) return

        accounts.remove(accounts.getByName(nickname))
        AccountsUpdateEvent.publish(accounts)
        save()
    }

    @Throws(IOException::class)
    fun save() {
        try {
            val json = json.encodeToString(accounts)
            IOUtil.writeUtf8String(accountsFile, json)
        } catch (e: IOException) {
            throw Exception("Failed to save accounts",e)
        }
    }
}
fun MutableList<Account>.getByName(nickname: String): Account? {
    return this.find { it.username == nickname }
}
object AccountsUpdateEvent {
    private val _events = MutableSharedFlow<List<Account>>()
    private val events = _events.asSharedFlow()

    fun publish(event: List<Account>) = CoroutineScope(Dispatchers.IO).launch {
        _events.emit(event)
    }


    fun subscribe(onEvent: (List<Account>) -> Unit) = CoroutineScope(Dispatchers.IO).launch {
        events.filterIsInstance<List<Account>>()
            .collectLatest { event ->
                coroutineContext.ensureActive()
                withContext(MainUIDispatcher){ onEvent(event) }
            }
    }
}
