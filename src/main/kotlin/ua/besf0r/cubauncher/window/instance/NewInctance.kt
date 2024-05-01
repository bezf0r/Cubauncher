package ua.besf0r.cubauncher.window.instance

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogState
import androidx.compose.ui.window.DialogWindow
import kotlinx.serialization.json.Json
import ua.besf0r.cubauncher.currentTheme
import ua.besf0r.cubauncher.minecraft.version.VersionManifest
import ua.besf0r.cubauncher.util.IOUtils
import ua.besf0r.cubauncher.versionsDir
import java.awt.Dimension
import java.text.SimpleDateFormat

class NewInctance(
    val onDismissRequest: () -> Unit
) {
    private val json = Json { ignoreUnknownKeys = true }

    @Composable
    fun openNewInstanceWindow() {
        DialogWindow(
            state = DialogState(size = DpSize(800.dp, 600.dp)),
            onCloseRequest = { onDismissRequest() },
            title = "Cubauncher: створити збірку",
        ) {
            window.minimumSize = Dimension(780, 550)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(currentTheme.fontColor)
                    .padding(20.dp)
            ) {
                TextButton(
                    onClick = {},
                    colors = ButtonDefaults.buttonColors(backgroundColor = currentTheme.rightColumnColor),
                    modifier = Modifier.width(100.dp).height(100.dp).align(Alignment.TopStart)
                ) {

                }
            }

            val manifest: VersionManifest.VersionManifest = json.decodeFromString(
                IOUtils.readUtf8String(versionsDir.resolve("version_manifest_v2.json")))

            var selectedVersion by remember { mutableStateOf<VersionManifest.Version?>(null) }

            VersionColumn(manifest?.versions?.filter { it.type == "release" }?.toTypedArray()){
                selectedVersion = it
            }
            var instanceName by remember { mutableStateOf<String?>(null) }

            Row {
                val maxLength = 20

                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth()
                        .height(100.dp)
                        .padding(start = 140.dp, end = 20.dp, top = 20.dp)
                        .align(Alignment.CenterVertically),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = currentTheme.focusedBorderColor,
                        unfocusedBorderColor = currentTheme.unfocusedBorderColor,
                        textColor = currentTheme.textColor,
                        disabledTextColor = currentTheme.textColor,
                        focusedLabelColor = currentTheme.textColor,
                        unfocusedLabelColor = currentTheme.textColor
                    ),
                    value = instanceName?: selectedVersion?.id ?: " ",
                    onValueChange = { if (it.length <= maxLength) instanceName = it },
                    label = { Text("Назва збірки") }
                )
            }

            Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomEnd
            ) {
                val onStartDownload = remember { mutableStateOf(false) }

                if (onStartDownload.value) {
                    DownloadInstance(
                        if(instanceName != " "){
                            selectedVersion!!.id
                        }else{
                            instanceName!!
                        },
                        selectedVersion?.id ?: ""
                    ).downloadWindow()
                }

                Column(modifier = Modifier.padding(end = 10.dp, bottom = 25.dp)) {
                    TextButton(
                        onClick = {
                            onStartDownload.value = true
                        },
                        modifier = Modifier.width(200.dp).height(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AddCircle,
                            modifier = Modifier
                                .size(18.dp),
                            contentDescription = "",
                            tint = currentTheme.buttonIconColor
                        )
                        Text(
                            " Створити збірку ", color = currentTheme.textColor, fontSize = 15.sp,
                            textAlign = TextAlign.End
                        )
                    }
                    TextButton(
                        onClick = {
                            onDismissRequest()
                        },
                        modifier = Modifier.width(200.dp).height(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            modifier = Modifier
                                .size(18.dp),
                            contentDescription = "",
                            tint = currentTheme.buttonIconColor
                        )
                        Text(
                            " Закрити", color = currentTheme.textColor, fontSize = 15.sp,
                            textAlign = TextAlign.End
                        )
                    }
                }
            }
        }
    }
    @Composable
    fun VersionColumn(
        versions: Array<VersionManifest.Version>?,
        selected: (VersionManifest.Version) -> Unit
    ) {
        var selectedVersion by remember { mutableStateOf<VersionManifest.Version?>(null) }
        Box(
            modifier = Modifier.fillMaxWidth().padding(
                top = 120.dp, end = 220.dp, start = 150.dp, bottom = 20.dp)
                .border(
                    width = 1.dp,
                    color = currentTheme.focusedBorderColor,
                    shape = RoundedCornerShape(4.dp)
                ),
            contentAlignment = Alignment.TopCenter
        ) {
            Row {
                Text(
                    "  Версія", color = currentTheme.textColor, fontSize = 14.sp,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.weight(1f).padding(5.dp)
                )
                Text(
                    "Дата випуску  ",
                    color = currentTheme.textColor, fontSize = 14.sp,
                    textAlign = TextAlign.End,
                    modifier = Modifier.padding(5.dp)
                )
            }
            if (versions == null) return
            LazyColumn(
                modifier = Modifier.padding(top = 35.dp)
            ) {
                items(versions) {
                    val isSelected = it == selectedVersion
                    TextButton(
                        onClick = {
                            selectedVersion = it
                            selected.invoke(it)
                        },
                        modifier = Modifier.fillMaxWidth().height(35.dp)
                            .background(if (isSelected) Color.Gray else Color.Transparent)

                    ) {
                        Text(
                            it.id, color = currentTheme.textColor, fontSize = 13.sp,
                            textAlign = TextAlign.Start,
                            modifier = Modifier.weight(1f)
                        )
                        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX")
                        val date = inputFormat.parse(it.releaseTime)

                        val outputFormat = SimpleDateFormat("dd.MM.yyyy")
                        val formattedDate = outputFormat.format(date)

                        Text(
                            formattedDate,
                            color = currentTheme.textColor, fontSize = 13.sp,
                            textAlign = TextAlign.End
                        )
                    }
                }
            }
        }
    }
}