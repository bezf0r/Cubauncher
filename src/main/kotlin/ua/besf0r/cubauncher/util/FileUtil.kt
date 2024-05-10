package ua.besf0r.cubauncher.util

import java.io.IOException
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes

object FileUtil {
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
    fun createDirectoryIfNotExists(path: Path) {
        if (!Files.exists(path)) Files.createDirectories(path)
    }

    @Throws(IOException::class)
    fun createFileIfNotExists(file: Path) {
        if (!Files.exists(file)) {
            Files.createDirectories(file.parent)
            Files.createFile(file)
        }
    }

    fun createDirectories(vararg directories: Path) {
        directories.forEach {
            try { createDirectoryIfNotExists(it) } catch (e: IOException) {
                println("Unable to create launcher directories") }
        }
    }
}