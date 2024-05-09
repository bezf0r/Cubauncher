package ua.besf0r.cubauncher.window.instance.create

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ua.besf0r.cubauncher.currentTheme
import ua.besf0r.cubauncher.minecraft.version.VersionManifest

@Composable
fun changeNameSector(
    instanceName: MutableState<String?>,
    selectedVersion: MutableState<VersionManifest.Version?>
) {
    Box(
        modifier = Modifier
            .requiredWidth(width = 605.dp)
            .requiredHeight(height = 100.dp)
            .offset(110.dp, 25.dp)
    ) {
        Box(
            modifier = Modifier
                .requiredWidth(width = 605.dp)
                .requiredHeight(height = 100.dp)
                .background(color = currentTheme.panelsColor)
        )
        Text(
            text = "Назва збірки:",
            color = currentTheme.textColor,
            style = TextStyle(fontSize = 16.sp),
            modifier = Modifier
                .align(alignment = Alignment.TopStart)
                .offset(x = 32.dp, y = 15.dp)
        )

        val maxLength = 30
        TextField(
            value = instanceName.value ?: selectedVersion.value?.id ?: " ",
            onValueChange = { if (it.length <= maxLength) instanceName.value = it },
            label = {},
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = currentTheme.focusedBorderColor,
                unfocusedBorderColor = currentTheme.unfocusedBorderColor,
                textColor = currentTheme.textColor,
                disabledTextColor = currentTheme.textColor,
                focusedLabelColor = currentTheme.textColor,
                unfocusedLabelColor = currentTheme.textColor
            ),
            textStyle = TextStyle(fontSize = 15.sp),
            modifier = Modifier
                .align(alignment = Alignment.TopStart)
                .offset(x = 18.dp, y = 35.dp)
                .requiredWidth(width = 520.dp)
                .requiredHeight(height = 50.dp)
        )
    }
}
@Composable
fun iconButton() {
    TextButton(
        onClick = { },
        colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent),
        modifier = Modifier
            .requiredSize(size = 100.dp)
            .offset(5.dp, 25.dp)
    ) {
        Box(
            modifier = Modifier
                .requiredSize(size = 100.dp)
        ) {
            Box(
                modifier = Modifier
                    .requiredSize(size = 100.dp)
                    .background(color = currentTheme.panelsColor)
            )
            //                    Image(
            //                        painter = painterResource(id = R.drawable.minecrafticon2048x20483ifq7gy71),
            //                        contentDescription = "minecraft-icon-2048x2048-3ifq7gy7 1",
            //                        modifier = Modifier
            //                            .align(alignment = Alignment.TopStart)
            //                            .offset(x = 5.dp,
            //                                y = 5.dp)
            //                            .requiredSize(size = 90.dp))
        }
    }
}