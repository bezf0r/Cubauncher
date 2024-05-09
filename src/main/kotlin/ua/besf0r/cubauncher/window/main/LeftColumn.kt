package ua.besf0r.cubauncher.window.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.List
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ua.besf0r.cubauncher.account.OfflineAccount
import ua.besf0r.cubauncher.currentTheme
import ua.besf0r.cubauncher.instance.InstanceRunner
import ua.besf0r.cubauncher.instanceManager
import ua.besf0r.cubauncher.selectedInstance
import java.awt.Desktop

@Composable
fun leftColumn() {
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
                .background(color = currentTheme.panelsColor)
        )
//            Image(
//                painter = painterResource(id = R.drawable.logo1),
//                contentDescription = "logo 1",
//                modifier = Modifier
//                    .align(alignment = Alignment.TopStart)
//                    .offset(x = 40.5.dp,y = 67.dp)
//                    .requiredWidth(width = 100.dp)
//                    .requiredHeight(height = 113.5.dp))
        Text(
            text = "Cubauncher",
            color = currentTheme.textColor,
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
                .background(color = currentTheme.panelsColor)
        )

    }
    Box(
        modifier = Modifier
            .requiredWidth(width = 180.dp)
            .requiredHeight(height = 193.dp)
            .offset(y = 218.dp)
    ) {
        Box(
            modifier = Modifier
                .requiredWidth(width = 180.dp)
                .requiredHeight(height = 193.dp)
                .background(color = currentTheme.panelsColor))
        TextButton(
            onClick = {
                val instance = instanceManager.getInstanceByName(selectedInstance!!)?: return@TextButton
                val desktop = Desktop.getDesktop()
                desktop.open(instanceManager.getMinecraftDir(instance).toFile())
            },
            colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent),
            modifier = Modifier
                .align(alignment = Alignment.TopStart)
                .offset(x = 25.dp, y = 109.dp)
                .requiredWidth(width = 130.dp)
                .requiredHeight(height = 30.dp)
        ) {
            Box(
                modifier = Modifier
                    .requiredWidth(width = 130.dp)
                    .requiredHeight(height = 30.dp)
            ) {
                Box(
                    modifier = Modifier
                        .requiredWidth(width = 130.dp)
                        .requiredHeight(height = 30.dp)
                        .clip(shape = RoundedCornerShape(3.5.dp))
                        .background(color = currentTheme.buttonColor))
                Text(
                    text = "Директорія",
                    color = currentTheme.textColor,
                    style = TextStyle(
                        fontSize = 14.5.sp
                    ),
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
                        tint = currentTheme.buttonIconColor
                    )
                }
            }
            TextButton(
                onClick = { },
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent),
                modifier = Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(x = 25.dp, y = 72.dp)
                    .requiredWidth(width = 130.dp)
                    .requiredHeight(height = 30.dp)
            ) {
                Box(
                    modifier = Modifier
                        .requiredWidth(width = 130.dp)
                        .requiredHeight(height = 30.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .requiredWidth(width = 130.dp)
                            .requiredHeight(height = 30.dp)
                            .clip(shape = RoundedCornerShape(3.5.dp))
                            .background(color = currentTheme.buttonColor))
                    Text(
                        text = "Редагувати",
                        color = currentTheme.textColor,
                        style = TextStyle(
                            fontSize = 14.5.sp
                        ),
                        modifier = Modifier
                            .align(alignment = Alignment.Center)
                            .offset(x = 13.dp)
                            .requiredWidth(width = 90.dp)
                            .requiredHeight(height = 18.dp)
                    )
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "",
                        modifier = Modifier
                            .align(alignment = Alignment.CenterStart)
                            .offset(x = 10.dp)
                            .requiredWidth(width = 20.dp)
                            .requiredHeight(height = 20.dp),
                        tint = currentTheme.buttonIconColor)
                }
            }
        val isClickedStart = remember { mutableStateOf(false) }

        if (isClickedStart.value){
            isClickedStart.value = false
            if (selectedInstance != null) {
                val instance = instanceManager.getInstanceByName(selectedInstance!!)?: return
                InstanceRunner(OfflineAccount("besf0r"), instance).run()
            }
        }
            TextButton(
                onClick = {
                    isClickedStart.value = true
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent),
                modifier = Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(x = 25.dp, y = 35.dp)
                    .requiredWidth(width = 130.dp)
                    .requiredHeight(height = 30.dp)
            ) {
                Box(
                    modifier = Modifier
                        .requiredWidth(width = 130.dp)
                        .requiredHeight(height = 30.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .requiredWidth(width = 130.dp)
                            .requiredHeight(height = 30.dp)
                            .clip(shape = RoundedCornerShape(3.5.dp))
                            .background(color = currentTheme.buttonColor))
                    Text(
                        text = "Запуск",
                        color = currentTheme.textColor,
                        style = TextStyle(
                            fontSize = 14.5.sp
                        ),
                        modifier = Modifier
                            .align(alignment = Alignment.Center)
                            .offset(x = (-5).dp)
                            .requiredWidth(width = 54.dp)
                            .requiredHeight(height = 18.dp))
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = "",
                        modifier = Modifier
                            .align(alignment = Alignment.CenterStart)
                            .offset(x = 10.dp)
                            .requiredWidth(width = 20.dp)
                            .requiredHeight(height = 20.dp),
                        tint = currentTheme.buttonIconColor
                    )
            }
        }
    }
}