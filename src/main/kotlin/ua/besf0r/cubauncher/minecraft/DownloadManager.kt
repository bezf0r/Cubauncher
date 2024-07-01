package ua.besf0r.cubauncher.minecraft

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import kotlinx.coroutines.*
import ua.besf0r.cubauncher.instanceManager
import ua.besf0r.cubauncher.minecraft.fabric.FabricInstaller
import ua.besf0r.cubauncher.minecraft.quilt.QuiltInstaller
import ua.besf0r.cubauncher.minecraft.forge.ForgeInstaller
import ua.besf0r.cubauncher.minecraft.liteloader.LiteLoaderInstaller
import ua.besf0r.cubauncher.minecraft.optifine.OptiFineInstaller
import ua.besf0r.cubauncher.network.DownloadListener
import ua.besf0r.cubauncher.window.alert.ProgressAlert
import ua.besf0r.cubauncher.window.instance.create.CreateInstanceData
import ua.besf0r.cubauncher.window.instance.create.ModificationManager

@Composable
fun downloadFiles(
    screenData: MutableState<CreateInstanceData>,
    isDismiss: MutableState<Boolean>
) {
    val instanceName = screenData.value.instanceName
    val selectedVersion = screenData.value.selectedVersion
    val modManagerVersion = screenData.value.modManagerVersion
    val modManager = screenData.value.modManager

    val rememberStage = remember { mutableStateOf<String?>(null) }
    val rememberRate = remember { mutableStateOf(0) }

    val downloadListener =
        object : DownloadListener {
            override fun onStageChanged(stage: String) { rememberStage.value = stage }
            override fun onProgress(value: Long, size: Long) {
                try {
                    val perPercent = size / 100
                    rememberRate.value = (value / perPercent).toInt()
                }catch (_: Exception){}
            }
        }

    val currentJob = CoroutineScope(Job())


    val minecraftDownloader = MinecraftDownloader(currentJob, downloadListener)

    ProgressAlert(rememberStage, rememberRate) {
        isDismiss.value = false
        minecraftDownloader.cancel(
            instanceName ?: selectedVersion!!.id
        )
    }

    currentJob.launch {
        runBlocking {
            if (selectedVersion == null) return@runBlocking

            val correctName = instanceName?: selectedVersion.id
            val instance = instanceManager.createInstance(correctName,selectedVersion.id)

            minecraftDownloader.downloadMinecraft(instance)

            if (modManager == ModificationManager.WITHOUT) {
                instance.mainClass = instance.versionInfo?.mainClass.toString()
            }

            if (modManager == ModificationManager.FORGE) {
                if (modManagerVersion != null)
                ForgeInstaller().download(
                    downloadListener, modManagerVersion, instance
                )
                if (screenData.value.hasOptifine) {
                    val optifineVersion = screenData.value.optifineVersion
                    if (optifineVersion != null)
                        OptiFineInstaller().download(downloadListener,optifineVersion,instance)
                }
            }
            if (modManager == ModificationManager.FABRIC){
                if (modManagerVersion != null)
                FabricInstaller().download(
                    downloadListener,modManagerVersion,instance
                )
            }
            if (modManager == ModificationManager.QUILT){
                if (modManagerVersion != null)
                    QuiltInstaller().download(
                        downloadListener,modManagerVersion,instance
                    )
            }
            if (modManager == ModificationManager.LITE_LOADER){
                if (modManagerVersion != null)
                    LiteLoaderInstaller().download(
                        downloadListener,modManagerVersion,instance
                    )
            }


            instanceManager.update(instance)
            isDismiss.value = false
        }
    }
}


