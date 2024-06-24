package ua.besf0r.cubauncher.minecraft.forge

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.utils.io.errors.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.*
import ua.besf0r.cubauncher.Logger
import ua.besf0r.cubauncher.instance.Instance
import ua.besf0r.cubauncher.instanceManager
import ua.besf0r.cubauncher.librariesDir
import ua.besf0r.cubauncher.minecraft.OperatingSystem
import ua.besf0r.cubauncher.minecraft.forge.newprofile.NewForgeProfile
import ua.besf0r.cubauncher.minecraft.forge.oldprofile.OldForgeProfile
import ua.besf0r.cubauncher.network.DownloadListener
import ua.besf0r.cubauncher.network.DownloadManager
import ua.besf0r.cubauncher.network.file.FilesManager.createDirectoryIfNotExists
import ua.besf0r.cubauncher.network.file.FilesManager.createFileIfNotExists
import ua.besf0r.cubauncher.network.file.IOUtil
import ua.besf0r.cubauncher.network.file.MavenUtil
import ua.besf0r.cubauncher.workDir
import java.io.*
import java.io.File
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.net.MalformedURLException
import java.net.URL
import java.net.URLClassLoader
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.util.zip.ZipFile
import kotlin.io.path.deleteIfExists

class ForgeInstaller {
    private val json = Json { ignoreUnknownKeys = true }

    private val installer = "https://maven.minecraftforge.net/net/minecraftforge/forge/"

    @Throws(Exception::class)
    fun download(
        progress: DownloadListener,
        forgeVersion: String,
        instance: Instance
    ) = runBlocking {
        progress.onStageChanged("Завантажуємо Forge($forgeVersion)")

        val installerUrl = "$installer${forgeVersion}/forge-${forgeVersion}-installer.jar"

        val installerPath = workDir.resolve("forge-installer-${forgeVersion}.jar")
        val installerLog = workDir.resolve("forge-installer-${forgeVersion}.jar.log")
        val profiles = workDir.resolve("launcher_profiles.json").createFileIfNotExists()
        IOUtil.writeUtf8String(profiles, "{\"selectedProfile\": \"(Default)\",\"profiles\": {\"(Default)\": {\"name\": \"(Default)\"}},\"clientToken\": \"88888888-8888-8888-8888-888888888888\"}")

        try {
            DownloadManager(
                fileUrl = installerUrl,
                saveAs = installerPath
            ).execute { value, size -> progress.onProgress(value, size) }

            if (isNewVersion(instance.minecraftVersion)){
                val newTask = newForgeDownload(installerPath)
                instance.newForgeProfile = newTask
                instance.mainClass = newTask?.mainClass.toString()
            }else{
                val oldTask = oldForgeDownload(installerPath)
                instance.oldForgeProfile = oldTask?.versionInfo
                instance.mainClass = oldTask?.versionInfo?.mainClass.toString()
            }

            instanceManager.getMinecraftDir(instance).resolve("mods")
                .createDirectoryIfNotExists()
        }finally {
            try {
                profiles.deleteIfExists()
                installerLog.deleteIfExists()
                installerPath.deleteIfExists()
            }catch (_: Exception){}
        }
    }
    private fun isNewVersion(version: String): Boolean {
        val limitVersion = "1.12.1"

        val versionParts = version.split(".").map { it.toIntOrNull() ?: 0 }
        val limitVersionParts = limitVersion.split(".").map { it.toIntOrNull() ?: 0 }

        for (i in 0 until maxOf(versionParts.size, limitVersionParts.size)) {
            val vPart = versionParts.getOrElse(i) { 0 }
            val lPart = limitVersionParts.getOrElse(i) { 0 }

            if (vPart != lPart) {
                return vPart > lPart
            }
        }
        return false
    }

    private fun newForgeDownload(installerPath: Path): NewForgeProfile? {
        var forgeProfile: NewForgeProfile? = null
        val zipFile = ZipFile(installerPath.toFile())
        zipFile.use { zip ->
            val entry = zip.getEntry("version.json")
            if (entry != null) {
                val stream = zip.getInputStream(entry).readBytes().decodeToString()
                val installProfile = json.decodeFromString<NewForgeProfile>(stream)
                forgeProfile = installProfile
            }
        }
        zipFile.close()

        val process = ProcessBuilder(
            listOf(
                OperatingSystem.javaType,
                "-jar",
                installerPath.toFile().name,
                "--installClient"
            )
        ).directory(workDir.toFile()).start()

        BufferedReader(
            InputStreamReader(process.inputStream, StandardCharsets.UTF_8)
        ).use { reader ->
            var line: String
            while ((reader.readLine().also { line = it }) != null) {
                Logger.publish(line)
            }
        }
        process.waitFor()
        return forgeProfile
    }
    private fun oldForgeDownload(installerPath: Path): OldForgeProfile? {
        var forgeProfile: OldForgeProfile? = null

        val zipFile = ZipFile(installerPath.toFile())
        zipFile.use { zip ->
            val entry = zip.getEntry("install_profile.json")
            if (entry != null) {
                val stream = zip.getInputStream(entry).readBytes().decodeToString()
                val installProfile = json.decodeFromString<OldForgeProfile>(stream)
                forgeProfile = installProfile
            }
        }
        zipFile.close()
        val process = ProcessBuilder(
            listOf(
                OperatingSystem.javaType,
                "-jar",
                installerPath.toFile().name,
                "--installServer"
            )
        ).directory(workDir.toFile()).start()

        BufferedReader(
            InputStreamReader(process.inputStream, StandardCharsets.UTF_8)
        ).use { reader ->
            var line: String
            while ((reader.readLine().also { line = it }) != null) {
                Logger.publish(line)
            }
        }
        process.waitFor()

        val loader = getClassLoader(installerPath)

        val predicate: Class<*>?
        val clientInstall: Class<*>?

        try {
            predicate = Class.forName("com.google.common.base.Predicate", true, loader)
            clientInstall = Class.forName("net.minecraftforge.installer.ClientInstall", true, loader)
        } catch (e: ClassNotFoundException) {
            return null
        }

        val handler = MInvocationHandler()
        val pred = Proxy.newProxyInstance(loader, arrayOf<Class<*>?>(predicate), handler)

        val install = clientInstall.getConstructor().newInstance()
        clientInstall.getDeclaredMethod("run", File::class.java, predicate).invoke(install, workDir.toFile(), pred)

        return forgeProfile
    }
    private fun getClassLoader(jarfile: Path): ClassLoader? {
        try {
            val url = jarfile.toFile().toURI().toURL()
            return URLClassLoader(arrayOf<URL>(url),
                ForgeInstaller::class.java.getClassLoader())
        } catch (e: MalformedURLException) {
            return null
        }
    }
    private class MInvocationHandler : InvocationHandler {
        override fun invoke(proxy: Any?, method: Method?, args: Array<Any?>?): Any {
            return true
        }
    }
}