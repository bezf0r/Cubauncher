package ua.besf0r.cubauncher.network

import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import ua.besf0r.cubauncher.httpClient
import ua.besf0r.cubauncher.network.file.FilesManager
import ua.besf0r.cubauncher.network.file.FilesManager.createFileIfNotExists
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import java.util.*

class DownloadManager (
    private val url: String?,
    private var sha1: String?,
    private val declaredSize: Long,
    private val saveAs: Path
) {
    private fun calculateHash(stream: InputStream): String {
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
    @Throws(IOException::class)
    fun execute(downloadProgress: DownloadProgress) {
        try {
            createDirectoriesAndFile()
            downloadFile(downloadProgress)
        } catch (_: Exception) {
        }
    }

    private fun downloadFile(downloadProgress: DownloadProgress) {
        url ?: return
        runBlocking {
            withContext(Dispatchers.IO) {
                if (!shouldDownloadFile()) return@withContext
                httpClient.get(url) {
                    onDownload { bytesSentTotal, _ ->
                        downloadProgress(bytesSentTotal, declaredSize)
                    }
                }.bodyAsChannel().copyTo(saveAs.toFile().writeChannel())
            }
        }
    }

    private fun createDirectoriesAndFile() {
        FilesManager.createDirectories(saveAs.parent)
        saveAs.createFileIfNotExists()
    }
    private suspend fun shouldDownloadFile(): Boolean {
        val fileSize = withContext(Dispatchers.IO) {
            Files.size(saveAs)
        }
        return if (fileSize != declaredSize) {
            true
        } else {
            if (sha1 == null) {
                sha1 = calculateHash(httpClient.get(url!!).body())
            }

            val shaInDisk = calculateHash(withContext(Dispatchers.IO) {
                FileInputStream(saveAs.toFile())
            })
            shaInDisk != sha1
        }
    }
}
