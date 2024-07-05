package ua.besf0r.kovadlo.window.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.material.Slider
import androidx.compose.material.SliderColors
import androidx.compose.material.SliderDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sun.management.OperatingSystemMXBean
import ua.besf0r.kovadlo.settings.LauncherSettings
import ua.besf0r.kovadlo.settingsManager
import java.lang.management.ManagementFactory
import kotlin.math.ceil
import kotlin.math.roundToInt

@Composable
fun JavaSector(){
    val mb = 1024 * 1024
    val memorySize = remember {
        (ManagementFactory.getOperatingSystemMXBean() as OperatingSystemMXBean).totalPhysicalMemorySize / mb
    }

    val settings = settingsManager.settings

    val minimumRam = remember { mutableStateOf(settings.minimumRam.toFloat()) }
    val maximumRam = remember { mutableStateOf(settings.maximumRam.toFloat()) }

    Box(
        modifier = Modifier
            .requiredWidth(width = 525.dp)
            .requiredHeight(height = 513.dp)
            .offset(195.dp)
            .background(color = Color(0xff2d2d2d))
    ) {
        Text(
            text = "Оперативна пам’ять:",
            color = Color.White,
            style = TextStyle(fontSize = 18.sp),
            modifier = Modifier
                .align(alignment = Alignment.TopStart)
                .offset(x = 27.dp, y = 78.dp)
                .requiredWidth(width = 229.dp)
                .requiredHeight(height = 25.dp)
        )
        //Minimum
        Text(
            text = "Мінімальна кількість оперативної пам’яті:",
            color = Color.White,
            style = TextStyle(fontSize = 12.sp),
            modifier = Modifier
                .align(alignment = Alignment.TopStart)
                .offset(x = 27.dp, y = 120.dp)
                .requiredWidth(width = 263.dp)
                .requiredHeight(height = 16.dp))
        Slider(
            value = minimumRam.value,
            onValueChange = { minimumRam.value = it},
            onValueChangeFinished = {
                val rounded = roundToBeautifulRamSize(
                    minimumRam.value.toInt(),memorySize.toInt()).toFloat()

                minimumRam.value = rounded
                settings.minimumRam = rounded.roundToInt()

                if (maximumRam.value < minimumRam.value){
                    maximumRam.value = minimumRam.value
                }
            },
            modifier = Modifier
                .align(alignment = Alignment.TopStart)
                .offset(x = 300.dp, y = 105.dp)
                .requiredWidth(width = 215.dp),
            valueRange = 512f..memorySize.toFloat(),
            colors = customSliderColors(settings)
        )
        Text(
            text = "${minimumRam.value.roundToInt()} мб",
            color = Color.White,
            style = TextStyle(fontSize = 12.sp),
            modifier = Modifier
                .requiredWidth(width = 66.dp)
                .requiredHeight(height = 15.dp)
                .offset(x = 450.dp, y = 132.dp)
        )

        //Maximum
        Text(
            text = "Максимальна кількість оперативної пам’яті:",
            color = Color.White,
            style = TextStyle(fontSize = 12.sp),
            modifier = Modifier
                .align(alignment = Alignment.TopStart)
                .offset(x = 27.dp, y = 143.dp)
                .requiredWidth(width = 263.dp)
                .requiredHeight(height = 16.dp)
        )
        Slider(
            value = maximumRam.value,
            onValueChange = { maximumRam.value = it },
            onValueChangeFinished = {
                val rounded = roundToBeautifulRamSize(maximumRam.value.toInt(),
                    memorySize.toInt()).toFloat()

                maximumRam.value = rounded
                settings.maximumRam = rounded.roundToInt()

                if (maximumRam.value < minimumRam.value){
                    minimumRam.value = maximumRam.value
                }
            },
            modifier = Modifier
                .align(alignment = Alignment.TopStart)
                .offset(x = 300.dp, y = 128.dp)
                .requiredWidth(width = 215.dp),
            valueRange = 512f..memorySize.toFloat(),
            colors = customSliderColors(settings)
        )
        Text(
            text = "${maximumRam.value.roundToInt()} мб",
            color = Color.White,
            style = TextStyle(fontSize = 12.sp),
            modifier = Modifier
                .requiredWidth(width = 66.dp)
                .requiredHeight(height = 15.dp)
                .offset(x = 450.dp, y = 155.dp)
        )
    }
}
fun roundToBeautifulRamSize(size: Int, maxMemory: Int): Int {
    val sizeInGB = size / 1024.0
    val roundedGB = ceil(sizeInGB * 2) / 2

    val rounded = (roundedGB * 1024).toInt()

    return if (rounded > maxMemory) maxMemory else rounded
}
@Composable
private fun customSliderColors(settings: LauncherSettings): SliderColors = SliderDefaults.colors(
    activeTickColor = settings.currentTheme.selectedButtonColor,
    inactiveTickColor = settings.currentTheme.panelsColor,
    inactiveTrackColor = settings.currentTheme.textColor,
    activeTrackColor = settings.currentTheme.fontColor,
    thumbColor = settings.currentTheme.textColor
)