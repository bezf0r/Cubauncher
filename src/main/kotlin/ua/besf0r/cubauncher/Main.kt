package ua.besf0r.cubauncher

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.events.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import ua.besf0r.cubauncher.laucnher.logger.LoggerManager.runLogger
import ua.besf0r.cubauncher.account.AccountsManager
import ua.besf0r.cubauncher.instance.InstanceManager
import ua.besf0r.cubauncher.laucnher.SettingsManager
import ua.besf0r.cubauncher.minecraft.version.VersionList
import ua.besf0r.cubauncher.network.file.FilesManager
import ua.besf0r.cubauncher.network.file.FilesManager.createFileIfNotExists
import ua.besf0r.cubauncher.window.createMainTitleBar
import ua.besf0r.cubauncher.window.main.bottomColumn
import ua.besf0r.cubauncher.window.main.instancesGrid
import ua.besf0r.cubauncher.window.main.leftColumn
import ua.besf0r.cubauncher.window.theme.UiTheme
import java.nio.file.FileSystems

@Composable
 fun App(currentLog: MutableState<String>) {
    Box(modifier = Modifier
        .fillMaxSize()
        .background(settingsManager.settings.currentTheme.fontColor)
    ) {

        leftColumn()
        bottomColumn(currentLog)
        instancesGrid()
    }
}

val workDir = FileSystems.getDefault().getPath(System.getProperty("user.home"),
    "Cubauncher")

val assetsDir  = workDir.resolve("assets")
val librariesDir = workDir.resolve("libraries")
val instancesDir = workDir.resolve("instances")
val versionsDir = workDir.resolve("versions")
val settingsFile = workDir.resolve("settings.json")

val httpClient = HttpClient(CIO){
    install(HttpTimeout) {
        requestTimeoutMillis = 30000
    }
}

val accountsManager = AccountsManager(workDir)
val instanceManager = InstanceManager(instancesDir)
val settingsManager = SettingsManager(settingsFile)

var applicationScope: ApplicationScope? = null

fun main() = application {
    applicationScope = this

    runBlocking {
        withContext(Dispatchers.IO) {
            FilesManager.createDirectories(workDir, assetsDir, librariesDir, instancesDir, versionsDir)
            VersionList.download()
        }
    }

    settingsManager.loadSettings()

    val currentLog = remember { mutableStateOf("") }
    runLogger(currentLog)

    val windowState = rememberWindowState(size = DpSize(720.dp, 512.dp))
    Window(
        state = windowState,
        resizable = false,
        undecorated = true,
        onCloseRequest = { settingsManager.update() }
    ) {
        App(currentLog)
        createMainTitleBar(windowState)
    }
}


