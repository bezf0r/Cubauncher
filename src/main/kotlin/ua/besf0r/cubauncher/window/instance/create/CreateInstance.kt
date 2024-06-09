package ua.besf0r.cubauncher.window.instance.create

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
import kotlinx.serialization.json.Json
import ua.besf0r.cubauncher.minecraft.version.VersionManifest
import ua.besf0r.cubauncher.network.file.IOUtil
import ua.besf0r.cubauncher.settingsManager
import ua.besf0r.cubauncher.versionsDir
import ua.besf0r.cubauncher.window.createMainTitleBar

@Composable
fun CreateInstanceWindow(
    onDismissed: () -> Unit,
    onDownload: @Composable (MutableState<CreateInstanceData>) -> Unit
) {
    val windowState = rememberWindowState(size = DpSize(720.dp, 512.dp))

    val json = Json { ignoreUnknownKeys = true }
    val manifest = json.decodeFromString<VersionManifest.VersionManifest>(
        IOUtil.readUtf8String(versionsDir.resolve("version_manifest_v2.json"))
    )

    val screenData = remember{ mutableStateOf(CreateInstanceData()) }
    val isDownloading = remember { mutableStateOf(false) }

    Window(
        icon = painterResource("logo.png"),
        title = "Cubauncher (1.1-beta) - створити збірку",
        state = windowState,
        resizable = false,
        undecorated = true,
        onCloseRequest = { onDismissed() }
    ) {
        Box(modifier = Modifier.fillMaxSize().background(
            settingsManager.settings.currentTheme.fontColor
        ))

        createMainTitleBar(windowState){ onDismissed() }

        IconSector()
        ChangeNameSector(screenData)
        ChangeVersionSector(manifest, versionType(screenData), screenData)
        ChangeModsManagerSector(screenData)
        ButtonsSector(isDownloading){ onDismissed() }

        if (isDownloading.value) { onDownload(screenData) }
    }
}

data class CreateInstanceData(
    val instanceName: String? = null,
    var selectedVersion: VersionManifest.Version? = null,
    var isRelease: Boolean = true,
    val modManager: ModificationManager = ModificationManager.WITHOUT,
    val modManagerVersion: String = ""
)
enum class ModificationManager{ WITHOUT, FORGE, FABRIC }

private fun versionType(screenData: MutableState<CreateInstanceData>) =
    if (screenData.value.isRelease) "release" else "snapshot"



