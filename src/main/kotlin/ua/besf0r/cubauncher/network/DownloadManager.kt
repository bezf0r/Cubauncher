package ua.besf0r.cubauncher.network

import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import ua.besf0r.cubauncher.httpClient
import ua.besf0r.cubauncher.network.file.FilesManager
import ua.besf0r.cubauncher.network.file.FilesManager.createFileIfNotExists
import java.io.FileInputStream
import java.io.IOException
import java.nio.file.Path
import java.security.MessageDigest
import java.util.*
import java.util.concurrent.Executors
import kotlin.io.path.exists

class DownloadManager(
    private val fileUrl: String,
    private var sha1: String? = null,
    private val declaredSize: Long = 0,
    private val saveAs: Path
) {
    @Throws(IOException::class)
    fun execute(
        shouldCheck: Boolean = true,
        downloadProgress: DownloadProgress
    )  {
        if (shouldCheck){
            if (!shouldDownloadFile(sha1, saveAs)) return
        }

        try {
            createDirectoriesAndFile()
            downloadFile(downloadProgress)
        } catch (_: Exception) { }
    }

    private fun downloadFile(
        downloadProgress: DownloadProgress
    ) = runBlocking {
        withContext(Dispatchers.IO) {
            httpClient.prepareRequest {
                url(fileUrl)
                onDownload{bytesSentTotal, _ ->
                    downloadProgress(bytesSentTotal, declaredSize)
                }
            }.execute {
                it.bodyAsChannel().copyAndClose(saveAs.toFile().writeChannel())
            }
        }
    }


    private fun createDirectoriesAndFile() {
        FilesManager.createDirectories(saveAs.parent)
        saveAs.createFileIfNotExists()
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
            downloadProgress: (String, Long, Long) -> Unit
        ) = runBlocking {
            val context = Executors.newFixedThreadPool(2).asCoroutineDispatcher()

            launch(context){
                files.filter { shouldDownloadFile(it.sha1, it.saveAs) }.map { file ->
                    val downloadManager = DownloadManager(
                        file.url, file.sha1,
                        file.declaredSize, file.saveAs
                    )
                    downloadManager.execute(false) { bytesSentTotal, totalSize ->
                        downloadProgress(file.url, bytesSentTotal, totalSize)
                    }
                }
            }
            context.close()
        }

        val json = Json { ignoreUnknownKeys = true }

        inline fun <reified T> downloadDataList(url: String): List<T> = try {
            runBlocking {
                val response = httpClient.get(url).bodyAsText()
                json.decodeFromString<List<T>>(response)
            }
        } catch (e: Exception) {
            e.printStackTrace()
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
