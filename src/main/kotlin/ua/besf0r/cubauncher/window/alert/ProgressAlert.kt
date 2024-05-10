package ua.besf0r.cubauncher.window.alert

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun progressAlert(
    stage: MutableState<String?>,
    rate: MutableState<Int>,
    onDismissRequest: () -> Unit
) {
    if (stage.value == null) return
    Dialog(
        onDismissRequest = { }
    ) {
        Box(
            modifier = Modifier
                .requiredWidth(width = 350.dp)
                .requiredHeight(height = 200.dp)
                .clip(shape = RoundedCornerShape(10.dp))
                .background(color = Color(0xff121212))
        ) {
            LinearProgressIndicator(
                color = Color(0xff5297ff),
                backgroundColor = Color(0xff385682),
                modifier = Modifier
                    .align(alignment = Alignment.TopCenter)
                    .offset(x = 0.5.dp, y = 83.dp)
            )
            Text(
                text = stage.value!!,
                color = Color.White,
                textAlign = TextAlign.Center,
                style = TextStyle(
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Medium
                ),
                modifier = Modifier
                    .align(alignment = Alignment.TopCenter)
                    .offset(y = 55.dp)
                    .wrapContentHeight(align = Alignment.CenterVertically)
            )
            TextButton(
                onClick = { onDismissRequest() },
                modifier = Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(x = 140.dp, y = 150.dp)
            ) {
                Text(
                    text = "Скасувати завантаження",
                    color = Color(0xffec6262),
                    textAlign = TextAlign.Center,
                    style = TextStyle(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium),
                    modifier = Modifier
                        .wrapContentHeight(align = Alignment.CenterVertically))
            }
            Text(
                text = "завантаження...",
                color = Color.White,
                textAlign = TextAlign.Center,
                style = TextStyle(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                ),
                modifier = Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(x = 110.dp, y = 111.dp)
                    .wrapContentHeight(align = Alignment.CenterVertically)
            )
            Text(
                text = "${rate.value}%",
                color = Color(0xffc7ddff),
                textAlign = TextAlign.Center,
                style = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                ),
                modifier = Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(x = 68.dp, y = 108.dp)
                    .wrapContentHeight(align = Alignment.CenterVertically)
            )
        }
    }
}