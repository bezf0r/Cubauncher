package ua.besf0r.kovadlo

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.kodein.di.DI
import ua.besf0r.kovadlo.ApplicationDI.createDI
import ua.besf0r.kovadlo.localization.LocalizationManager
import ua.besf0r.kovadlo.localization.Strings
import ua.besf0r.kovadlo.network.file.FileManager
import ua.besf0r.kovadlo.network.file.IOUtil
import ua.besf0r.kovadlo.network.file.UpdaterManager
import ua.besf0r.kovadlo.window.createMainTitleBar
import ua.besf0r.kovadlo.window.main.BottomColumn
import ua.besf0r.kovadlo.window.main.InstancesGrid
import ua.besf0r.kovadlo.window.main.LeftColumn
import java.nio.file.Path

const val launcherVersion = "1.1.2-beta"

val LocalAppStrings = staticCompositionLocalOf<LocalizationManager> { error("No localization provided") }
val AppStrings: Strings
    @Composable get() = LocalAppStrings.current.strings.value

@Composable
@Preview
fun App(di: DI) {
    val settingsManager = di.settingsManager()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(settingsManager.settings.currentTheme.fontColor)
    ) {
        LeftColumn(di)
        BottomColumn(di)
        InstancesGrid(di)
    }

}

fun main() = application {
    val workDir: Path = IOUtil.byGetProtectionDomain(Logger::class.java).parent
    val di = createDI(workDir)

    loadMainData(di)

    val localization = LocalizationManager(di.logger(),di.workingDirs().localizationDir)

    val windowState = rememberWindowState(size = DpSize(720.dp, 512.dp))
    Window(
        icon = painterResource("logo.png"),
        title = "Kovadlo ($launcherVersion)",
        state = windowState,
        resizable = false,
        undecorated = true,
        onCloseRequest = { onDisable(di) }
    ) {
        CompositionLocalProvider(LocalAppStrings provides localization) {
            App(di)
            createMainTitleBar(windowState) { onDisable(di) }
        }
    }
}

private fun ApplicationScope.onDisable(di: DI) {
    di.settingsManager().update()
    di.accountsManager().save()
    exitApplication()
}

private fun loadMainData(di: DI) = runBlocking {
    withContext(Dispatchers.IO) {
        val config = di.workingDirs()
        val updaterManager = UpdaterManager(di)

        updaterManager.downloadUpdater()
        updaterManager.checkForUpdates()

        FileManager.createDirectories(
            config.workDir, config.assetsDir, config.librariesDir,
            config.versionsDir, config.javaDir, config.localizationDir
        )

    }
    di.accountsManager().loadAccounts()
    di.instanceManager().loadInstances()
    di.settingsManager().loadSettings()
}