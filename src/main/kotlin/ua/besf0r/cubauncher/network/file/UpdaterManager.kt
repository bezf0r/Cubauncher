package ua.besf0r.cubauncher.network.file

import io.ktor.network.sockets.*
import ua.besf0r.cubauncher.librariesDir
import ua.besf0r.cubauncher.minecraft.OperatingSystem
import ua.besf0r.cubauncher.network.DownloadManager
import ua.besf0r.cubauncher.network.file.FilesManager.createFileIfNotExists
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URISyntaxException
import java.net.URL
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.Path
import kotlin.io.path.pathString
import kotlin.system.exitProcess

object UpdaterManager {
    private const val GITHUB_TOKEN = "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"
    private const val GITHUB_REPO = "bezf0r/cubauncherdata"

    private val fileDir = librariesDir.resolve("ua/besf0r/updater/updater.jar")
    fun checkForUpdates() {
        try {
            val fileInfo = readConfigAsString().split(";".toRegex()).dropLastWhile {
                it.isEmpty()
            }.toTypedArray()

            val expectedHash = fileInfo[0]
            if (!DownloadManager.shouldDownloadFile(expectedHash,
                    Path(byGetProtectionDomain(UpdaterManager::class.java)))) return

            ProcessBuilder(
                OperatingSystem.javaType,
                "-jar", fileDir.pathString,
                "-update", byGetProtectionDomain(UpdaterManager::class.java)
            ).start()

            exitProcess(0)
        }catch (_: Exception){}
    }

    @Throws(IOException::class)
    private fun readConfigAsString(): String {
        val url = URL("https://api.github.com/repos/$GITHUB_REPO/contents/config.txt")
        val connection = (url.openConnection() as HttpURLConnection).authToGit()

        val `in` = BufferedReader(InputStreamReader(connection.inputStream))

        val dataBuilder = StringBuilder()
        var inputLine: String
        while ((`in`.readLine().also { inputLine = it }) != null) {
            println("The data of the saved file: $inputLine")
            dataBuilder.append(inputLine)
        }
        return dataBuilder.toString()
    }
    fun downloadUpdater() {
        try {
            val url = URL("https://api.github.com/repos/$GITHUB_REPO/contents/updater.jar")
            val connection = (url.openConnection() as HttpURLConnection).authToGit()

            val baos = ByteArrayOutputStream()
            connection.inputStream.use { `in` ->
                val buffer = ByteArray(4096)
                var bytesRead: Int
                while ((`in`.read(buffer).also { bytesRead = it }) != -1) {
                    baos.write(buffer, 0, bytesRead)
                }
            }

            val fileData = baos.toByteArray()

            FilesManager.createDirectories(fileDir.parent)
            fileDir.createFileIfNotExists()

            Files.write(fileDir, fileData)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    private fun HttpURLConnection.authToGit(): HttpURLConnection {
        return this.apply {
            requestMethod = "GET"
            setRequestProperty("Authorization", "token $GITHUB_TOKEN")
            setRequestProperty("Accept", "application/vnd.github.v3.raw")
        }
    }
    @Throws(URISyntaxException::class)
    fun byGetProtectionDomain(clazz: Class<*>): String {
        val url = clazz.protectionDomain.codeSource.location
        return Paths.get(url.toURI()).toString()
    }
}