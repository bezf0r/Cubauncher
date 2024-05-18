package ua.besf0r.cubauncher.network.file

import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

object IOUtil {
    @Throws(IOException::class)
    fun writeUtf8String(file: Path, s: String) {
        Files.write(file, s.toByteArray(StandardCharsets.UTF_8))
    }

    @Throws(IOException::class)
    fun readUtf8String(file: Path): String {
        return java.lang.String.join(System.lineSeparator(), Files.readAllLines(file))
    }
}
