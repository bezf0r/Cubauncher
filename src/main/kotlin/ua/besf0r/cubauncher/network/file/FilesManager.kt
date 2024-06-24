package ua.besf0r.cubauncher.network.file

import java.io.IOException
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
import kotlin.io.path.createFile

object FilesManager {
    private val deleteVisitor = object : SimpleFileVisitor<Path>() {
        override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
            Files.delete(file)
            return FileVisitResult.CONTINUE
        }

        override fun postVisitDirectory(dir: Path, e: IOException?): FileVisitResult {
            Files.delete(dir)
            return FileVisitResult.CONTINUE
        }
    }

    @Throws(IOException::class)
    fun deleteDirectoryRecursively(path: Path) {
        if (!Files.exists(path)) return

        if (Files.isRegularFile(path)) Files.delete(path)

        Files.walkFileTree(path, deleteVisitor)
    }

    @Throws(IOException::class)
    fun Path.createDirectoryIfNotExists(): Path {
        if (!Files.exists(this)) Files.createDirectories(this)
        return this
    }

    @Throws(IOException::class)
    fun Path.createFileIfNotExists(): Path {
        if (!Files.exists(this)) this.createFile()
        return this
    }

    fun createDirectories(vararg directories: Path) {
        try {
            directories.forEach { it.createDirectoryIfNotExists() }
        } catch (_: IOException) { }
    }
}
