package ua.besf0r.cubauncher.minecraft.version

import ua.besf0r.cubauncher.network.FileDownloader
import ua.besf0r.cubauncher.versionsDir
import java.nio.file.Files

object VersionList {
    private const val VM_V2 = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json"

    fun download() {
        val dir = versionsDir.resolve("version_manifest_v2.json")
        FileDownloader(VM_V2,null, 0, dir).execute{ _, _->}
    }
}
