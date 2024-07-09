package ua.besf0r.kovadlo.network.file

import java.io.IOException
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
import kotlin.io.path.createFile
import kotlin.io.path.pathString

object FileManager {
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

    operator fun Path.minus(other: Path): String {
        return this.pathString.removePrefix(other.pathString)
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

    fun Path.createFileIfNotExists(): Path {
        return try {
            this.createFile()
        }catch (e: FileAlreadyExistsException){
            this
        }
    }

    fun createDirectories(vararg directories: Path) =
        directories.forEach { it.createDirectoryIfNotExists() }

}
