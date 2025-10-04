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
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import ua.besf0r.kovadlo.AppStrings
import ua.besf0r.kovadlo.minecraft.fabric.FabricInstaller
import ua.besf0r.kovadlo.minecraft.fabric.FabricVersionList
import ua.besf0r.kovadlo.minecraft.forge.ForgeInstaller
import ua.besf0r.kovadlo.minecraft.quilt.QuiltVersionList
import ua.besf0r.kovadlo.minecraft.forge.ForgeVersionList
import ua.besf0r.kovadlo.minecraft.liteloader.LiteLoaderInstaller
import ua.besf0r.kovadlo.minecraft.liteloader.LiteLoaderVersionList
import ua.besf0r.kovadlo.minecraft.optifine.OptiFineVersionList
import ua.besf0r.kovadlo.minecraft.quilt.QuiltInstaller
import ua.besf0r.kovadlo.settingsManager
import ua.besf0r.kovadlo.window.component.CircularCheckbox
import ua.besf0r.kovadlo.window.component.RenderAsync

@Composable
fun ChangeModsManagerSector(
    di: DI,
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
                .background(color = di.settingsManager().settings.currentTheme.panelsColor)
        )
        Box(
            modifier = Modifier
                .align(alignment = Alignment.TopStart)
                .offset(x = 50.dp, y = 5.dp)
                .requiredWidth(width = 203.dp)
                .requiredHeight(height = 170.dp)
                .clip(shape = RoundedCornerShape(5.dp))
                .background(color = di.settingsManager().settings.currentTheme.fontColor)
        ) {
            Box(
                modifier = Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(x = 5.6.dp, y = 4.dp)
                    .requiredWidth(width = 191.dp)
                    .requiredHeight(height = 15.dp)
                    .clip(shape = RoundedCornerShape(5.dp))
                    .background(color = di.settingsManager().settings.currentTheme.panelsColor)
            )
            Text(
                text = AppStrings.get("create_instance_screen.modification_version"),
                color = di.settingsManager().settings.currentTheme.textColor,
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
                        val optifineVersions: OptiFineVersionList = di.direct.instance()
                        return@Load optifineVersions.versions
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

            when(modManager){
                is ForgeInstaller -> {
                    RenderAsync(load = Load@{
                        val forgeVersions: ForgeVersionList = di.direct.instance()
                        return@Load (forgeVersions.versions.versions[selectedVersion?.id] ?: listOf()).reversed()
                    }, itemContent = { ModificationVersionsGrid(di, it, screenData) })
                }
                is FabricInstaller -> {
                    RenderAsync(
                        load = Load@{
                            val fabricVersions = di.direct.instance<FabricVersionList>().loaderVersion
                            val supportedVersions = di.direct.instance<FabricVersionList>().minecraftVersions
                            val isSupported = supportedVersions.find { it.version == selectedVersion?.id } != null

                            return@Load if (isSupported) {
                                fabricVersions.map { it.version }
                            } else {
                                listOf()
                            }
                        },itemContent = { ModificationVersionsGrid(di, it, screenData) })
                }
                is QuiltInstaller -> {
                    RenderAsync(load = Load@{
                        val quiltVersions = di.direct.instance<QuiltVersionList>().loaderVersion
                        val supportedVersions = di.direct.instance<QuiltVersionList>().minecraftVersions
                        val isSupported = supportedVersions.find { it.version == selectedVersion?.id } != null

                        return@Load if (isSupported) {
                            quiltVersions.map { it.version }
                        } else {
                            listOf()
                        }
                    }, itemContent = { ModificationVersionsGrid(di, it, screenData) })
                }
                is LiteLoaderInstaller -> {
                    RenderAsync(load = Load@{
                        val liteLoaderVersions = di.direct.instance<LiteLoaderVersionList>().versions
                        return@Load liteLoaderVersions.filter { it.inheritsFrom == selectedVersion?.id }
                            .map { it.version }
                    }, itemContent = { ModificationVersionsGrid(di, it, screenData) })
                }
                else -> { }
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
                text = AppStrings.get("create_instance_screen.modification_name"),
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
                    di,
                    checked = modManager == null,
                    onCheckedChange = {
                        screenData.value = screenData.value.copy(modManager = null)
                    },
                    modifier = Modifier.align(alignment = Alignment.CenterStart)
                )
                Text(
                    text = AppStrings.get("create_instance_screen.without_modification"),
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
                    di,
                    checked = modManager is ForgeInstaller,
                    onCheckedChange = {
                        screenData.value = screenData.value.copy(modManager = ForgeInstaller(di))
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
                    di,
                    checked = modManager is FabricInstaller,
                    onCheckedChange = {
                        screenData.value = screenData.value.copy(modManager = FabricInstaller(di))
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
                    di,
                    checked = modManager is QuiltInstaller,
                    onCheckedChange = {
                        screenData.value = screenData.value.copy(modManager = QuiltInstaller(di))
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
                        .offset(x = 23.dp)
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
                    di,
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
                                val optifineVersions = di.direct.instance<OptiFineVersionList>().versions
                                return@Load optifineVersions
                            },
                            itemContent = Item@ { optifine ->
                                val forCurrentVersion = optifine.filter { it.mcversion == selectedVersion?.id }
                                if (modManager !is LiteLoaderInstaller ||
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
            Box(
                modifier = Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(x = 0.dp,
                        y = 130.dp)
                    .requiredWidth(width = 162.dp)
                    .requiredHeight(height = 17.dp)
            ) {
                CircularCheckbox(
                    di,
                    checked = modManager is LiteLoaderInstaller,
                    onCheckedChange = {
                        screenData.value = screenData.value.copy(modManager = LiteLoaderInstaller(di))
                    },
                    modifier = Modifier.align(alignment = Alignment.CenterStart)
                )
                Text(
                    text = "Liteloader",
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
        }
    }
}
@Composable
fun ModificationVersionsGrid(
    di: DI,
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
                            di.settingsManager().settings.currentTheme.selectedButtonColor
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
                            color = di.settingsManager().settings.currentTheme.textColor,
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
