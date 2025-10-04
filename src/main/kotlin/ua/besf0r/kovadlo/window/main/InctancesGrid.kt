package ua.besf0r.kovadlo.window.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
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
import org.kodein.di.DI
import ua.besf0r.kovadlo.instanceManager
import ua.besf0r.kovadlo.settingsManager
import ua.besf0r.kovadlo.window.component.AsyncImage
import ua.besf0r.kovadlo.window.component.loadImageBitmap

@Composable
fun InstancesGrid(
    di: DI
) {
    val instances = di.instanceManager().instances

    val selectedInstance = remember { mutableStateOf(
        di.settingsManager().settings.selectedInstance
    ) }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .offset(220.dp, 44.dp)
            .requiredWidth(width = 464.dp)
            .requiredHeight(height = 347.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        items(instances.map { it.name }) {instance ->
            InstanceGrid(di, selectedInstance, instance)
        }
    }
}

@Composable
private fun InstanceGrid(
    di: DI,
    selectedInstance: MutableState<String?>,
    instance: String,
) {
    Box(
        Modifier
        .requiredWidth(width = 210.dp)
        .requiredHeight(height = 55.dp)
    ) {
        TextButton(
            onClick = {
                selectedInstance.value = instance
                di.settingsManager().settings.selectedInstance = selectedInstance.value
            },
            modifier = Modifier
                .align(alignment = Alignment.TopStart)
                .offset(x = 12.dp, y = 12.5.dp)
                .requiredWidth(width = 210.dp)
                .requiredHeight(height = 55.dp)
        ) {
            Box(
                modifier = Modifier
                    .requiredWidth(width = 210.dp)
                    .requiredHeight(height = 55.dp)
            ) {
                Box(
                    modifier = Modifier
                        .align(alignment = Alignment.TopStart)
                        .offset(x = 60.dp)
                        .requiredWidth(width = 150.dp)
                        .requiredHeight(height = 55.dp)
                        .clip(shape = RoundedCornerShape(3.5.dp))
                        .background(
                            color = if (selectedInstance.value == instance)
                                di.settingsManager().settings.currentTheme.selectedButtonColor
                            else Color(0xff464646)
                        )
                )
                Box(
                    modifier = Modifier
                        .requiredWidth(width = 59.dp)
                        .requiredHeight(height = 55.dp)
                        .background(
                            color = if (selectedInstance.value == instance)
                                di.settingsManager().settings.currentTheme.selectedButtonColor
                            else Color(0xff464646)
                        )
                ) {
                    AsyncImage(
                        load = { loadImageBitmap("https://cdn.apexminecrafthosting.com/img/uploads/2021/12/06173101/crafting-table.png") },
                        painterFor = { remember { BitmapPainter(it) } },
                        "",
                        modifier = Modifier
                            .align(alignment = Alignment.Center)
                            .requiredSize(size = 45.dp)
                    )
                }
                Text(
                    text = instance,
                    color = di.settingsManager().settings.currentTheme.textColor,
                    style = TextStyle(fontSize = 12.sp),
                    modifier = Modifier
                        .align(alignment = Alignment.TopStart)
                        .offset(x = 65.5.dp, y = 7.5.dp)
                        .requiredWidth(width = 138.dp)
                        .requiredHeight(height = 40.dp)
                )
            }
        }
    }
}