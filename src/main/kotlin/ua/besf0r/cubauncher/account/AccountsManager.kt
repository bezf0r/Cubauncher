package ua.besf0r.cubauncher.account

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ua.besf0r.cubauncher.accountsManager
import ua.besf0r.cubauncher.util.FileUtil
import ua.besf0r.cubauncher.util.IOUtils
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

class AccountsManager(workDir: Path) {
    private val accountsFile: Path = workDir.resolve("accounts.json")
    private val accounts: MutableMap<String?, Account>

    init {
        try {
            FileUtil.createFileIfNotExists(accountsFile)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
        accounts = LinkedHashMap()
    }

    private fun canCreateAccount(username: String?): Boolean {
        return !accounts.containsKey(username)
    }

    @Throws(IOException::class)
    fun loadAccounts() {
        val accounts = Json.decodeFromString<Map<String, Account>>(
            IOUtils.readUtf8String(
                accountsFile
            )
        )
        this.accounts.putAll(accounts)
    }

    fun saveAccount(account: Account): Boolean {
        if (!canCreateAccount(account.username)) {
            return false
        }
        accounts[account.username] = account
        return try {
            save()
            true
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    fun deleteAccount(nickname: String?): Boolean {
        if (accounts.containsKey(nickname)) {
            accounts.remove(nickname)
            try {
                save()
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
            return true
        }
        return false
    }

    fun selectAccount(account: Account) {
        for ((_, value) in accounts) {
            value.isSelected = false
        }
        account.isSelected = true
    }

    @Throws(IOException::class)
    fun save() {
        val json = Json.encodeToString(accounts)
        Files.write(accountsFile, json.toByteArray(StandardCharsets.UTF_8))
    }

    fun getAccounts(): List<Account> {
        return ArrayList(accounts.values)
    }

    val accountsMap: Map<String?, Account>
        get() = accounts

    @Throws(IOException::class)
    fun removeAccount(account: Account) {
        accounts.remove(account.username)
        save()
    }

    companion object {
        val currentAccount: Account?
            get() {
                val accountsMap: Map<String?, Account> = accountsManager.accountsMap
                for ((_, account) in accountsMap) {
                    if (account.isSelected) {
                        return account
                    }
                }
                return null
            }
    }
}
