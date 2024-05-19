package ua.besf0r.cubauncher.window

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.WindowState

@Composable
fun windowTitleBar(
    canBeHided : Boolean = true,
    visible: () -> Unit = {},
    close: (() -> Unit)
) {
    Box(
        modifier = Modifier
            .requiredWidth(width = 720.dp)
            .requiredHeight(height = 20.dp)
            .background(color = Color(0xff6a6a6a))
    ) {
        TextButton(
            onClick = {
                close()
            },
            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xffd9d9d9)),
            modifier = Modifier
                .align(alignment = Alignment.CenterStart)
                .offset(x = 695.dp)
                .requiredSize(size = 15.dp)
                .wrapContentSize(Alignment.Center)
        ) {
            Box(modifier = Modifier.requiredSize(15.dp)) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    "", tint = Color.Black,
                    modifier = Modifier.size(12.dp)
                        .offset(x = 1.dp, y = 1.dp)
                        .wrapContentHeight(align = Alignment.CenterVertically)
                )
            }
        }
        if (canBeHided) {
            TextButton(
                onClick = { visible() },
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xffd9d9d9)),
                modifier = Modifier
                    .align(alignment = Alignment.CenterStart)
                    .offset(x = 675.dp)
                    .requiredSize(size = 15.dp)
            ) {
                Box(modifier = Modifier.requiredSize(15.dp)) {
                    Text(
                        text = "_",
                        color = Color.Black,
                        style = TextStyle(
                            fontSize = 12.5.sp
                        ),
                        modifier = Modifier
                            .align(alignment = Alignment.TopStart)
                            .offset(x = 4.dp, y = (-4).dp)
                            .wrapContentHeight(align = Alignment.CenterVertically)
                    )
                }
            }
        }
        Text(
            text = "Cubauncher (1.0-beta)",
            color = Color.White,
            style = TextStyle(fontSize = 12.sp),
            modifier = Modifier
                .align(alignment = Alignment.CenterStart)
                .offset(x = 37.dp)
                .requiredWidth(width = 155.dp)
                .wrapContentHeight(align = Alignment.CenterVertically)
        )
    }
}
@Composable
fun FrameWindowScope.createMainTitleBar(
    windowState: WindowState, onDismissed: (() -> Unit)) {
    this.WindowDraggableArea {
        windowTitleBar(
            visible = {
                windowState.isMinimized = !windowState.isMinimized
            },
            close = { onDismissed() }
        )
    }
}