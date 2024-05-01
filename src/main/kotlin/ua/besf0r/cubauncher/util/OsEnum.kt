package ua.besf0r.cubauncher.util

import com.sun.jna.Platform
import kotlinx.serialization.SerialName
import java.util.*

enum class OsEnum {
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
        val oS: OsEnum
            get() {
                val osName = System.getProperty("os.name").lowercase(Locale.getDefault())
                if (osName.contains("win")) return WINDOWS
                if (osName.contains("mac")) return MACOS
                if (osName.contains("solaris") || osName.contains("sunos")) return SOLARIS
                return if (osName.contains("linux") || osName.contains("unix")) LINUX else UNKNOWN
        }
        val osName = when (oS) {
            WINDOWS -> "windows"
            LINUX, SOLARIS -> "linux"
            MACOS -> "osx"
            UNKNOWN -> "unknown"
            else -> "unknown"
        }

        val bits = if (Platform.is64Bit()) {
                "64"
            } else "32"

        val arch = if (Platform.is64Bit()) {
                "x64"
            } else "x86"
        var javaType: String = if (OsEnum.oS == WINDOWS) {
            "javaw.exe"
        } else {
            "java"
        }
    }
}
