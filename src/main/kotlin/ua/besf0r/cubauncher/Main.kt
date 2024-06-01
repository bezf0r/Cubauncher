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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import ua.besf0r.cubauncher.account.AccountsManager
import ua.besf0r.cubauncher.instance.InstanceManager
import ua.besf0r.cubauncher.laucnher.SettingsManager
import ua.besf0r.cubauncher.laucnher.logger.LoggerManager.runLogger
import ua.besf0r.cubauncher.minecraft.OperatingSystem
import ua.besf0r.cubauncher.minecraft.fabric.version.FabricVersionManifest
import ua.besf0r.cubauncher.minecraft.version.MinecraftVersion
import ua.besf0r.cubauncher.minecraft.version.VersionList
import ua.besf0r.cubauncher.network.file.FilesManager
import ua.besf0r.cubauncher.window.createMainTitleBar
import ua.besf0r.cubauncher.window.main.BottomColumn
import ua.besf0r.cubauncher.window.main.InstancesGrid
import ua.besf0r.cubauncher.window.main.LeftColumn
import java.nio.file.FileSystems
import java.nio.file.Path


val workDir: Path = FileSystems.getDefault().getPath(
    System.getProperty("user.home"), "Cubauncher")

val assetsDir: Path  = workDir.resolve("assets")
val librariesDir: Path = workDir.resolve("libraries")
val instancesDir: Path = workDir.resolve("instances")
val versionsDir: Path = workDir.resolve("versions")
val javaDir: Path = workDir.resolve("java")
val settingsFile: Path = workDir.resolve("settings.json")

val httpClient = HttpClient(CIO){
    install(HttpTimeout) { requestTimeoutMillis = 30000 }
}

val accountsManager = AccountsManager(workDir)
val instanceManager = InstanceManager(instancesDir)
val settingsManager = SettingsManager(settingsFile)
@Composable
fun App(currentLog: MutableState<String>) {
    Box(modifier = Modifier
        .fillMaxSize()
        .background(settingsManager.settings.currentTheme.fontColor)
    ) {
        LeftColumn()
        BottomColumn(currentLog)
        InstancesGrid()
    }
}

fun main() = application {
    val currentLog = remember { mutableStateOf("") }
    runLogger(currentLog)

    loadMainData()

    val windowState = rememberWindowState(size = DpSize(720.dp, 512.dp))
    Window(
        title = "Cubauncher (1.0-beta)",
        state = windowState,
        resizable = false,
        undecorated = true,
        onCloseRequest = { onDisable() }
    ) {
        App(currentLog)

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
        FilesManager.createDirectories(
            workDir, assetsDir, librariesDir, versionsDir, javaDir
        )
        VersionList.download()

        accountsManager.loadAccounts()
        instanceManager.loadInstances()
        settingsManager.loadSettings()
    }
}




