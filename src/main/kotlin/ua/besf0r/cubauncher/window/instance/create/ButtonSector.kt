package ua.besf0r.cubauncher.window.instance.create

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ua.besf0r.cubauncher.laucnher.SettingsManager
import ua.besf0r.cubauncher.settingsManager

@Composable
fun ButtonsSector(
    canStartDownload: MutableState<Boolean>,
    onDismissRequest: () -> Unit
) {
    Box(
        modifier = Modifier
            .requiredWidth(width = 206.dp)
            .requiredHeight(height = 90.dp)
            .offset(509.dp, 324.dp)
            .background(color = Color(0xff2d2d2d))
    )
    TextButton(
        onClick = {
            canStartDownload.value = true
        },
        modifier = Modifier
            .requiredWidth(width = 176.dp)
            .requiredHeight(height = 30.dp)
            .offset(520.dp, 429.dp)
    ) {
        Box(
            modifier = Modifier
                .requiredWidth(width = 176.dp)
                .requiredHeight(height = 30.dp)
                .clip(shape = RoundedCornerShape(5.dp))
                .background(color = Color(0xff464646))
        ) {
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
                contentDescription = "",
                tint = settingsManager.settings.currentTheme.buttonIconColor,
                modifier = Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(x = 12.dp, y = 6.dp)
                    .requiredWidth(width = 17.dp)
                    .requiredHeight(height = 17.dp)
            )
        }
    }
    TextButton(
        onClick = { onDismissRequest() },
        modifier = Modifier
            .requiredWidth(width = 176.dp)
            .requiredHeight(height = 30.dp)
            .offset(520.dp, 467.dp)
    ) {
        Box(
            modifier = Modifier
                .requiredWidth(width = 176.dp)
                .requiredHeight(height = 30.dp)
                .clip(shape = RoundedCornerShape(5.dp))
                .background(color = Color(0xff464646))
        ) {
            Text(
                text = "Закрити",
                color = Color.White,
                style = TextStyle(fontSize = 14.sp),
                modifier = Modifier
                    .align(alignment = Alignment.Center)
                    .offset(x = 4.dp)
                    .requiredWidth(width = 114.dp)
                    .requiredHeight(height = 20.dp)
            )
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "",
                tint = settingsManager.settings.currentTheme.buttonIconColor,
                modifier = Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(x = 12.dp, y = 6.dp)
                    .requiredWidth(width = 17.dp)
                    .requiredHeight(height = 17.dp)
            )
        }
    }
}