package ua.besf0r.cubauncher.window.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.List
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ua.besf0r.cubauncher.currentTheme

@Composable
fun leftColumn() {
    var isClosed by remember { mutableStateOf(false) }

    Box(
        Modifier.height(200.dp).width(180.dp)
        .background(currentTheme.rightColumnColor),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            //cubauncher logo
            Box(Modifier.width(145.dp).height(145.dp).padding(top = 10.dp)) {
//                Image(
//                    logo,
//                    "",
//                    modifier = Modifier.align(Alignment.Center)
//                )
            }
            Text(
                "Cubauncher", fontSize = 15.sp, color = currentTheme.textColor
            )
        }
    }
    Box(
        Modifier.width(180.dp).padding(top = 201.dp).fillMaxHeight()
            .background(currentTheme.rightColumnColor),
        contentAlignment = Alignment.TopEnd
    ) {
        Column(Modifier.padding(top = 30.dp)) {
            //кнопка запуску
            TextButton(
                onClick = {},
                colors = ButtonDefaults.buttonColors(backgroundColor = currentTheme.rightColumnColor),
                modifier = Modifier.width(170.dp).height(45.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    modifier = Modifier
                        .size(18.dp),
                    contentDescription = "",
                    tint = currentTheme.buttonIconColor
                )
                Text(
                    " Запустити", color = currentTheme.textColor, fontSize = 15.sp,
                    modifier = Modifier.weight(1f)
                )
            }

            TextButton(
                onClick = {
                   isClosed = true
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = currentTheme.rightColumnColor),
                modifier = Modifier.width(170.dp).height(45.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    modifier = Modifier
                        .size(18.dp),
                    contentDescription = "",
                    tint = currentTheme.buttonIconColor
                )
                Text(
                    " Зупинити", color = currentTheme.textColor, fontSize = 15.sp,
                    modifier = Modifier.weight(1f)
                )
            }


            //Змінити збірку
            TextButton(
                onClick = {},
                colors = ButtonDefaults.buttonColors(backgroundColor = currentTheme.rightColumnColor),
                modifier = Modifier.width(170.dp).height(45.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    modifier = Modifier
                        .size(18.dp),
                    contentDescription = "",
                    tint = currentTheme.buttonIconColor
                )
                Text(
                    " Змінити збірку", color = currentTheme.textColor, fontSize = 15.sp,
                    modifier = Modifier.weight(1f)
                )
            }

            TextButton(
                onClick = {},
                colors = ButtonDefaults.buttonColors(backgroundColor = currentTheme.rightColumnColor),
                modifier = Modifier.width(170.dp).height(45.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.List,
                    modifier = Modifier
                        .size(18.dp),
                    contentDescription = "",
                    tint = currentTheme.buttonIconColor
                )
                Text(
                    " Папка", color = currentTheme.textColor, fontSize = 15.sp,
                    modifier = Modifier.weight(1f)
                )
            }
       }
    }
}