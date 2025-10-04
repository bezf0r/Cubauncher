package ua.besf0r.kovadlo.network

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import ua.besf0r.kovadlo.Logger
import ua.besf0r.kovadlo.network.DownloadService.Companion.calculateSHA1
import ua.besf0r.kovadlo.network.file.FileManager
import java.nio.file.Path
import java.security.MessageDigest
import java.util.concurrent.atomic.AtomicLong
import kotlin.coroutines.cancellation.CancellationException
import kotlin.io.path.exists
import kotlin.io.path.outputStream
import kotlin.io.path.writeBytes

class DownloadManager(
    private val httpClient: HttpClient,
    private val logger: Logger,
    private val fileUrl: String,
    private val saveAs: Path,
    private val sha1: String? = null,
    private val declaredSize: Long = 0
) {

    suspend fun downloadFile(
        downloadProgress: (Long, Long) -> Unit = { _, _ -> }
    ) = coroutineScope {
        if (!shouldDownload()) return@coroutineScope

        FileManager.createDirectories(saveAs.parent)
        val tempFile = saveAs.resolveSibling("${saveAs.fileName}.tmp")
        if (!tempFile.exists()) tempFile.writeBytes(byteArrayOf())

        try {
            val response = httpClient.get(fileUrl)
            val channel: ByteReadChannel = response.body()
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            val totalDownloaded = AtomicLong(0)

            tempFile.outputStream().use { output ->
                while (!channel.isClosedForRead) {
                    ensureActive()
                    val read = channel.readAvailable(buffer)
                    if (read > 0) {
                        output.write(buffer, 0, read)
                        val current = totalDownloaded.addAndGet(read.toLong())
                        downloadProgress(current, declaredSize)
                    }
                }
            }

            if (saveAs.exists()) saveAs.toFile().delete()
            tempFile.toFile().renameTo(saveAs.toFile())

        } catch (e: CancellationException) {
            logger.publish("launcher", "Download cancelled: $fileUrl")
            if (tempFile.exists()) tempFile.toFile().delete()
            throw e
        } catch (e: Exception) {
            logger.publish("launcher", "Error downloading $fileUrl:\n${e.stackTraceToString()}")
            if (tempFile.exists()) tempFile.toFile().delete()
            throw e
        }
    }

    suspend fun shouldDownload(): Boolean {
        if (!saveAs.exists()) return true
        if (sha1 == null) return true
        val fileSha1 = calculateSHA1(saveAs)
        return fileSha1 != sha1
    }
}

class DownloadService(
    val logger: Logger,
    val httpClient: HttpClient,
    private val scope: CoroutineScope
) {
    val json = Json { ignoreUnknownKeys = true }

    suspend fun executeMultiple(
        files: List<DownloadFile>,
        downloadProgress: (Long, Long) -> Unit
    ) {
        val totalSize = files.sumOf { it.declaredSize }
        val downloaded = AtomicLong(0)

        val filesToDownload = files.filter {
            DownloadManager(httpClient, logger, it.url, it.saveAs, it.sha1, it.declaredSize).shouldDownload()
        }

        supervisorScope {
            filesToDownload.chunked(20).forEach { chunk ->
                chunk.map { file ->
                    scope.async {
                        try {
                            DownloadManager(
                                httpClient,
                                logger,
                                file.url,
                                file.saveAs,
                                file.sha1,
                                file.declaredSize
                            ).downloadFile { current, _ ->
                                val new = downloaded.addAndGet(current)
                                downloadProgress(new, totalSize)
                            }
                        } catch (e: Exception) {
                            logger.publish("launcher", "Failed to download ${file.url}:\n${e.stackTraceToString()}")
                        }
                    }
                }.awaitAll()
            }
        }
    }

    inline fun <reified T> downloadDataList(url: String): List<T> = runBlocking {
        return@runBlocking try {
            val response = httpClient.get(url).bodyAsText()
            json.decodeFromString<List<T>>(response)
        } catch (e: Exception) {
            logger.publish("launcher", "Failed to download data from $url:\n${e.stackTraceToString()}")
            emptyList()
        }
    }

    data class DownloadFile(
        val url: String,
        val sha1: String? = null,
        val declaredSize: Long = 0,
        val saveAs: Path
    )

    companion object{
        suspend fun shouldDownload(sha1: String?, saveAs: Path): Boolean {
            if (!saveAs.exists()) return true
            if (sha1 == null) return true
            val fileSha1 = calculateSHA1(saveAs)
            return fileSha1 != sha1
        }

        suspend fun calculateSHA1(path: Path): String = withContext(Dispatchers.Default) {
            val digest = MessageDigest.getInstance("SHA-1")
            path.toFile().inputStream().use { stream ->
                val buffer = ByteArray(4096)
                var read = stream.read(buffer)
                while (read != -1) {
                    digest.update(buffer, 0, read)
                    read = stream.read(buffer)
                }
            }
            return@withContext digest.digest().joinToString("") { "%02x".format(it) }
        }
    }
}