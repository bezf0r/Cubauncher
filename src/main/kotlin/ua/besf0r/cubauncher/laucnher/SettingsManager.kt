package ua.besf0r.cubauncher.laucnher

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ua.besf0r.cubauncher.network.file.FilesManager.createFileIfNotExists
import ua.besf0r.cubauncher.network.file.IOUtil
import java.nio.file.Path
import kotlin.io.path.exists

class SettingsManager(
    private val settingsFile: Path
) {
    lateinit var settings: LauncherSettings

    private val json = Json { ignoreUnknownKeys = true }
    fun loadSettings(){
        settingsFile.createFileIfNotExists()

        settings = try {
            json.decodeFromString<LauncherSettings>(
                IOUtil.readUtf8String(settingsFile))
        }catch (_: IllegalArgumentException){
            LauncherSettings(512,2048)
        }
    }
    fun update() {
        val settingsAsString = Json.encodeToString<LauncherSettings>(settings)
        IOUtil.writeUtf8String(settingsFile, settingsAsString)
    }
}