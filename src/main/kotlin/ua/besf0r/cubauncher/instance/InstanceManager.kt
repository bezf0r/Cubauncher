package ua.besf0r.cubauncher.instance

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ua.besf0r.cubauncher.util.OsEnum
import ua.besf0r.cubauncher.util.FileUtil
import ua.besf0r.cubauncher.util.IOUtils
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists

class InstanceManager(
    private val workDir: Path
) {

    val instances: MutableList<Instance> = mutableListOf()
    private val minecraftDirName: String = if (OsEnum.oS == OsEnum.MACOS)
        "minecraft" else ".minecraft"

    @Throws(IOException::class)
    fun loadInstances() {
        val paths = mutableListOf<File>()
        workDir.toFile().listFiles()?.forEach {
            paths.add(it)
        }
        paths.forEach {
            if (!it.isDirectory) return@forEach

            val instanceFile = it.resolve("instance.json")
            if (!instanceFile.exists()) return@forEach

            val instance = Json.decodeFromString<Instance>(
                IOUtils.readUtf8String(instanceFile.toPath()))
            instances.add(instance)
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
    fun save(instance: Instance) {
        val instanceDir = getInstanceDir(instance)
        if (!instanceDir.exists()) return
        val instanceFile = instanceDir.resolve("instance.json")
        IOUtils.writeUtf8String(instanceFile, Json.encodeToString<Instance>(instance))
    }

    @Throws(IOException::class)
    fun createInstance(name: String, minecraftVersion: String): Instance {
        val instance = Instance(name, minecraftVersion)
        instances.add(instance)

        instance.name = instance.name
        createDirName(instance)
        val instanceDir = getInstanceDir(instance)

        FileUtil.createDirectoryIfNotExists(instanceDir)
        val minecraftDir = instanceDir.resolve(minecraftDirName)
        FileUtil.createDirectoryIfNotExists(minecraftDir)
        val instanceFile = instanceDir.resolve("instance.json")
        FileUtil.createFileIfNotExists(instanceDir)

        IOUtils.writeUtf8String(instanceFile, Json.encodeToString(instance))

        return instance
    }

    @Throws(IOException::class)
    fun deleteInstance(name: String) {
        val instance = getInstanceByName(name)
        val instanceDir = getInstanceDir(instance!!)
        if (Files.exists(instanceDir)) {
            FileUtil.deleteDirectoryRecursively(instanceDir)
        }
        instances.remove(instance)
    }


    private fun getInstanceDir(instance: Instance): Path {
        return workDir.resolve(instance.name)
    }

    fun getMinecraftDir(instance: Instance): Path {
        return workDir.resolve(instance.name).resolve(".minecraft")
    }

    fun getInstanceByName(name: String): Instance? {
        return instances.find { it.name == name }
    }
}
