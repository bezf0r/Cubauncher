package ua.besf0r.cubauncher.minecraft.version

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import ua.besf0r.cubauncher.network.DownloadManager
import ua.besf0r.cubauncher.versionsDir

object VersionList {
    private const val VM_V2 = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json"

    fun download() {
        val dir = versionsDir.resolve("version_manifest_v2.json")

        runBlocking {
            withContext(Dispatchers.IO) {
                DownloadManager(VM_V2, null, 0, dir).execute { _, _ -> }
            }
        }
    }
}
