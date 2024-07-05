package ua.besf0r.kovadlo.minecraft.optifine

import kotlinx.coroutines.runBlocking
import ua.besf0r.kovadlo.instance.Instance
import ua.besf0r.kovadlo.instanceManager
import ua.besf0r.kovadlo.network.DownloadListener
import ua.besf0r.kovadlo.network.DownloadManager

class OptiFineInstaller {
    private val installer = "https://optifine.net/download?f="

    @Throws(Exception::class)
    fun download(
        progress: DownloadListener,
        optiFineVersion: String,
        instance: Instance
    ) = runBlocking {
        progress.onStageChanged("Завантажуємо $optiFineVersion")

        val optifineUrl = installer + optiFineVersion
        val optifinePath = instanceManager.getMinecraftDir(instance)
            .resolve("mods").resolve(optiFineVersion)

        try {
            DownloadManager(
                fileUrl = optifineUrl,
                saveAs = optifinePath
            ).downloadFile { value, size -> progress.onProgress(value, size) }
        }finally { }
    }
}