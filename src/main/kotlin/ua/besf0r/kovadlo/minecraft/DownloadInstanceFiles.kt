package ua.besf0r.kovadlo.minecraft

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import ua.besf0r.kovadlo.Logger
import ua.besf0r.kovadlo.instanceManager
import ua.besf0r.kovadlo.minecraft.forge.ForgeInstaller
import ua.besf0r.kovadlo.minecraft.minecraft.MinecraftDownloader
import ua.besf0r.kovadlo.minecraft.optifine.OptiFineInstaller
import ua.besf0r.kovadlo.minecraft.minecraft.MinecraftVersion
import ua.besf0r.kovadlo.network.DownloadListener
import ua.besf0r.kovadlo.network.file.IOUtil
import ua.besf0r.kovadlo.versionsDir
import ua.besf0r.kovadlo.window.alert.ProgressAlert
import ua.besf0r.kovadlo.window.instance.create.CreateInstanceData

@Composable
fun downloadFiles(
    screenData: MutableState<CreateInstanceData>,
    isDismiss: MutableState<Boolean>
) {
    val json = Json { ignoreUnknownKeys = true }

    val instanceName = screenData.value.instanceName
    val selectedVersion = screenData.value.selectedVersion
    val modManagerVersion = screenData.value.modManagerVersion
    val modManager = screenData.value.modManager

    val rememberStage = remember { mutableStateOf<String?>(null) }
    val rememberRate = remember { mutableStateOf(0) }

    val currentJob = CoroutineScope(Job())

    val downloadListener = object : DownloadListener {
        override fun onStageChanged(stage: String) { rememberStage.value = stage }
        override fun onProgress(value: Long, size: Long) {
            try {
                val perPercent = size / 100
                rememberRate.value = (value / perPercent).toInt()
            }catch (_: Exception){}
        }
    }
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

            val startTime = minecraftDownloader.downloadMinecraft(instance)

            val versionInfoFile = versionsDir.resolve(selectedVersion.id)
                .resolve("${selectedVersion.id}.json")
            val versionInfo = json.decodeFromString<MinecraftVersion>(IOUtil.readUtf8String(versionInfoFile))

            if (modManagerVersion != null) {
                modManager?.download(downloadListener,modManagerVersion,instance)
            }
            if (modManager == null) {
                instance.mainClass = versionInfo.mainClass.toString()
            }
            if (modManager is ForgeInstaller){
                val optifineVersion = screenData.value.optifineVersion
                if (screenData.value.hasOptifine && optifineVersion != null)
                    OptiFineInstaller().download(downloadListener,optifineVersion,instance)
            }

            val endTime = System.currentTimeMillis()
            val duration = ((endTime - startTime).toInt())
            Logger.publish("Час завантаження - ${(duration.toDouble() / 60000.0)} хв")

            instanceManager.update(instance)
            isDismiss.value = false
        }
    }
}


