package ua.besf0r.kovadlo.network

import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import ua.besf0r.kovadlo.Logger
import ua.besf0r.kovadlo.httpClient
import ua.besf0r.kovadlo.network.file.FileManager
import ua.besf0r.kovadlo.network.file.FileManager.createFileIfNotExists
import java.io.FileInputStream
import java.io.IOException
import java.nio.file.Path
import java.security.MessageDigest
import java.util.*
import kotlin.io.path.exists

class DownloadManager(
    private val fileUrl: String,
    private var sha1: String? = null,
    private val declaredSize: Long = 0,
    private val saveAs: Path
) {
    @Throws(IOException::class)
    fun downloadFile(
        downloadProgress: DownloadProgress? = null
    ) = runBlocking {
        if (!shouldDownloadFile(sha1, saveAs)) return@runBlocking
        downloadFileAsync(downloadProgress)
    }

    private suspend fun downloadFileAsync(
        downloadProgress: DownloadProgress?
    ) = runBlocking {
        FileManager.createDirectories(saveAs.parent)
        saveAs.createFileIfNotExists()

        httpClient.prepareRequest {
            url(fileUrl)
            onDownload { bytesSentTotal, _ ->
                downloadProgress?.invoke(bytesSentTotal, declaredSize)
            }
        }.execute {
            it.bodyAsChannel().copyAndClose(saveAs.toFile().writeChannel())
        }
    }

    companion object {
        private fun calculateHash(stream: FileInputStream): String {
            val digest = MessageDigest.getInstance("SHA-1")
            val buffer = ByteArray(4096)
            var bytesRead = stream.read(buffer)
            while (bytesRead != -1) {
                digest.update(buffer, 0, bytesRead)
                bytesRead = stream.read(buffer)
            }
            stream.close()
            val hashBytes = digest.digest()
            val hexString = StringBuilder()
            for (byte in hashBytes) {
                hexString.append(String.format("%02x", byte))
            }
            return hexString.toString()
        }

        fun shouldDownloadFile(
            sha1: String?,
            saveAs: Path
        ): Boolean {
            if (!saveAs.exists()) return true
            if (sha1 == null) return true

            val shaInDisk = calculateHash(FileInputStream(saveAs.toFile()))

            return shaInDisk != sha1
        }

        @Throws(IOException::class)
        fun executeMultiple(
            files: List<DownloadFile>,
            taskJob: CoroutineScope? = null,
            chunkSize: Int = 15,
            downloadProgress: (Long, Long) -> Unit
        ) = runBlocking {
            val total = files.sumOf { it.declaredSize }
            var downloaded = 0L

            val forDownload = files.filter { shouldDownloadFile(it.sha1, it.saveAs) }
            files.filter { it !in forDownload }.map { downloaded += it.declaredSize }

            forDownload.chunked(chunkSize).forEach { chunk ->
                val requests = ArrayList<Deferred<Unit>>()
                chunk.map { file ->
                    requests.add(async {
                        taskJob?.let { if (!it.isActive) return@async }
                        val downloadManager = DownloadManager(
                            file.url, file.sha1,
                            file.declaredSize, file.saveAs
                        )
                        downloadManager.downloadFileAsync { _, _ -> }
                        downloaded += file.declaredSize
                        downloadProgress(downloaded, total)
                    })
                }
                requests.awaitAll()
            }
        }

        val json = Json { ignoreUnknownKeys = true }

        inline fun <reified T> downloadDataList(url: String): List<T> = try {
            runBlocking {
                val response = httpClient.get(url).bodyAsText()
                json.decodeFromString<List<T>>(response)
            }
        } catch (e: Exception) {
            Logger.publish(e.stackTraceToString())
            listOf()
        }
    }

    data class DownloadFile(
        val url: String,
        val sha1: String? = null,
        val declaredSize: Long = 0,
        val saveAs: Path
    )
}