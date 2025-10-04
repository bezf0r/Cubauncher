package ua.besf0r.kovadlo.settings.directories

import java.nio.file.Path

data class WorkingDirs(val workDir: Path){
    val assetsDir: Path get() = workDir.resolve("assets")
    val librariesDir: Path get() = workDir.resolve("libraries")
    val instancesDir: Path get() = workDir.resolve("instances")
    val versionsDir: Path get() = workDir.resolve("versions")
    val javaDir: Path get() = workDir.resolve("java")
    val settingsFile: Path get() = workDir.resolve("settings.json")
    val localizationDir: Path get() = workDir.resolve("localization")
}