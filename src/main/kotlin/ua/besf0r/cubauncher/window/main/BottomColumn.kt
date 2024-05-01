package ua.besf0r.cubauncher.window.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ua.besf0r.cubauncher.currentTheme
import ua.besf0r.cubauncher.window.instance.NewInctance

@Composable
fun bottomColumn(){
    Box(
        Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomEnd
    ){
        Box(Modifier
            .fillMaxWidth()
            .height(30.dp)
            .background(currentTheme.fontColor)
            .align(Alignment.BottomCenter))
        Box(Modifier
            .fillMaxWidth()
            .padding(start = 181.dp, bottom = 30.dp)
            .height(90.dp)
            .background(currentTheme.rightColumnColor),
            contentAlignment = Alignment.CenterStart
        ) {
            Row{
                val onNewInstance = remember { mutableStateOf(false) }
                TextButton(
                    onClick = { onNewInstance.value = true },
                    colors = ButtonDefaults.buttonColors(backgroundColor = currentTheme.rightColumnColor),
                    modifier = Modifier.width(180.dp).height(45.dp)
                ) {
                    if (onNewInstance.value) NewInctance {
                        onNewInstance.value = false
                    }.openNewInstanceWindow()
                    Icon(
                        imageVector = Icons.Filled.AddCircle,
                        modifier = Modifier
                            .size(18.dp),
                        contentDescription = "",
                        tint = currentTheme.buttonIconColor
                    )
                    Text(
                        " Створити збірку", color = currentTheme.textColor, fontSize = 15.sp,
                        modifier = Modifier.weight(1f)
                    )
                }

                TextButton(
                    onClick = {},
                    colors = ButtonDefaults.buttonColors(backgroundColor = currentTheme.rightColumnColor),
                    modifier = Modifier.width(170.dp).height(45.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        modifier = Modifier
                            .size(18.dp),
                        contentDescription = "",
                        tint = currentTheme.buttonIconColor
                    )
                    Text(
                        " Налаштування", color = currentTheme.textColor, fontSize = 15.sp,
                        modifier = Modifier.weight(1f)
                    )
                }

                TextButton(
                    onClick = {},
                    colors = ButtonDefaults.buttonColors(backgroundColor = currentTheme.rightColumnColor),
                    modifier = Modifier.width(130.dp).height(45.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        modifier = Modifier
                            .size(18.dp),
                        contentDescription = "",
                        tint = currentTheme.buttonIconColor
                    )
                    Text(
                        " Допомога", color = currentTheme.textColor, fontSize = 15.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            TextButton(
                onClick = {},
                colors = ButtonDefaults.buttonColors(backgroundColor = currentTheme.rightColumnColor),
                modifier = Modifier.width(130.dp).height(45.dp)
                    .align(Alignment.CenterEnd).padding(end = 15.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.AccountCircle,
                    modifier = Modifier
                        .size(18.dp),
                    contentDescription = "",
                    tint = currentTheme.buttonIconColor
                )
                Text(
                    " Аккаунт", color = currentTheme.textColor, fontSize = 15.sp,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}