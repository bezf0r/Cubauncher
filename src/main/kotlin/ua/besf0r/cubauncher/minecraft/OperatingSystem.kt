package ua.besf0r.cubauncher.minecraft

import com.sun.jna.Platform
import kotlinx.serialization.SerialName
import ua.besf0r.cubauncher.javaDir
import ua.besf0r.cubauncher.minecraft.version.MinecraftVersion
import ua.besf0r.cubauncher.minecraft.version.Rule
import java.util.*
import kotlin.io.path.pathString


enum class OperatingSystem {
    @SerialName("windows")
    WINDOWS,

    @SerialName("linux")
    LINUX,

    @SerialName("osx")
    MACOS,
    SOLARIS,

    @SerialName("unknown")
    UNKNOWN;

    companion object {
        val oS: OperatingSystem
            get() {
                val osName = System.getProperty("os.name").lowercase(Locale.getDefault())
                return when {
                    osName.contains("win") -> WINDOWS
                    osName.contains("mac") -> MACOS
                    osName.contains("solaris") || osName.contains("sunos") -> SOLARIS
                    osName.contains("linux") || osName.contains("unix") -> LINUX
                    else -> UNKNOWN
                }
            }
        val osName = when (oS) {
            WINDOWS -> "windows"
            LINUX, SOLARIS -> "linux"
            MACOS -> "osx"
            UNKNOWN -> "unknown"
        }

        fun getJavaKey(version: MinecraftVersion): String {
            val javaVersion = version.javaVersion ?: try {
                val split = version.id!!.split("\\.")
                val minorVersion = split[1].toInt()

                return if (minorVersion >= 17) {
                    "java-runtime-gamma"
                } else {
                    "jre-legacy"
                }
            } catch (ignored: Exception) {
                return "jre-legacy"
            }
            return javaVersion.component!!
        }

        fun getJavaPath(version: MinecraftVersion): String {
            var componentDir = javaDir.resolve(getJavaKey(version))

            if (OperatingSystem.oS == MACOS) {
                componentDir = componentDir.resolve("jre.bundle")
                    .resolve("Contents").resolve("Home")
            }

            return componentDir
                .resolve("bin")
                .resolve(javaType)
                .pathString
        }

        private val arch = if (Platform.is64Bit()) "x64"  else "x86"

        val jreOsName = when (oS) {
            LINUX -> if (arch == "x86") "linux-i386" else "linux"
            WINDOWS -> if (arch == "x86") "windows-x86" else "windows-x64"
            MACOS -> if (arch == "x64") "mac-os" else "mac-os-arm64"

            else -> throw RuntimeException("Unreachable")
        }

        var javaType: String = if (oS == WINDOWS) "javaw.exe" else "java"

        fun List<Rule>?.applyOnThisPlatform(): Boolean {
            var lastAction = Rule.Action.DISALLOW

            if (isNullOrEmpty()) {
                lastAction = Rule.Action.ALLOW
            } else {
                this.forEach {  rule ->
                    val os = rule.os
                    val versionMatches = os?.version?.let {
                        Regex(it).matches(OperatingSystem.osName)
                    } ?: false

                    if (os == null || osName == os.name ||
                        versionMatches || OperatingSystem.arch == os.arch) {
                        lastAction = rule.action!!
                    }
                }
            }
            return lastAction == Rule.Action.ALLOW
        }
    }
}
