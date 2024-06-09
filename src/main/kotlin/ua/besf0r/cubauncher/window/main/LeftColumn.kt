package ua.besf0r.cubauncher.window.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.List
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ua.besf0r.cubauncher.instance.InstanceRunner
import ua.besf0r.cubauncher.instanceManager
import ua.besf0r.cubauncher.settingsManager
import ua.besf0r.cubauncher.window.component.AsyncImage
import ua.besf0r.cubauncher.window.component.loadImageBitmap
import ua.besf0r.cubauncher.workDir
import java.awt.Desktop

@Composable
fun LeftColumn() {
    Box(
        modifier = Modifier
            .requiredWidth(width = 180.dp)
            .requiredHeight(height = 218.dp)
    ) {
        Box(
            modifier = Modifier
                .align(alignment = Alignment.TopStart)
                .offset(y = 61.dp)
                .requiredWidth(width = 180.dp)
                .requiredHeight(height = 156.dp)
                .background(color = settingsManager.settings.currentTheme.panelsColor)
        )
        AsyncImage(
            load = { loadImageBitmap("https://i.imgur.com/142KSyy.png") },
            painterFor = { remember { BitmapPainter(it) } },
            "",
            modifier = Modifier
                .align(alignment = Alignment.TopStart)
                .offset(x = 40.5.dp, y = 67.dp)
                .requiredWidth(width = 100.dp)
                .requiredHeight(height = 113.5.dp)
        )
        Text(
            text = "Cubauncher",
            color = settingsManager.settings.currentTheme.textColor,
            style = TextStyle(fontSize = 16.sp),
            modifier = Modifier
                .align(alignment = Alignment.TopEnd)
                .offset(x = (-42).dp, y = 187.dp)
                .requiredWidth(width = 96.dp)
                .requiredHeight(height = 22.dp)
        )
        Box(
            modifier = Modifier
                .requiredWidth(width = 180.dp)
                .requiredHeight(height = 60.dp)
                .background(color = settingsManager.settings.currentTheme.panelsColor)
        )
    }
    Box(
        modifier = Modifier
            .requiredWidth(width = 180.dp)
            .requiredHeight(height = 193.dp)
            .offset(y = 218.dp)
            .background(color = settingsManager.settings.currentTheme.panelsColor)
    ) {
        val isClickedStart = remember { mutableStateOf(false) }

        if (isClickedStart.value) {
            isClickedStart.value = false

            val selectedInstance = settingsManager.settings.selectedInstance
            if (selectedInstance != null) {
                val instance = instanceManager.getInstanceByName(selectedInstance) ?: return
                InstanceRunner(instance).run()
            }
        }
        TextButton(
            onClick = { isClickedStart.value = true },
            colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent),
            modifier = Modifier
                .align(alignment = Alignment.TopStart)
                .offset(x = 25.dp, y = 29.dp)
                .requiredWidth(width = 130.dp)
                .requiredHeight(height = 30.dp)
        ) {
            Box(
                modifier = Modifier
                    .requiredWidth(width = 130.dp)
                    .requiredHeight(height = 30.dp)
                    .clip(shape = RoundedCornerShape(3.5.dp))
                    .background(color = settingsManager.settings.currentTheme.buttonColor)
            ) {
                Text(
                    text = "Запуск",
                    color = settingsManager.settings.currentTheme.textColor,
                    style = TextStyle(fontSize = 14.5.sp),
                    modifier = Modifier
                        .align(alignment = Alignment.Center)
                        .offset(x = (-5).dp)
                        .requiredWidth(width = 54.dp)
                        .requiredHeight(height = 18.dp)
                )
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = "",
                    modifier = Modifier
                        .align(alignment = Alignment.CenterStart)
                        .offset(x = 10.dp)
                        .requiredWidth(width = 20.dp)
                        .requiredHeight(height = 20.dp),
                    tint = settingsManager.settings.currentTheme.buttonIconColor
                )
            }
        }

        TextButton(
            onClick = {
                val currentInstance = settingsManager.settings.selectedInstance

                val directory = if (currentInstance == null) workDir else{
                    instanceManager.getMinecraftDir(currentInstance)
                }

                val desktop = Desktop.getDesktop()
                desktop.open(directory.toFile())
            },
            colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent),
            modifier = Modifier
                .align(alignment = Alignment.TopStart)
                .offset(x = 25.dp, y = 66.dp)
                .requiredWidth(width = 130.dp)
                .requiredHeight(height = 30.dp)
        ) {
            Box(
                modifier = Modifier
                    .requiredWidth(width = 130.dp)
                    .requiredHeight(height = 30.dp)
                    .clip(shape = RoundedCornerShape(3.5.dp))
                    .background(color = settingsManager.settings.currentTheme.buttonColor)
            ) {
                Text(
                    text = "Директорія",
                    color = settingsManager.settings.currentTheme.textColor,
                    style = TextStyle(fontSize = 14.5.sp),
                    modifier = Modifier
                        .align(alignment = Alignment.Center)
                        .offset(x = 13.dp)
                        .requiredWidth(width = 90.dp)
                        .requiredHeight(height = 18.dp))
                Icon(
                    imageVector = Icons.Outlined.List,
                    contentDescription = "",
                    modifier = Modifier
                        .align(alignment = Alignment.CenterStart)
                        .offset(x = 10.dp)
                        .requiredWidth(width = 20.dp)
                        .requiredHeight(height = 20.dp),
                    tint = settingsManager.settings.currentTheme.buttonIconColor
                )
            }
        }

        TextButton(
            onClick = {
                if (settingsManager.settings.selectedInstance == null) return@TextButton

                instanceManager.deleteInstance(settingsManager.settings.selectedInstance!!)
            },
            colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent),
            modifier = Modifier
                .requiredWidth(width = 130.dp)
                .requiredHeight(height = 30.dp)
                .offset(25.dp,103.dp)
        ) {
            Box(
                modifier = Modifier
                    .requiredWidth(width = 130.dp)
                    .requiredHeight(height = 30.dp)
                    .clip(shape = RoundedCornerShape(3.5.dp))
                    .background(color = Color(0xff464646))
            ) {
                Text(
                    text = "Видалити",
                    color = Color.White,
                    style = TextStyle(fontSize = 14.5.sp),
                    modifier = Modifier
                        .align(alignment = Alignment.Center)
                        .offset(x = 13.dp)
                        .requiredWidth(width = 90.dp)
                        .requiredHeight(height = 18.dp)
                )
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "",
                    modifier = Modifier
                        .align(alignment = Alignment.CenterStart)
                        .offset(x = 10.dp)
                        .requiredWidth(width = 20.dp)
                        .requiredHeight(height = 20.dp),
                    tint = settingsManager.settings.currentTheme.buttonIconColor
                )
            }
        }
    }
}