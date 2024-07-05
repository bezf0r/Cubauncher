package ua.besf0r.kovadlo.minecraft.forge

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.utils.io.errors.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.*
import org.jetbrains.skiko.MainUIDispatcher
import ua.besf0r.kovadlo.Logger
import ua.besf0r.kovadlo.instance.Instance
import ua.besf0r.kovadlo.instanceManager
import ua.besf0r.kovadlo.minecraft.ModificationManager
import ua.besf0r.kovadlo.minecraft.OperatingSystem
import ua.besf0r.kovadlo.minecraft.forge.newprofile.ForgeNewInstallProfile
import ua.besf0r.kovadlo.minecraft.forge.oldprofile.ForgeOldIntallProfile
import ua.besf0r.kovadlo.network.DownloadListener
import ua.besf0r.kovadlo.network.DownloadManager
import ua.besf0r.kovadlo.network.file.FileManager.createDirectoryIfNotExists
import ua.besf0r.kovadlo.network.file.FileManager.createFileIfNotExists
import ua.besf0r.kovadlo.network.file.IOUtil
import ua.besf0r.kovadlo.workDir
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

class ForgeInstaller : ModificationManager {
    private val json = Json { ignoreUnknownKeys = true }

    private val installer = "https://maven.minecraftforge.net/net/minecraftforge/forge/"

    @Throws(Exception::class)
    override fun download(
        progress: DownloadListener,
        version: String,
        instance: Instance
    ): Unit = runBlocking {
        progress.onStageChanged("Завантажуємо Forge($version)")

        val installerUrl = "$installer${version}/forge-${version}-installer.jar"

        val installerPath = workDir.resolve("forge-installer-${version}.jar")
        val installerLog = workDir.resolve("forge-installer-${version}.jar.log")
        val profiles = createFakeLauncherProfiles()

        try {
            DownloadManager(
                fileUrl = installerUrl,
                saveAs = installerPath
            ).downloadFile { value, size -> progress.onProgress(value, size) }
            delay(500)

            processInstallation(instance, installerPath)

            instanceManager.getMinecraftDir(instance).resolve("mods").createDirectoryIfNotExists()
        }finally {
            try {
                CoroutineScope(MainUIDispatcher).launch {
                    delay(10000)
                    profiles.deleteIfExists()
                    installerLog.deleteIfExists()
                    installerPath.deleteIfExists()
                }
            }catch (_: Exception){}
        }
    }

    private fun processInstallation(instance: Instance, installerPath: Path) {
        if (isNewVersion(instance.minecraftVersion)) {
            val newTask = newForgeDownload(installerPath)
            instance.forgeNewInstallProfile = newTask
            instance.mainClass = newTask?.mainClass.toString()
        } else {
            val oldTask = oldForgeDownload(installerPath)
            instance.forgeOldIntallProfile = oldTask?.versionInfo
            instance.mainClass = oldTask?.versionInfo?.mainClass.toString()
        }
    }

    private fun createFakeLauncherProfiles(): Path {
        val profiles = workDir.resolve("launcher_profiles.json").createFileIfNotExists()
        IOUtil.writeUtf8String(
            profiles,
            "{\"selectedProfile\": \"(Default)\",\"profiles\": {\"(Default)\": {\"name\": \"(Default)\"}},\"clientToken\": \"88888888-8888-8888-8888-888888888888\"}"
        )
        return profiles
    }

    private fun isNewVersion(version: String): Boolean {
        val limitVersion = "1.12.1"

        val versionParts = version.split(".").map { it.toIntOrNull() ?: 0 }
        val limitVersionParts = limitVersion.split(".").map { it.toIntOrNull() ?: 0 }

        for (i in 0 until maxOf(versionParts.size, limitVersionParts.size)) {
            val vPart = versionParts.getOrElse(i) { 0 }
            val lPart = limitVersionParts.getOrElse(i) { 0 }

            if (vPart != lPart)  return vPart > lPart
        }
        return false
    }

    private fun newForgeDownload(installerPath: Path): ForgeNewInstallProfile? {
        var forgeProfile: ForgeNewInstallProfile? = null
        val zipFile = ZipFile(installerPath.toFile())
        zipFile.use { zip ->
            val entry = zip.getEntry("version.json")
            if (entry != null) {
                val stream = zip.getInputStream(entry).readBytes().decodeToString()
                val installProfile = json.decodeFromString<ForgeNewInstallProfile>(stream)
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
    private fun oldForgeDownload(installerPath: Path): ForgeOldIntallProfile? {
        var forgeProfile: ForgeOldIntallProfile? = null

        val zipFile = ZipFile(installerPath.toFile())
        zipFile.use { zip ->
            val entry = zip.getEntry("install_profile.json")
            if (entry != null) {
                val stream = zip.getInputStream(entry).readBytes().decodeToString()
                val installProfile = json.decodeFromString<ForgeOldIntallProfile>(stream)
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