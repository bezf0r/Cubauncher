package ua.besf0r.cubauncher.window

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import ua.besf0r.cubauncher.currentTheme

class StatebleWindow (
    private val text: String = "",
    private val icon: ImageVector? = null,
    private var progress: Pair<Boolean,Int> = Pair(false,0),
    private val subText: String = "",
    private val first: String = "",
    private val onFirst: () -> Unit = {},
    private val second: String = "",
    private val onSecond: () -> Unit = {}
) {

    @Composable
    fun stateWindow() {
        var canClose by remember { mutableStateOf(true) }

        if (!canClose) return
        Dialog(onDismissRequest = { }) {
            Card(
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.padding(10.dp, 5.dp, 10.dp, 10.dp),
                elevation = 8.dp
            ) {
                Column(
                    Modifier.background(currentTheme.fontColor)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {

                        if (icon != null) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                modifier = Modifier
                                    .padding(top = 35.dp)
                                    .height(70.dp)
                                    .fillMaxWidth(),
                                tint = currentTheme.textColor
                            )
                        }
                        if (progress.first) {
                            customProgressBar(
                                Modifier
                                    .clip(shape = RoundedCornerShape(5.dp))
                                    .height(14.dp)
                                    .padding(top = 5.dp)
                                    .align(Alignment.CenterHorizontally),
                                Color.White,
                                Brush.horizontalGradient(listOf(Color(0xffFD7D20), Color(0xffFBE41A)))
                            )
                        }

                        Text(
                            text = text,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .padding(top = 5.dp)
                                .fillMaxWidth(),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            color = currentTheme.textColor
                        )
                        Text(
                            text = subText,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .padding(top = 10.dp, start = 25.dp, end = 25.dp)
                                .fillMaxWidth(),
                            color = currentTheme.textColor
                        )
                    }
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                            .background(currentTheme.rightColumnColor),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        if (first != "") {
                            TextButton(onClick = {
                                onFirst.invoke()
                                canClose = false
                            }) {
                                Text(
                                    first,
                                    fontWeight = FontWeight.Bold,
                                    color = currentTheme.textColor,
                                    modifier = Modifier.padding(top = 5.dp, bottom = 5.dp)
                                )
                            }
                        }
//                        if (second != "") {
//                            TextButton(onClick = {
//                                onSecond.invoke()
//                                canClose = false
//                            }) {
//                                Text(
//                                    second,
//                                    fontWeight = FontWeight.ExtraBold,
//                                    color = currentTheme.textColor,
//                                    modifier = Modifier.padding(top = 5.dp, bottom = 5.dp)
//                                )
//                            }
//                        }
                    }
                }
            }
        }
    }

    @Composable
    fun customProgressBar(
        modifier: Modifier,
        backgroundColor: Color,
        foregroundColor: Brush
    ) {
        Box(
            modifier = modifier
                .background(backgroundColor)
                .width(300.dp)
        ) {
            Box(
                modifier = modifier
                    .background(foregroundColor)
                    .width(300.dp * progress.second / 100)
            )
        }
    }
}
