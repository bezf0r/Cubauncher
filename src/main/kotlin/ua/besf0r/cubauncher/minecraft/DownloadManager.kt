package ua.besf0r.cubauncher.minecraft

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import kotlinx.coroutines.*
import ua.besf0r.cubauncher.*
import ua.besf0r.cubauncher.instance.CreateInstance
import ua.besf0r.cubauncher.minecraft.forge.ForgeDownloader
import ua.besf0r.cubauncher.minecraft.version.VersionManifest
import ua.besf0r.cubauncher.network.DownloadListener
import ua.besf0r.cubauncher.window.alert.progressAlert

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
                println(stage)
            }

            override fun onProgress(value: Long, size: Long) {
                if (value == 0L) return
                val perRate = size % 100
                if (perRate == 0L) return
                rememberRate.value = ((value % perRate).toInt())
            }
        }

    val currentJob = CoroutineScope(Dispatchers.IO)

    val minecraftDownloader = MinecraftDownloader(
       currentJob, versionsDir, assetsDir, librariesDir, downloadListener
    )

    progressAlert(rememberStage, rememberRate) {
        isDismiss.value = false
        minecraftDownloader.cancel(
            instanceName.value ?: selectedVersion.value!!.id
        )
    }

    currentJob.launch {
        runBlocking {
            val instance = CreateInstance(
                instanceName.value,
                selectedVersion.value?.id
            ).createFiles() ?: return@runBlocking

            minecraftDownloader.downloadMinecraft(instance)

            if (isForge.value) {
                ForgeDownloader().download(downloadListener,
                    modManagerVersion.value, instance)
            }
            instanceManager.update(instance)
            isDismiss.value = false
        }
    }
}
typealias ArgumentsForDownload = (
    instanceName: MutableState<String?>,
    selectedVersion: MutableState<VersionManifest.Version?>,
    isForge: MutableState<Boolean>,
    modManagerVersion: MutableState<String>
) -> Unit


