package ua.besf0r.kovadlo

import io.ktor.client.*
import io.ktor.client.engine.java.*
import io.ktor.client.plugins.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.kodein.di.*
import ua.besf0r.kovadlo.account.AccountDI
import ua.besf0r.kovadlo.account.AccountsManager
import ua.besf0r.kovadlo.instance.InstanceManager
import ua.besf0r.kovadlo.localization.LocalizationManager
import ua.besf0r.kovadlo.minecraft.InitialiseDI
import ua.besf0r.kovadlo.network.DownloadService
import ua.besf0r.kovadlo.settings.SettingsManager
import ua.besf0r.kovadlo.settings.directories.WorkingDirs
import java.nio.file.Path

object ApplicationDI {
    fun createDI(workDir: Path) = DI {
        bind<WorkingDirs>() with eagerSingleton { WorkingDirs(workDir) }

        bind<SettingsManager>() with eagerSingleton { SettingsManager(instance<WorkingDirs>().settingsFile) }
        bind<AccountsManager>() with eagerSingleton { AccountsManager(workDir) }
        bind<InstanceManager>() with eagerSingleton { InstanceManager(instance(), instance(), instance()) }
        bind<CoroutineScope>() with eagerSingleton { CoroutineScope(SupervisorJob() + Dispatchers.IO) }

        bind<Logger>() with eagerSingleton { Logger(instance()) }

        import(InitialiseDI.versionsModule())
        import(AccountDI.accountsModule())

        bind<DownloadService>() with singleton { DownloadService(instance(), instance(), instance()) }
        bind<HttpClient>() with singleton {
            HttpClient(Java) { install(HttpTimeout) { requestTimeoutMillis = 3_000_000 } }
        }
    }
}

fun DI.settingsManager(): SettingsManager = direct.instance()
fun DI.accountsManager(): AccountsManager = direct.instance()
fun DI.instanceManager(): InstanceManager = direct.instance()
fun DI.workingDirs(): WorkingDirs = direct.instance()
fun DI.httpClient(): HttpClient = direct.instance()
fun DI.coroutine(): CoroutineScope = direct.instance()
fun DI.logger(): Logger = direct.instance()