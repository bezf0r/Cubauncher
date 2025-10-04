package ua.besf0r.kovadlo.instance

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.platform.LocalLocalization
import kotlinx.coroutines.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import org.jetbrains.skiko.MainUIDispatcher
import ua.besf0r.kovadlo.LocalAppStrings
import ua.besf0r.kovadlo.settings.directories.WorkingDirs
import ua.besf0r.kovadlo.minecraft.OperatingSystem
import ua.besf0r.kovadlo.minecraft.forge.newprofile.ForgeNewInstallProfile
import ua.besf0r.kovadlo.minecraft.forge.newprofile.NewProfilePathSerializer
import ua.besf0r.kovadlo.minecraft.forge.oldprofile.ForgeOldIntallProfile
import ua.besf0r.kovadlo.minecraft.forge.oldprofile.OldProfilePathSerializer
import ua.besf0r.kovadlo.minecraft.liteloader.LiteLoaderPathSerializer
import ua.besf0r.kovadlo.minecraft.liteloader.LiteLoaderProfile
import ua.besf0r.kovadlo.network.file.FileManager
import ua.besf0r.kovadlo.network.file.FileManager.createDirectoryIfNotExists
import ua.besf0r.kovadlo.network.file.FileManager.createFileIfNotExists
import ua.besf0r.kovadlo.network.file.IOUtil
import ua.besf0r.kovadlo.settings.SettingsManager
import java.io.IOException
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardWatchEventKinds
import kotlin.io.path.exists

class InstanceManager(
    coroutineScope: CoroutineScope,
    private val workingDirs: WorkingDirs,
    private val settingsManager: SettingsManager
) {
    val instances = mutableStateListOf<Instance>()

    private val minecraftDirName: String =
        if (OperatingSystem.oS == OperatingSystem.MACOS) "minecraft" else ".minecraft"

    init {
        try {
            workingDirs.instancesDir.createDirectoryIfNotExists()
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
        watchInstancesDir(coroutineScope)
    }

    private val module = SerializersModule {
        contextual(ForgeNewInstallProfile::class, NewProfilePathSerializer(workingDirs))
        contextual(ForgeOldIntallProfile.VersionInfo::class, OldProfilePathSerializer(workingDirs))
        contextual(LiteLoaderProfile::class, LiteLoaderPathSerializer(workingDirs))
    }

    private val json = Json {
        ignoreUnknownKeys = true
        serializersModule = module
    }

    fun loadInstances() {
        val loaded = workingDirs.instancesDir.toFile()
            .listFiles()
            ?.filter { it.isDirectory && it.resolve("instance.json").exists() }
            ?.map { file ->
                json.decodeFromString<Instance>(
                    IOUtil.readUtf8String(file.resolve("instance.json").toPath())
                )
            } ?: emptyList()

        instances.clear()
        instances.addAll(loaded)
    }

    private fun watchInstancesDir(scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            val watcher = FileSystems.getDefault().newWatchService()
            workingDirs.instancesDir.register(
                watcher,
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_DELETE,
                StandardWatchEventKinds.ENTRY_MODIFY
            )

            while (isActive) {
                val key = watcher.take()
                key.reset()
                withContext(MainUIDispatcher) {
                    loadInstances()
                }
                delay(300)
            }
        }
    }

    private fun createDirName(instance: Instance) {
        var count = 1
        val originalName = instance.name

        while (getInstanceDir(instance).exists()) {
            instance.name = "$originalName($count)"
            count++
        }
    }

    @Throws(IOException::class)
    fun update(instance: Instance) {
        val instanceDir = getInstanceDir(instance)
        if (!instanceDir.exists()) return
        val instanceFile = instanceDir.resolve("instance.json")
        IOUtil.writeUtf8String(instanceFile, Json.encodeToString(instance))
    }

    @Throws(IOException::class)
    fun createInstance(name: String, minecraftVersion: String): Instance {
        val instance = Instance(name, minecraftVersion)
        instances.add(instance)

        instance.name = instance.name
        createDirName(instance)

        val instanceDir = getInstanceDir(instance)
        workingDirs.instancesDir.createDirectoryIfNotExists()

        val minecraftDir = instanceDir.resolve(minecraftDirName)
        minecraftDir.createDirectoryIfNotExists()
        val instanceFile = instanceDir.resolve("instance.json")
        instanceFile.createFileIfNotExists()

        IOUtil.writeUtf8String(instanceFile, Json.encodeToString(instance))

        return instance
    }

    @Throws(IOException::class)
    fun deleteInstance(name: String) {
        val instance = getInstanceByName(name) ?: return
        val instanceDir = getInstanceDir(instance)
        if (Files.exists(instanceDir)) {
            FileManager.deleteDirectoryRecursively(instanceDir)
        }

        val selected = settingsManager.settings.selectedInstance
        if (selected == name) {
            settingsManager.settings.selectedInstance = null
        }
        instances.remove(instance)
    }

    private fun getInstanceDir(instance: Instance): Path {
        return workingDirs.instancesDir.resolve(instance.name)
    }

    fun getMinecraftDir(instance: Instance): Path {
        return workingDirs.instancesDir.resolve(instance.name).resolve(".minecraft")
    }

    fun getMinecraftDir(instance: String): Path {
        return workingDirs.instancesDir.resolve(instance).resolve(".minecraft")
    }

    fun getInstanceByName(name: String): Instance? {
        return instances.find { it.name == name }
    }
}

