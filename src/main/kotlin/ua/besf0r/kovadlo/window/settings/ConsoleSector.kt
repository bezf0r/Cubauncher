package ua.besf0r.kovadlo.window.settings

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import org.jetbrains.skiko.MainUIDispatcher
import ua.besf0r.kovadlo.Logger

@Composable
fun ConsoleSector() {
    val logs = remember { mutableStateOf("") }

    try {
        Logger.subscribe {
            CoroutineScope(MainUIDispatcher).async{
                logs.value = it.joinToString("\n")
            }
        }
    }catch (_: Exception){}


    Box(
        modifier = Modifier
            .offset(x = 195.dp)
            .requiredWidth(width = 525.dp)
            .requiredHeight(height = 513.dp)
            .background(color = Color(0xff2d2d2d))
    ) {
        Box(
            modifier = Modifier
                .align(alignment = Alignment.TopStart)
                .offset(x = 25.dp, y = 58.dp)
                .requiredWidth(width = 474.dp)
                .requiredHeight(height = 399.dp)
        ) {
            Box(
                modifier = Modifier
                    .align(alignment = Alignment.TopCenter)
                    .requiredWidth(width = 474.dp)
                    .requiredHeight(height = 399.dp)
                    .clip(shape = RoundedCornerShape(10.dp))
                    .background(color = Color.Black)
            )
            Divider(
                color = Color.White,
                modifier = Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(y = 20.dp)
                    .requiredWidth(width = 474.dp)
            )

            val verticalScrollState = rememberScrollState()
            val horizontalScrollState = rememberScrollState()

            SelectionContainer {
                Text(
                    text = logs.value,
                    color = Color.White,
                    style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Medium),
                    modifier = Modifier
                        .align(alignment = Alignment.TopStart)
                        .offset(x = 4.dp, y = 22.dp)
                        .requiredWidth(width = 445.dp)
                        .requiredHeight(height = 360.dp)
                        .verticalScroll(verticalScrollState)
                        .horizontalScroll(horizontalScrollState)
                )
            }
            VerticalScrollbar(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .requiredWidth(width = 8.dp)
                    .requiredHeight(height = 338.dp)
                    .clip(shape = RoundedCornerShape(10.dp))
                    .background(Color.White),
                adapter = rememberScrollbarAdapter(scrollState = verticalScrollState)
            )
            HorizontalScrollbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .requiredWidth(width = 440.dp)
                    .requiredHeight(height = 8.dp)
                    .clip(shape = RoundedCornerShape(10.dp))
                    .background(Color.White),
                adapter = rememberScrollbarAdapter(scrollState = horizontalScrollState)
            )
        }
    }
}