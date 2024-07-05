package ua.besf0r.kovadlo.window.instance.create

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ua.besf0r.kovadlo.minecraft.minecraft.MinecraftVersionList
import ua.besf0r.kovadlo.minecraft.minecraft.VersionManifest
import ua.besf0r.kovadlo.settingsManager
import ua.besf0r.kovadlo.window.component.CircularCheckbox
import ua.besf0r.kovadlo.window.component.RenderAsync
import java.text.SimpleDateFormat

@Composable
fun ChangeVersionSector(
    versionType: String,
    screenData: MutableState<CreateInstanceData>
) {
    val isRelease = screenData.value.isRelease

    Box(
        modifier = Modifier
            .requiredWidth(width = 710.dp)
            .requiredHeight(height = 191.dp)
            .offset(5.dp, 129.dp)
    ) {
        Box(
            modifier = Modifier
                .requiredWidth(width = 710.dp)
                .requiredHeight(height = 191.dp)
                .background(color = settingsManager.settings.currentTheme.panelsColor)
        )
        Box(
            modifier = Modifier
                .align(alignment = Alignment.TopStart)
                .offset(x = 50.dp, y = 5.dp)
                .requiredWidth(width = 450.dp)
                .requiredHeight(height = 181.dp)
        ) {
            Box(
                modifier = Modifier
                    .requiredWidth(width = 450.dp)
                    .requiredHeight(height = 181.dp)
                    .clip(shape = RoundedCornerShape(5.dp))
                    .background(color = settingsManager.settings.currentTheme.fontColor)
            )
            Box(
                modifier = Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(x = 5.dp, y = 5.dp)
                    .requiredWidth(width = 440.dp)
                    .requiredHeight(height = 15.dp)
                    .clip(shape = RoundedCornerShape(5.dp))
                    .background(color = settingsManager.settings.currentTheme.panelsColor)
            )
            Text(
                text = "Дата виходу",
                color = settingsManager.settings.currentTheme.textColor,
                style = TextStyle(fontSize = 13.sp),
                modifier = Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(x = 297.dp, y = 4.dp)
                    .requiredWidth(width = 81.dp)
                    .requiredHeight(height = 16.dp)
            )
            Text(
                text = "Версія",
                color = settingsManager.settings.currentTheme.textColor,
                style = TextStyle(fontSize = 13.sp),
                modifier = Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(x = 73.dp, y = 4.dp)
                    .requiredWidth(width = 45.dp)
                    .requiredHeight(height = 16.dp)
            )

            RenderAsync(
                load = Load@ {
                    return@Load MinecraftVersionList.versions.filter { it.type == versionType }
                },
                itemContent = { versions ->
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(1),
                        horizontalArrangement = Arrangement.spacedBy((-440).dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        content = {
                            items(versions) {
                                MinecraftVersion(it, screenData)
                            }
                        },
                        modifier = Modifier
                            .align(alignment = Alignment.TopStart)
                            .offset(x = 5.dp, y = 26.dp)
                            .requiredWidth(width = 440.dp)
                            .requiredHeight(height = 148.dp)
                    )
                }
            )
        }
        Box(
            modifier = Modifier
                .requiredWidth(width = 81.dp)
                .requiredHeight(height = 78.dp)
                .offset(542.dp, 13.dp)
        ) {
            Text(
                text = "Фільтр:",
                color = Color.White,
                style = TextStyle(
                    fontSize = 15.sp
                ),
                modifier = Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(x = 27.dp)
            )
            Box(
                modifier = Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(y = 60.dp)
                    .requiredWidth(width = 72.dp)
                    .requiredHeight(height = 18.dp)
            ) {
                CircularCheckbox(
                    checked = !isRelease,
                    onCheckedChange = {
                        screenData.value = screenData.value.copy(isRelease = !isRelease)
                    },
                    modifier = Modifier
                        .align(alignment = Alignment.TopStart)
                        .offset(y = 3.dp),
                )
                Text(
                    text = "Знімки",
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    style = TextStyle(
                        fontSize = 15.sp
                    ),
                    modifier = Modifier
                        .align(alignment = Alignment.TopStart)
                        .offset(x = 21.dp)
                )
            }
            Box(
                modifier = Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(y = 35.dp)
                    .requiredWidth(width = 68.dp)
                    .requiredHeight(height = 18.dp)
            ) {
                Text(
                    text = "Релізи",
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    style = TextStyle(
                        fontSize = 15.sp
                    ),
                    modifier = Modifier
                        .align(alignment = Alignment.TopStart)
                        .offset(x = 21.dp)
                )

                CircularCheckbox(
                    checked = isRelease,
                    onCheckedChange = {
                        screenData.value = screenData.value.copy(isRelease = !isRelease)
                    },
                    modifier = Modifier
                        .align(alignment = Alignment.TopStart)
                        .offset(y = 3.dp),
                )
            }
        }
    }
}

@Composable
private fun MinecraftVersion(
    version: VersionManifest.Version,
    screenData: MutableState<CreateInstanceData>
) {
    val selectedVersion = screenData.value.selectedVersion

    TextButton(
        onClick = {
            screenData.value = screenData.value.copy(selectedVersion = version)
        },
        colors = ButtonDefaults.buttonColors(
            backgroundColor =
            if (selectedVersion == version)
                settingsManager.settings.currentTheme.selectedButtonColor
            else Color.Transparent
        ),
        modifier = Modifier
            .requiredWidth(width = 440.dp)
            .requiredHeight(height = 16.dp)
    ) {
        Box(
            modifier = Modifier
                .requiredWidth(width = 440.dp)
                .requiredHeight(height = 16.dp)
        ) {
            Text(
                text = version.id,
                color = settingsManager.settings.currentTheme.textColor,
                textAlign = TextAlign.Center,
                style = TextStyle(fontSize = 13.sp),
                modifier = Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(x = 25.dp)
                    .requiredWidth(width = 125.dp)
            )

            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX")
            val date = inputFormat.parse(version.releaseTime)

            val outputFormat = SimpleDateFormat("dd.MM.yyyy")
            val formattedDate = outputFormat.format(date)
            Text(
                text = formattedDate,
                color = settingsManager.settings.currentTheme.textColor,
                textAlign = TextAlign.Center,
                style = TextStyle(fontSize = 13.sp),
                modifier = Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(x = 267.dp)
                    .requiredWidth(width = 125.dp)
            )
        }
    }
}