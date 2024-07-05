package ua.besf0r.kovadlo.minecraft.minecraft

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import ua.besf0r.kovadlo.network.DownloadManager
import ua.besf0r.kovadlo.network.file.IOUtil
import ua.besf0r.kovadlo.versionsDir

object MinecraftVersionList {
    private const val VM_V2 = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json"
    private val json = Json { ignoreUnknownKeys = true }

    val versions = runBlocking {
        val versions: List<VersionManifest.Version>

        val dir = versionsDir.resolve("version_manifest_v2.json")
        DownloadManager(VM_V2, null, 0, dir).downloadFile { _, _ -> }

        versions = json.decodeFromString<VersionManifest
        .VersionManifest>(IOUtil.readUtf8String(dir)).versions

        return@runBlocking versions
    }
}
