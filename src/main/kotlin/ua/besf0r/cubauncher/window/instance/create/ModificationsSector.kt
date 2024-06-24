package ua.besf0r.cubauncher.window.instance.create

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
import ua.besf0r.cubauncher.minecraft.fabric.FabricVersionManifest
import ua.besf0r.cubauncher.minecraft.quilt.QuiltVersionManifest
import ua.besf0r.cubauncher.minecraft.forge.ForgeVersionManifest
import ua.besf0r.cubauncher.minecraft.optifine.OptiFineVersionManifest
import ua.besf0r.cubauncher.settingsManager
import ua.besf0r.cubauncher.window.component.CircularCheckbox
import ua.besf0r.cubauncher.window.component.RenderAsync

@Composable
fun ChangeModsManagerSector(
    screenData: MutableState<CreateInstanceData>
) {
    val modManager = screenData.value.modManager
    val modManagerVersion = screenData.value.modManagerVersion
    val selectedVersion = screenData.value.selectedVersion

    Box(
        modifier = Modifier
            .requiredWidth(width = 500.dp)
            .requiredHeight(height = 180.dp)
            .offset(5.dp, 324.dp)
    ) {
        Box(
            modifier = Modifier
                .requiredWidth(width = 500.dp)
                .requiredHeight(height = 180.dp)
                .background(color = settingsManager.settings.currentTheme.panelsColor)
        )
        Box(
            modifier = Modifier
                .align(alignment = Alignment.TopStart)
                .offset(x = 50.dp, y = 5.dp)
                .requiredWidth(width = 203.dp)
                .requiredHeight(height = 170.dp)
                .clip(shape = RoundedCornerShape(5.dp))
                .background(color = settingsManager.settings.currentTheme.fontColor)
        ) {
            Box(
                modifier = Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(x = 5.6.dp, y = 4.dp)
                    .requiredWidth(width = 191.dp)
                    .requiredHeight(height = 15.dp)
                    .clip(shape = RoundedCornerShape(5.dp))
                    .background(color = settingsManager.settings.currentTheme.panelsColor)
            )
            Text(
                text = "Версія",
                color = settingsManager.settings.currentTheme.textColor,
                style = TextStyle(fontSize = 13.sp),
                modifier = Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(x = 83.dp, y = 3.5.dp)
                    .requiredWidth(width = 51.dp)
                    .requiredHeight(height = 15.dp)
            )

            if (screenData.value.hasOptifine){
                RenderAsync(
                    load = Load@{
                        val optifineVersions = OptiFineVersionManifest.versions
                        return@Load optifineVersions
                    },
                    itemContent = Item@ { optifine ->
                        val forCurrentVersion = optifine.filter { it.mcversion == selectedVersion?.id }
                        if (forCurrentVersion.isEmpty()) return@Item

                        screenData.value = screenData.value.copy(optifineVersion = forCurrentVersion.last().filename )
                    }
                )
            }else{
                screenData.value = screenData.value.copy(optifineVersion = null)
            }

            when (modManager) {
                ModificationManager.WITHOUT -> {
                    ModificationVersionsGrid(listOf(), screenData)
                }

                ModificationManager.FORGE -> {
                    RenderAsync(
                        load = Load@{
                            val forgeVersions = ForgeVersionManifest.versions.versions
                            return@Load forgeVersions
                        },
                        itemContent = { forge ->
                            ModificationVersionsGrid((forge.find {
                                it.first == selectedVersion?.id
                            }?.second ?: listOf()).reversed(), screenData)
                        }
                    )
                }

                ModificationManager.FABRIC -> {
                    RenderAsync(
                        load = Load@{
                            val fabricVersions = FabricVersionManifest.loaderVersion
                            val supportedVersions = FabricVersionManifest.minecraftVersions

                            return@Load Pair(fabricVersions, supportedVersions)
                        },
                        itemContent = { fabric ->
                            val supportedVersions = fabric.second
                            val fabricVersions =
                                if (supportedVersions.find { it.version == selectedVersion?.id } == null) listOf() else fabric.first

                            ModificationVersionsGrid(fabricVersions.map { it.version }, screenData)
                        }
                    )
                }
                ModificationManager.QUILT -> {
                    RenderAsync(
                        load = Load@{
                            val fabricVersions = QuiltVersionManifest.loaderVersion
                            val supportedVersions = QuiltVersionManifest.minecraftVersions

                            return@Load Pair(fabricVersions, supportedVersions)
                        },
                        itemContent = { quilt ->
                            val supportedVersions = quilt.second
                            val quiltVersions =
                                if (supportedVersions.find { it.version == selectedVersion?.id } == null) listOf() else quilt.first

                            ModificationVersionsGrid(quiltVersions.map { it.version }, screenData)
                        }
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .align(alignment = Alignment.TopStart)
                .offset(x = 286.dp, y = 11.dp)
                .requiredWidth(width = 167.dp)
                .requiredHeight(height = 97.dp)
        ) {
            Text(
                text = "Завантажувач модів:",
                color = Color.White,
                style = TextStyle(fontSize = 15.sp),
                modifier = Modifier
                    .requiredWidth(width = 167.dp)
                    .requiredHeight(height = 17.dp)
            )
            Box(
                modifier = Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(y = 26.dp)
                    .requiredWidth(width = 154.dp)
                    .requiredHeight(height = 20.dp)
            ) {
                CircularCheckbox(
                    checked = modManager == ModificationManager.WITHOUT,
                    onCheckedChange = {
                        screenData.value = screenData.value.copy(modManager = ModificationManager.WITHOUT)
                    },
                    modifier = Modifier.align(alignment = Alignment.CenterStart)
                )
                Text(
                    text = "Без завантажуча",
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    style = TextStyle(fontSize = 15.sp),
                    modifier = Modifier
                        .align(alignment = Alignment.CenterStart)
                        .offset(x = 10.dp)
                        .requiredWidth(width = 139.dp)
                        .requiredHeight(height = 20.dp)
                )
            }
            Box(
                modifier = Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(y = 53.dp)
                    .requiredWidth(width = 162.dp)
                    .requiredHeight(height = 20.dp)
            ) {
                CircularCheckbox(
                    checked = modManager == ModificationManager.FORGE,
                    onCheckedChange = {
                        screenData.value = screenData.value.copy(modManager = ModificationManager.FORGE)
                    },
                    modifier = Modifier.align(alignment = Alignment.CenterStart)
                )
                Text(
                    text = "Forge",
                    color = Color.White,
                    style = TextStyle(fontSize = 15.sp),
                    modifier = Modifier
                        .align(alignment = Alignment.CenterStart)
                        .offset(x = 23.dp)
                        .requiredWidth(width = 139.dp)
                        .requiredHeight(height = 20.dp)
                )
            }
            Box(
                modifier = Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(y = 80.dp)
                    .requiredWidth(width = 162.dp)
                    .requiredHeight(height = 20.dp)
            ) {
                CircularCheckbox(
                    checked = modManager == ModificationManager.FABRIC,
                    onCheckedChange = {
                        screenData.value = screenData.value.copy(modManager = ModificationManager.FABRIC)
                    },
                    modifier = Modifier.align(alignment = Alignment.CenterStart)
                )
                Text(
                    text = "Fabric",
                    color = Color.White,
                    style = TextStyle(fontSize = 15.sp),
                    modifier = Modifier
                        .align(alignment = Alignment.CenterStart)
                        .offset(x = 23.dp)
                        .requiredWidth(width = 139.dp)
                        .requiredHeight(height = 20.dp)
                )
            }
            Box(
                modifier = Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(y = 106.dp)
                    .requiredWidth(width = 162.dp)
                    .requiredHeight(height = 20.dp)
            ) {
                CircularCheckbox(
                    checked = modManager == ModificationManager.QUILT,
                    onCheckedChange = {
                        screenData.value = screenData.value.copy(modManager = ModificationManager.QUILT)
                    },
                    modifier = Modifier.align(alignment = Alignment.CenterStart)
                )
                Text(
                    text = "Quilt",
                    color = Color.White,
                    style = TextStyle(
                        fontSize = 15.sp),
                    modifier = Modifier
                        .align(alignment = Alignment.TopStart)
                        .offset(x = 23.dp,
                            y = 0.dp)
                        .requiredWidth(width = 139.dp)
                        .requiredHeight(height = 17.dp))
            }
            Box(
                modifier = Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(x = 79.dp, y = 53.dp)
                    .requiredWidth(width = 59.dp)
                    .requiredHeight(height = 20.dp)
            ) {
                CircularCheckbox(
                    checked = screenData.value.hasOptifine,
                    onCheckedChange = {
                        screenData.value = screenData.value.copy(
                            hasOptifine = !screenData.value.hasOptifine
                        )
                    },
                    modifier = Modifier.align(alignment = Alignment.CenterStart)
                )
                Text(
                    text = "Optifine",
                    color = run Color@ {
                        var color = Color.White
                        RenderAsync(
                            load = Load@{
                                val optifineVersions = OptiFineVersionManifest.versions
                                return@Load optifineVersions
                            },
                            itemContent = Item@ { optifine ->
                                val forCurrentVersion = optifine.filter { it.mcversion == selectedVersion?.id }
                                if (modManager != ModificationManager.FORGE ||
                                    forCurrentVersion.isEmpty() ||
                                    modManagerVersion == null
                                ) {
                                    color = Color.Gray
                                    return@Item
                                }
                                color = Color.White
                            }
                        )
                        return@Color color
                    },
                    style = TextStyle(fontSize = 15.sp),
                    modifier = Modifier
                        .align(alignment = Alignment.CenterStart)
                        .offset(x = 60.dp)
                        .requiredWidth(width = 139.dp)
                        .requiredHeight(height = 20.dp))
            }
        }
    }
}
@Composable
fun ModificationVersionsGrid(
    versions: List<String>,
    screenData: MutableState<CreateInstanceData>,
    onCustomValue: ((name: String) -> Unit)? = null
) {
    val modManagerVersion = screenData.value.modManagerVersion

    LazyVerticalGrid(
        columns = GridCells.Fixed(1),
        horizontalArrangement = Arrangement.spacedBy((-191).dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        content = {
            items(versions) {
                TextButton(
                    onClick = {
                        if (onCustomValue != null) onCustomValue(it)
                        else screenData.value = screenData.value.copy(modManagerVersion = it)
                    },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = if (modManagerVersion == it)
                            settingsManager.settings.currentTheme.selectedButtonColor
                        else Color.Transparent
                    ),
                    modifier = Modifier
                        .requiredWidth(width = 191.dp)
                        .requiredHeight(height = 17.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .requiredWidth(width = 191.dp)
                            .requiredHeight(height = 17.dp)
                    ) {
                        Text(
                            text = it,
                            color = settingsManager.settings.currentTheme.textColor,
                            textAlign = TextAlign.Center,
                            style = TextStyle(fontSize = 13.sp),
                            modifier = Modifier
                                .align(alignment = Alignment.Center)
                                .requiredWidth(width = 125.dp)
                        )
                    }
                }
            }
        },
        modifier = Modifier
            .offset(x = 5.67.dp, y = 24.dp)
            .requiredWidth(width = 190.dp)
            .requiredHeight(height = 140.dp)
    )
}
