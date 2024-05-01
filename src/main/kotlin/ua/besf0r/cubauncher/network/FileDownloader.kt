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
import ua.besf0r.cubauncher.util.FileUtil
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import java.util.*
import kotlin.io.path.exists

class FileDownloader (
    private val url: String,
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
    fun execute(downloadListener: (value:Long, size:Long) -> Unit) {
        runBlocking {
            withContext(Dispatchers.IO) {
                FileUtil.createDirectoryIfNotExists(saveAs.parent)
                FileUtil.createFileIfNotExists(saveAs)

                var shouldDownload = false

                if (!saveAs.exists()){
                    shouldDownload = true
                } else {
                    val size = Files.size(saveAs)
                    if (declaredSize == size){
                        if (sha1 == null) sha1 = calculateHash(httpClient.get(url).body())

                        val shaInDisk = calculateHash(FileInputStream(saveAs.toFile()))
                        if (shaInDisk != sha1) shouldDownload = true
                    }else{
                        shouldDownload = true
                    }
                }

                if (shouldDownload) {
                    httpClient.get(url) {
                        onDownload { bytesSentTotal, _ ->
                            downloadListener(
                                bytesSentTotal, declaredSize
                            )
                        }
                    }.bodyAsChannel().copyTo(saveAs.toFile().writeChannel())
                    println("Успішно" + saveAs.toFile().path)
                }
            }
        }
    }
}
