package ua.besf0r.cubauncher.network.file

import java.io.BufferedOutputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.URISyntaxException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

object IOUtil {
    @Throws(URISyntaxException::class)
    fun byGetProtectionDomain(clazz: Class<*>): Path {
        val url = clazz.protectionDomain.codeSource.location
        return Paths.get(url.toURI())
    }
    @Throws(IOException::class)
    fun writeUtf8String(file: Path, s: String) {
        Files.write(file, s.toByteArray(StandardCharsets.UTF_8))
    }
    @Throws(IOException::class)
    fun extractFile(inputStream: InputStream, file: Path) {
        val bos = BufferedOutputStream(FileOutputStream(file.toFile()))
        val bytesIn = ByteArray(4096)
        var read: Int
        while (inputStream.read(bytesIn).also { read = it } != -1) {
            bos.write(bytesIn, 0, read)
        }
        bos.close()
    }

    @Throws(IOException::class)
    fun readUtf8String(file: Path): String {
        return java.lang.String.join(System.lineSeparator(), Files.readAllLines(file))
    }
}
