package ua.besf0r.cubauncher.window.instance.create

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindow
import androidx.compose.ui.window.rememberDialogState
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import ua.besf0r.cubauncher.*
import ua.besf0r.cubauncher.instance.CreateInstanceFiles
import ua.besf0r.cubauncher.minecraft.MinecraftDownloader
import ua.besf0r.cubauncher.minecraft.forge.ForgeDownloader
import ua.besf0r.cubauncher.minecraft.version.VersionManifest
import ua.besf0r.cubauncher.network.DownloadListener
import ua.besf0r.cubauncher.util.IOUtils
import ua.besf0r.cubauncher.window.alert.progressAlert
import ua.besf0r.cubauncher.window.main.windowTitleBar

@Composable
fun createNewInstanceWindow(onDismissed: () -> Unit,
    onDownload: @Composable (
        instanceName: MutableState<String?>,
        selectedVersion: MutableState<VersionManifest.Version?>,
        isForge: MutableState<Boolean>,
        modManagerVersion: MutableState<String>
    ) -> Unit) {
    val windowState = rememberDialogState(size = DpSize(720.dp, 512.dp))

    DialogWindow(
        state = windowState,
        resizable = false,
        undecorated = true,
        onCloseRequest = { onDismissed() }
    ) {

        val json = Json { ignoreUnknownKeys = true }
        val manifest = json.decodeFromString<VersionManifest.VersionManifest>(
            IOUtils.readUtf8String(versionsDir.resolve("version_manifest_v2.json"))
        )

        val instanceName = remember { mutableStateOf<String?>(null) }
        val selectedVersion = remember { mutableStateOf<VersionManifest.Version?>(null) }
        val isRelease = remember { mutableStateOf(true) }
        val isWithoutMods = remember { mutableStateOf(true) }
        val isForge = remember { mutableStateOf(false) }
        val isFabric = remember { mutableStateOf(false) }
        val modManagerVersion = remember { mutableStateOf("") }
        val isDownloading = remember { mutableStateOf(false) }

        Box(modifier = Modifier.fillMaxSize().background(currentTheme.fontColor))
        this.WindowDraggableArea {
            windowTitleBar(false, close = { onDismissed() })
        }

        iconButton()
        changeNameSector(instanceName, selectedVersion)

        changeVersionSector(manifest, if (isRelease.value) "release" else "snapshot",
            selectedVersion, isRelease) {
            selectedVersion.value = it
        }
        changeModsManagerSector(isWithoutMods, isForge, selectedVersion, isFabric) {
            modManagerVersion.value = it
        }
        if (isDownloading.value) {
            onDownload(
                instanceName,
                selectedVersion,
                isForge,
                modManagerVersion
            )
        }
        buttonsSector(isDownloading){ onDismissed() }

    }
}

@OptIn(DelicateCoroutinesApi::class)
@Composable
fun downloadFiles(
    instanceName: MutableState<String?>,
    selectedVersion: MutableState<VersionManifest.Version?>,
    isForge: MutableState<Boolean>,
    modManagerVersion: MutableState<String>,
    isDismiss: MutableState<Boolean>
) {
    val rememberStage = remember { mutableStateOf<String?>(null) }
    val rememberRate = remember { mutableStateOf(0) }

    val downloadListener =
        object : DownloadListener {
            override fun onStageChanged(stage: String) {
                rememberStage.value = stage
            }

            override fun onProgress(value: Long, size: Long) {
                if (value == 0L) return
                val perRate = size % 100
                if (perRate == 0L) return
                rememberRate.value = ((value % perRate).toInt())
            }
        }

    progressAlert(rememberStage, rememberRate) {
        isDismiss.value = false
    }

    runBlocking {
        GlobalScope.launch(Dispatchers.IO) {
            val instance = CreateInstanceFiles(
                instanceName.value,
                selectedVersion.value?.id
            ).createFiles() ?: return@launch

            MinecraftDownloader(
                versionsDir, assetsDir, librariesDir,
                instance, downloadListener
            ).downloadMinecraft(instance.minecraftVersion)

            if (isForge.value) {
                ForgeDownloader().download(
                    downloadListener, modManagerVersion.value, instance
                )
            }
            instanceManager.save(instance)
            isDismiss.value = false
        }
    }
}

