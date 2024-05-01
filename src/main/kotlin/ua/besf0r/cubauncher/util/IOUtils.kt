package ua.besf0r.cubauncher.util

import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

class IOUtils private constructor() {
    init {
        throw UnsupportedOperationException()
    }

    companion object {
        @Throws(IOException::class)
        fun writeUtf8String(file: Path, s: String) {
            Files.write(file, s.toByteArray(StandardCharsets.UTF_8))
        }

        @Throws(IOException::class)
        fun readUtf8String(file: Path): String {
            return java.lang.String.join(System.lineSeparator(), Files.readAllLines(file))
        }
    }
}
