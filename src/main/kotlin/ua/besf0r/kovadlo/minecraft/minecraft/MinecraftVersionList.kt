package ua.besf0r.kovadlo.minecraft.minecraft

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json

class MinecraftVersionList(
    private val httpClient: HttpClient
) {
    private val VM_V2 = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json"
    private val json = Json { ignoreUnknownKeys = true }

    val versions = runBlocking {
        return@runBlocking json.decodeFromString<VersionManifest
        .VersionManifest>(httpClient.get(VM_V2).bodyAsText()).versions
    }
}
