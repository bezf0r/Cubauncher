package ua.besf0r.cubauncher.window.instance.create

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
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.rememberWindowState
import kotlinx.serialization.json.Json
import ua.besf0r.cubauncher.minecraft.ArgumentsForDownload
import ua.besf0r.cubauncher.minecraft.version.VersionManifest
import ua.besf0r.cubauncher.network.file.IOUtil
import ua.besf0r.cubauncher.settingsManager
import ua.besf0r.cubauncher.versionsDir
import ua.besf0r.cubauncher.window.createMainTitleBar

@Composable
fun createNewInstanceWindow(
    onDismissed: () -> Unit,
    onDownload: @Composable ArgumentsForDownload
) {
    val windowState = rememberWindowState(size = DpSize(720.dp, 512.dp))

    Window(
        state = windowState,
        resizable = false,
        undecorated = true,
        onCloseRequest = { onDismissed() }
    ) {
        Box(modifier = Modifier.fillMaxSize().background(
            settingsManager.settings.currentTheme.fontColor
        ))

        val json = Json { ignoreUnknownKeys = true }
        val manifest = json.decodeFromString<VersionManifest.VersionManifest>(
            IOUtil.readUtf8String(versionsDir.resolve("version_manifest_v2.json"))
        )

        val instanceName = remember { mutableStateOf<String?>(null) }
        val selectedVersion = remember { mutableStateOf<VersionManifest.Version?>(null) }
        val isRelease = remember { mutableStateOf(true) }
        val isWithoutMods = remember { mutableStateOf(true) }
        val isForge = remember { mutableStateOf(false) }
        val isFabric = remember { mutableStateOf(false) }
        val modManagerVersion = remember { mutableStateOf("") }
        val isDownloading = remember { mutableStateOf(false) }

        createMainTitleBar(windowState){ onDismissed() }

        iconButton()
        changeNameSector(instanceName, selectedVersion)

        changeVersionSector(
            manifest, versionType(isRelease), selectedVersion, isRelease
        ) {
            selectedVersion.value = it
        }

        changeModsManagerSector(
            isWithoutMods, isForge, selectedVersion, isFabric
        ) {
            modManagerVersion.value = it
        }

        if (isDownloading.value) {
            onDownload(
                instanceName, selectedVersion, isForge, modManagerVersion
            )
        }
        buttonsSector(isDownloading){ onDismissed() }
    }
}

private fun versionType(isRelease: MutableState<Boolean>) =
    if (isRelease.value) "release" else "snapshot"



