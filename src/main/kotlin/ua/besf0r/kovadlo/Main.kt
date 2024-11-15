package ua.besf0r.kovadlo

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import io.ktor.client.*
import io.ktor.client.engine.java.*
import io.ktor.client.plugins.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import ua.besf0r.kovadlo.account.AccountsManager
import ua.besf0r.kovadlo.instance.InstanceManager
import ua.besf0r.kovadlo.network.file.FileManager
import ua.besf0r.kovadlo.network.file.IOUtil
import ua.besf0r.kovadlo.network.file.UpdaterManager
import ua.besf0r.kovadlo.settings.SettingsManager
import ua.besf0r.kovadlo.window.createMainTitleBar
import ua.besf0r.kovadlo.window.main.BottomColumn
import ua.besf0r.kovadlo.window.main.InstancesGrid
import ua.besf0r.kovadlo.window.main.LeftColumn
import java.nio.file.Path

val workDir: Path = IOUtil.byGetProtectionDomain(Logger::class.java).parent
const val launcherVersion = "1.1.2-beta"

val assetsDir: Path  = workDir.resolve("assets")
val librariesDir: Path = workDir.resolve("libraries")
val instancesDir: Path = workDir.resolve("instances")
val versionsDir: Path = workDir.resolve("versions")
val javaDir: Path = workDir.resolve("java")
val settingsFile: Path = workDir.resolve("settings.json")

val httpClient = HttpClient(Java){
    install(HttpTimeout) { requestTimeoutMillis = 3000000 }
}

val accountsManager = AccountsManager(workDir)
val instanceManager = InstanceManager(instancesDir)
val settingsManager = SettingsManager(settingsFile)

@Composable
@Preview
fun App() {
    Box(modifier = Modifier
        .fillMaxSize()
        .background(settingsManager.settings.currentTheme.fontColor)
    ) {
        LeftColumn()
        BottomColumn()
        InstancesGrid()
    }
}

fun main() = application {
    loadMainData()

    val windowState = rememberWindowState(size = DpSize(720.dp, 512.dp))
    Window(
        icon = painterResource("logo.png"),
        title = "Kovadlo ($launcherVersion)",
        state = windowState,
        resizable = false,
        undecorated = true,
        onCloseRequest = { onDisable() }
    ) {
        App()

        createMainTitleBar(windowState){ onDisable() }
    }
}

private fun ApplicationScope.onDisable() {
    settingsManager.update()
    accountsManager.save()
    exitApplication()
}

private fun loadMainData() = runBlocking {
    withContext(Dispatchers.IO) {
        UpdaterManager.downloadUpdater()
        UpdaterManager.checkForUpdates()

        FileManager.createDirectories(
            workDir, assetsDir, librariesDir, versionsDir, javaDir
        )

        accountsManager.loadAccounts()
        instanceManager.loadInstances()
        settingsManager.loadSettings()
    }
}




