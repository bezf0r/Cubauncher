package ua.besf0r.kovadlo.window.instance.create

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.rememberWindowState
import ua.besf0r.kovadlo.launcherVersion
import ua.besf0r.kovadlo.minecraft.ModificationManager
import ua.besf0r.kovadlo.minecraft.minecraft.VersionManifest
import ua.besf0r.kovadlo.settingsManager
import ua.besf0r.kovadlo.window.createMainTitleBar

@Composable
fun InstanceWindow(
    onDismissed: () -> Unit,
    onDownload: @Composable (MutableState<CreateInstanceData>) -> Unit
) {
    val windowState = rememberWindowState(size = DpSize(720.dp, 512.dp))

    val screenData = remember{ mutableStateOf(CreateInstanceData()) }
    val isDownloading = remember { mutableStateOf(false) }
    val versionType = if (screenData.value.isRelease) "release" else "snapshot"

    Window(
        icon = painterResource("logo.png"),
        title = "Kovadlo ($launcherVersion) - створити збірку",
        state = windowState,
        resizable = false,
        undecorated = true,
        onCloseRequest = { onDismissed() }
    ) {
        Box(modifier = Modifier.fillMaxSize().background(settingsManager.settings.currentTheme.fontColor))

        createMainTitleBar(windowState){ onDismissed() }

        IconSector()
        ChangeNameSector(screenData)
        ChangeVersionSector(versionType, screenData)
        ChangeModsManagerSector(screenData)
        ButtonsSector(isDownloading) { onDismissed() }

        if (isDownloading.value) { onDownload(screenData) }
    }
}

data class CreateInstanceData(
    val instanceName: String? = null,
    var selectedVersion: VersionManifest.Version? = null,
    var isRelease: Boolean = true,
    val modManager: ModificationManager? = null,
    val hasOptifine: Boolean = false,
    val optifineVersion: String? = null,
    val modManagerVersion: String? = null
)



