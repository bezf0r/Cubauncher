package ua.besf0r.cubauncher.network.file

import java.io.IOException
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes

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
    fun Path.createDirectoryIfNotExists() {
        if (!Files.exists(this)) Files.createDirectories(this)
    }

    @Throws(IOException::class)
    fun Path.createFileIfNotExists() {
        if (!Files.exists(this)) {
            Files.createFile(this)
        }
    }

    fun createDirectories(vararg directories: Path) {
        directories.forEach {
            try {
                it.createDirectoryIfNotExists()
            } catch (e: IOException) {
                println("Unable to create launcher directories")
            }
        }
    }
}
