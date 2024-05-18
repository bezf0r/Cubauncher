package ua.besf0r.cubauncher.laucnher

import kotlinx.serialization.Serializable
import ua.besf0r.cubauncher.window.theme.ThemeData
import ua.besf0r.cubauncher.window.theme.UiTheme

@Serializable
data class LauncherSettings (
    var minimumRam: Int = 512,
    var maximumRam: Int = 2048,
    var selectedInstance: String? = null,
    var selectedAccount: String? = null,
    val currentTheme: ThemeData = UiTheme.dark
)