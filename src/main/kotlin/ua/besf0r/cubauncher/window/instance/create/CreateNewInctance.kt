package ua.besf0r.cubauncher.window.instance.create

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindow
import androidx.compose.ui.window.rememberDialogState
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import ua.besf0r.cubauncher.*
import ua.besf0r.cubauncher.minecraft.MinecraftDownloadListener
import ua.besf0r.cubauncher.minecraft.MinecraftDownloader
import ua.besf0r.cubauncher.minecraft.forge.ForgeDownloader
import ua.besf0r.cubauncher.minecraft.version.VersionManifest
import ua.besf0r.cubauncher.util.IOUtils
import ua.besf0r.cubauncher.window.StatebleWindow
import ua.besf0r.cubauncher.instance.DownloadInstanceFiles
import ua.besf0r.cubauncher.window.main.windowTitleBar

class NewInstance(
    val onDismissRequest: () -> Unit
) {
    private val json = Json { ignoreUnknownKeys = true }

    @Composable
    fun openNewInstanceWindow() {
        val windowState = rememberDialogState(size = DpSize(720.dp, 512.dp))

        val instanceName = remember { mutableStateOf<String?>(null) }
        val selectedVersion = remember { mutableStateOf<VersionManifest.Version?>(null) }

        val manifest: VersionManifest.VersionManifest = json.decodeFromString(
            IOUtils.readUtf8String(versionsDir.resolve("version_manifest_v2.json"))
        )

        //Version type
        val isRelease = remember { mutableStateOf(true) }
        val versionType = if (isRelease.value) "release" else "snapshot"

        //All for mods
        val isWithoutMods = remember { mutableStateOf(true) }
        val isForge = remember { mutableStateOf(false) }
        val isFabric = remember { mutableStateOf(false) }
        val modManagerVersion = remember { mutableStateOf("") }

        //Download indicators
        val onStartDownload = remember { mutableStateOf(false) }
        val progress = remember { mutableStateOf(0) }

        if (progress.value > 0) {
            StatebleWindow(
                progress = Pair(true, progress.value), second = "Зупинити",
                onSecond = {}, subText = "${progress.value}%"
            ).stateWindow()
        }

        if (onStartDownload.value) {
            onStartDownload.value = false
            downloadFiles(instanceName, selectedVersion, progress, isForge, modManagerVersion)
        }

        DialogWindow(
            state = windowState,
            resizable = false,
            undecorated = true,
            onCloseRequest = { onDismissRequest() }
        ) {
            Box(modifier = Modifier.fillMaxSize().background(currentTheme.fontColor))
            this.WindowDraggableArea {
                windowTitleBar(false, close = {
                    onDismissRequest()
                })
            }

            iconButton()
            changeNameSector(instanceName, selectedVersion)

            changeVersionSector(manifest, versionType, selectedVersion, isRelease) {
                selectedVersion.value = it
            }
            changeModsManagerSector(isWithoutMods, isForge, selectedVersion, isFabric){
                modManagerVersion.value = it
            }
            buttonsSector(onStartDownload){ onDismissRequest() }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun downloadFiles(
        instanceName: MutableState<String?>,
        selectedVersion: MutableState<VersionManifest.Version?>,
        progress: MutableState<Int>,
        isForge: MutableState<Boolean>,
        modManagerVersion: MutableState<String>
    ) {
        runBlocking {
            GlobalScope.launch(Dispatchers.IO) {
                val instance = DownloadInstanceFiles(
                    instanceName.value,
                    selectedVersion.value?.id
                ).downloadWindow() ?: return@launch

                MinecraftDownloader(
                    versionsDir, assetsDir, librariesDir, instance,
                    object : MinecraftDownloadListener {
                        override fun onStageChanged(stage: String) {}

                        override fun onProgress(value: Long, size: Long) {
                            if (value == 0L) return
                            val perProcent = size % 100
                            if (perProcent == 0L) return
                            progress.value = ((value % perProcent).toInt())
                        }

                    }).downloadMinecraft(instance.minecraftVersion)

                if (isForge.value) {
                    ForgeDownloader().download(
                        object : MinecraftDownloadListener {
                            override fun onStageChanged(stage: String) {}

                            override fun onProgress(value: Long, size: Long) {
    //                    if (value == 0L) return
    //                    val perProcent = size % 100
    //                    if (perProcent == 0L) return
    //                    progress = ((value % perProcent).toInt())
                            }

                        }, modManagerVersion.value, instance
                    )
                }
                instanceManager.save(instance)
            }
        }
    }
}

@Composable
fun circularCheckbox(
    modifier: Modifier = Modifier,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Box(
        modifier = modifier
            .size(15.dp)
            .background(color = currentTheme.fontColor, shape = CircleShape)
            .clickable { onCheckedChange(!checked) },
        contentAlignment = Alignment.Center
    ) {
        if (checked) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "",
                tint = currentTheme.textColor,
                modifier = Modifier.size(15.dp)
            )
        }
    }
}