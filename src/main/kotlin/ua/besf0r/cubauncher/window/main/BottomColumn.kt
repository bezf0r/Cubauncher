package ua.besf0r.cubauncher.window.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.cancel
import ua.besf0r.cubauncher.currentTheme
import ua.besf0r.cubauncher.httpClient
import ua.besf0r.cubauncher.minecraft.version.VersionManifest
import ua.besf0r.cubauncher.window.instance.create.createNewInstanceWindow
import ua.besf0r.cubauncher.window.instance.create.downloadFiles

@Composable
fun bottomColumn(){
    Box(
        modifier = Modifier
            .requiredWidth(width = 720.dp)
            .requiredHeight(height = 85.dp)
            .offset(y = 412.dp)
    ) {
        Box(
            modifier = Modifier
                .requiredWidth(width = 720.dp)
                .requiredHeight(height = 85.dp)
                .background(color = currentTheme.panelsColor))
        TextButton(
            onClick = { },
            colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent),
            modifier = Modifier
                .align(alignment = Alignment.TopStart)
                .offset(x = 562.dp,
                    y = 30.dp)
                .requiredWidth(width = 140.dp)
                .requiredHeight(height = 30.dp)
        ) {
            Box(
                modifier = Modifier
                    .requiredWidth(width = 140.dp)
                    .requiredHeight(height = 30.dp)
            ) {
                Box(
                    modifier = Modifier
                        .requiredWidth(width = 140.dp)
                        .requiredHeight(height = 30.dp)
                        .clip(shape = RoundedCornerShape(4.5.dp))
                        .background(color = currentTheme.buttonColor))
                Text(
                    text = "Аккаунт",
                    color = currentTheme.textColor,
                    style = TextStyle(fontSize = 14.sp),
                    modifier = Modifier
                        .align(alignment = Alignment.Center)
                        .offset(x = 11.5.dp)
                        .requiredWidth(width = 67.dp)
                        .requiredHeight(height = 20.dp))
                Icon(
                    imageVector = Icons.Filled.AccountCircle,
                    tint = currentTheme.buttonIconColor,
                    contentDescription = "",
                    modifier = Modifier
                        .align(alignment = Alignment.CenterStart)
                        .offset(x = 28.dp)
                        .requiredWidth(width = 15.dp)
                        .requiredHeight(height = 15.dp))
            }
        }
        TextButton(
            onClick = { },
            colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent),
            modifier = Modifier
                .align(alignment = Alignment.TopStart)
                .offset(x = 413.dp,
                    y = 31.dp)
                .requiredWidth(width = 140.dp)
                .requiredHeight(height = 30.dp)
        ) {
            Box(
                modifier = Modifier
                    .requiredWidth(width = 140.dp)
                    .requiredHeight(height = 30.dp)
            ) {
                Box(
                    modifier = Modifier
                        .requiredWidth(width = 140.dp)
                        .requiredHeight(height = 30.dp)
                        .clip(shape = RoundedCornerShape(4.5.dp))
                        .background(color = currentTheme.buttonColor))
                Text(
                    text = "Допомога",
                    color = currentTheme.textColor,
                    style = TextStyle(
                        fontSize = 14.sp),
                    modifier = Modifier
                        .align(alignment = Alignment.Center)
                        .offset(x = 3.5.dp)
                        .requiredWidth(width = 69.dp)
                        .requiredHeight(height = 20.dp))
                Icon(
                    imageVector = Icons.Filled.Info,
                    tint = currentTheme.buttonIconColor,
                    contentDescription = "",
                    modifier = Modifier
                        .align(alignment = Alignment.CenterStart)
                        .offset(x = 20.dp)
                        .requiredWidth(width = 15.dp)
                        .requiredHeight(height = 15.dp))
            }
        }
        TextButton(
            onClick = { },
            colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent),
            modifier = Modifier
                .align(alignment = Alignment.TopStart)
                .offset(x = 231.dp,
                    y = 31.dp)
                .requiredWidth(width = 173.dp)
                .requiredHeight(height = 30.dp)
        ) {
            Box(
                modifier = Modifier
                    .requiredWidth(width = 173.dp)
                    .requiredHeight(height = 30.dp)
            ) {
                Box(
                    modifier = Modifier
                        .requiredWidth(width = 173.dp)
                        .requiredHeight(height = 30.dp)
                        .clip(shape = RoundedCornerShape(4.5.dp))
                        .background(color = currentTheme.buttonColor))
                Text(
                    text = "Налаштування",
                    color = currentTheme.textColor,
                    style = TextStyle(
                        fontSize = 14.sp),
                    modifier = Modifier
                        .align(alignment = Alignment.Center)
                        .offset(x = 19.dp)
                        .requiredWidth(width = 128.dp)
                        .requiredHeight(height = 17.dp)
                )
                Icon(
                    imageVector = Icons.Filled.Settings,
                    tint = currentTheme.buttonIconColor,
                    contentDescription = "",
                    modifier = Modifier
                        .align(alignment = Alignment.CenterStart)
                        .offset(x = 18.dp)
                        .requiredSize(size = 18.dp))
            }
        }
        val onNewInstance = remember { mutableStateOf(false) }
        TextButton(
            onClick = { onNewInstance.value = true },
            colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent),
            modifier = Modifier
                .align(alignment = Alignment.TopStart)
                .offset(x = 46.dp, y = 32.dp)
                .requiredWidth(width = 176.dp)
                .requiredHeight(height = 30.dp)
        ) {
            if (onNewInstance.value) createNewInstanceWindow(
                onDismissed = {
                    onNewInstance.value = false
                }, onDownload = {
                    instanceName: MutableState<String?>,
                    selectedVersion: MutableState<VersionManifest.Version?>,
                    isForge: MutableState<Boolean>,
                    modManagerVersion: MutableState<String>
                    ->
                    downloadFiles(instanceName, selectedVersion, isForge,
                        modManagerVersion, onNewInstance)
                }
            )
            Box(
                modifier = Modifier
                    .requiredWidth(width = 176.dp)
                    .requiredHeight(height = 30.dp)
            ) {
                Box(
                    modifier = Modifier
                        .requiredWidth(width = 176.dp)
                        .requiredHeight(height = 30.dp)
                        .clip(shape = RoundedCornerShape(4.5.dp))
                        .background(color = currentTheme.buttonColor))
                Text(
                    text = "Створити збірку",
                    color = Color.White,
                    style = TextStyle(fontSize = 14.sp),
                    modifier = Modifier
                        .align(alignment = Alignment.Center)
                        .offset(x = 4.dp)
                        .requiredWidth(width = 114.dp)
                        .requiredHeight(height = 20.dp)
                )
                Icon(
                    imageVector = Icons.Filled.AddCircle,
                    tint = currentTheme.buttonIconColor,
                    contentDescription = "",
                    modifier = Modifier
                        .align(alignment = Alignment.CenterStart)
                        .offset(x = 12.dp)
                        .requiredWidth(width = 17.dp)
                        .requiredHeight(height = 17.dp))
            }
        }
    }
}