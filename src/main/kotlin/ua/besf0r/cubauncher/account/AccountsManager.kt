package ua.besf0r.cubauncher.account

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ua.besf0r.cubauncher.network.file.FilesManager.createFileIfNotExists
import ua.besf0r.cubauncher.network.file.IOUtil
import java.io.IOException
import java.nio.file.Path

class AccountsManager(workDir: Path) {
    private val accountsFile: Path = workDir.resolve("accounts.json")
    val accounts: HashMap<String?, Account>

    init {
        try {
            accountsFile.createFileIfNotExists()
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
        accounts = LinkedHashMap()
    }

    @Throws(IOException::class)
    fun loadAccounts() {
        val loadedAccounts = Json.decodeFromString<Map<String, Account>>(
            IOUtil.readUtf8String(accountsFile)
        )
        accounts.putAll(loadedAccounts)
    }

    fun createAccount(account: Account) {
        if (accounts.containsKey(account.username)) return

        accounts[account.username] = account
        return try {
            save()
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    fun deleteAccount(nickname: String?) {
        if (accounts.containsKey(nickname)) {
            accounts.remove(nickname)
            try {
                save()
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
        }
    }

    @Throws(IOException::class)
    fun save() {
        val json = Json.encodeToString(accounts)
        IOUtil.writeUtf8String(accountsFile, json)
    }
}
