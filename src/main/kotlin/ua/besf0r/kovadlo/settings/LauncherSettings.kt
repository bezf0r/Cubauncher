package ua.besf0r.kovadlo.settings

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import kotlinx.serialization.Serializable
import ua.besf0r.kovadlo.window.MutableStateSerializer
import ua.besf0r.kovadlo.window.theme.ThemeData
import ua.besf0r.kovadlo.window.theme.WindowTheme

@Serializable
data class LauncherSettings(
    var minimumRam: Int = 512,
    var maximumRam: Int = 2048,
    var selectedInstance: String? = null,
    @Serializable(with = MutableStateSerializer::class)
    var selectedAccount: MutableState<String?> = mutableStateOf(null),
    val currentTheme: ThemeData = WindowTheme.dark
)