package ua.besf0r.cubauncher.window.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.rememberWindowState
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import ua.besf0r.cubauncher.settingsManager
import ua.besf0r.cubauncher.window.createMainTitleBar

@Composable
fun settingWindow(currentLog: MutableState<String>, onDismiss: () -> Unit) {
    val windowState = rememberWindowState(size = DpSize(720.dp, 512.dp))

    val selectedSection = remember { mutableStateOf(SettingsSection.CONSOLE) }

    Window(
        state = windowState,
        resizable = false,
        undecorated = true,
        onCloseRequest = { onDismiss() }
    ) {
        Box(
            modifier = Modifier.fillMaxSize().background(
                settingsManager.settings.currentTheme.fontColor
            )
        )

        Box(
            modifier = Modifier
                .requiredWidth(width = 190.dp)
                .requiredHeight(height = 513.dp)
        ) {
            Box(
                modifier = Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(y = 473.dp)
                    .requiredWidth(width = 190.dp)
                    .requiredHeight(height = 40.dp)
                    .background(color = Color(0xff2d2d2d))
            )
            Box(
                modifier = Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(y = 41.dp)
                    .requiredWidth(width = 190.dp)
                    .requiredHeight(height = 431.dp)
                    .background(color = Color(0xff2d2d2d))
            )
            Box(
                modifier = Modifier
                    .requiredWidth(width = 190.dp)
                    .requiredHeight(height = 40.dp)
                    .background(color = Color(0xff2d2d2d))
            )
            TextButton(
                onClick = {
                    selectedSection.value = SettingsSection.CONSOLE
                },
                modifier = Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(y = 83.dp)
                    .requiredWidth(width = 190.dp)
                    .requiredHeight(height = 35.dp)
            ) {
                Box(
                    modifier = Modifier
                        .requiredWidth(width = 190.dp)
                        .requiredHeight(height = 35.dp)
                        .background(selectedSection.generateColorForButton(SettingsSection.CONSOLE))
                ) {
                    Text(
                        text = "Консоль",
                        color = Color.White,
                        style = TextStyle(fontSize = 18.sp),
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .offset(x = 62.dp, y = 6.dp)
                            .wrapContentHeight(align = Alignment.CenterVertically)
                    )
                    KamelImage(
                        resource = asyncPainterResource("https://www.iconsdb.com/icons/preview/white/console-xxl.png"),
                        contentDescription = "",
                        modifier = Modifier
                            .align(alignment = Alignment.CenterStart)
                            .offset(x = 38.dp)
                            .requiredSize(size = 20.dp),
                        colorFilter = ColorFilter.tint(settingsManager.settings.currentTheme.textColor)
                    )
                }
            }

            TextButton(
                onClick = {
                    selectedSection.value = SettingsSection.JAVA
                },
                modifier = Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(y = 125.dp)
                    .requiredWidth(width = 190.dp)
                    .requiredHeight(height = 35.dp)
            ) {
                Box(
                    modifier = Modifier
                        .requiredWidth(width = 190.dp)
                        .requiredHeight(height = 35.dp)
                        .background(selectedSection.generateColorForButton(SettingsSection.JAVA))
                ) {
                    Text(
                        text = "Java",
                        color = Color.White,
                        style = TextStyle(fontSize = 18.sp),
                        modifier = Modifier
                            .align(alignment = Alignment.Center)
                            .offset(x = (-13.5).dp)
                            .wrapContentHeight(align = Alignment.CenterVertically)
                    )
                    KamelImage(
                        resource = asyncPainterResource("https://www.stimulsoft.com/images/products/reports-java/description/java.png"),
                        contentDescription = "",
                        modifier = Modifier
                            .align(alignment = Alignment.CenterStart)
                            .offset(x = 38.dp)
                            .requiredSize(20.dp),
                        colorFilter = ColorFilter.tint(settingsManager.settings.currentTheme.textColor)
                    )
                }
            }
            TextButton(
                onClick = {
                    selectedSection.value = SettingsSection.ACCOUNT
                },
                modifier = Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(y = 166.dp)
                    .requiredWidth(width = 190.dp)
                    .requiredHeight(height = 35.dp)
            ) {
                Box(
                    modifier = Modifier
                        .requiredWidth(width = 190.dp)
                        .requiredHeight(height = 35.dp)
                        .background(selectedSection.generateColorForButton(SettingsSection.ACCOUNT))
                ) {
                    Text(
                        text = "Аккаунт",
                        color = Color.White,
                        style = TextStyle(fontSize = 18.sp),
                        modifier = Modifier
                            .align(alignment = Alignment.Center)
                            .wrapContentHeight(align = Alignment.CenterVertically)
                    )
                    Icon(
                        imageVector = Icons.Filled.AccountCircle,
                        contentDescription = "",
                        modifier = Modifier
                            .align(alignment = Alignment.CenterStart)
                            .offset(x = 38.dp)
                            .requiredSize(size = 20.dp),
                        tint = settingsManager.settings.currentTheme.textColor
                    )
                }
            }
        }
        when (selectedSection.value) {
            SettingsSection.CONSOLE -> {
                consoleSector(currentLog)
            }

            SettingsSection.JAVA -> {
                javaSector()
            }

            SettingsSection.ACCOUNT -> {}
        }

        createMainTitleBar(windowState) { onDismiss() }
    }
}

fun MutableState<SettingsSection>.generateColorForButton(section: SettingsSection): Color {
    return if (value == section)
        settingsManager.settings.currentTheme.selectedButtonColor
    else Color.Transparent
}

enum class SettingsSection { CONSOLE, JAVA, ACCOUNT }
