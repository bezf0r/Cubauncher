package ua.besf0r.kovadlo.minecraft.optifine

import io.ktor.client.*
import kotlinx.coroutines.runBlocking
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import ua.besf0r.kovadlo.httpClient
import ua.besf0r.kovadlo.instance.Instance
import ua.besf0r.kovadlo.instanceManager
import ua.besf0r.kovadlo.logger
import ua.besf0r.kovadlo.network.DownloadListener
import ua.besf0r.kovadlo.network.DownloadManager

class OptiFineInstaller(
    private val di: DI
) {
    private val installer = "https://optifine.net/download?f="

    @Throws(Exception::class)
    fun download(
        progress: DownloadListener,
        optiFineVersion: String,
        instance: Instance
    ) = runBlocking {
        progress.onStageChanged("Завантажуємо $optiFineVersion")

        val optifineUrl = installer + optiFineVersion
        val optifinePath = di.instanceManager().getMinecraftDir(instance)
            .resolve("mods").resolve(optiFineVersion)

        try {
            DownloadManager(
                di.httpClient(),
                di.logger(),
                fileUrl = optifineUrl,
                saveAs = optifinePath
            ).downloadFile { value, size -> progress.onProgress(value, size) }
        }finally { }
    }
}