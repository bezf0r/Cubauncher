package ua.besf0r.kovadlo.settings

import kotlinx.serialization.Serializable
import ua.besf0r.kovadlo.window.theme.ThemeData
import ua.besf0r.kovadlo.window.theme.WindowTheme

@Serializable
data class LauncherSettings (
    var minimumRam: Int = 512,
    var maximumRam: Int = 2048,
    var selectedInstance: String? = null,
    var selectedAccount: String? = null,
    val currentTheme: ThemeData = WindowTheme.dark
)