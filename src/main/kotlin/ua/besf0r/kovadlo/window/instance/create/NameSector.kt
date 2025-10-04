package ua.besf0r.kovadlo.window.instance.create

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.kodein.di.DI
import ua.besf0r.kovadlo.AppStrings
import ua.besf0r.kovadlo.settingsManager
import ua.besf0r.kovadlo.window.component.AsyncImage
import ua.besf0r.kovadlo.window.component.loadImageBitmap

@Composable
fun ChangeNameSector(
    di: DI,
    screenData: MutableState<CreateInstanceData>
) {
    val instanceName = screenData.value.instanceName
    val selectedVersion = screenData.value.selectedVersion

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
                .background(color = di.settingsManager().settings.currentTheme.panelsColor)
        )
        Text(
            text = AppStrings.get("create_instance_screen.name_of_instance"),
            color = di.settingsManager().settings.currentTheme.textColor,
            style = TextStyle(fontSize = 16.sp),
            modifier = Modifier
                .align(alignment = Alignment.TopStart)
                .offset(x = 32.dp, y = 15.dp)
        )

        val maxLength = 30
        TextField(
            value = instanceName ?: selectedVersion?.id ?: " ",
            onValueChange = {
                if (it.length <= maxLength)
                    screenData.value = screenData.value.copy(instanceName = it)
            },
            label = {},
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = di.settingsManager().settings.currentTheme.focusedBorderColor,
                unfocusedBorderColor = di.settingsManager().settings.currentTheme.unfocusedBorderColor,
                textColor = di.settingsManager().settings.currentTheme.textColor,
                disabledTextColor = di.settingsManager().settings.currentTheme.textColor,
                focusedLabelColor = di.settingsManager().settings.currentTheme.textColor,
                unfocusedLabelColor = di.settingsManager().settings.currentTheme.textColor
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
fun IconSector(
    di: DI
) {
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
                .background(color = di.settingsManager().settings.currentTheme.panelsColor)
        ) {
            AsyncImage(
                load = { loadImageBitmap("https://cdn.apexminecrafthosting.com/img/uploads/2021/12/06173101/crafting-table.png") },
                painterFor = { remember { BitmapPainter(it) } },
                "",
                modifier = Modifier
                    .align(alignment = Alignment.Center)
                    .requiredSize(size = 75.dp)
            )
        }
    }
}