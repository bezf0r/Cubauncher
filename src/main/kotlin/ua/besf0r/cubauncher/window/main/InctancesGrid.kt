package ua.besf0r.cubauncher.window.main

import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.useResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ua.besf0r.cubauncher.currentTheme

@Composable
fun instancesGrid() {
//    val icon = useResource("https://icons.iconarchive.com/icons/papirus-team/papirus-apps/256/minecraft-icon.png") { loadImageBitmap(it) }
    Box(
        Modifier.fillMaxSize()
            .padding(start = 185.dp, bottom = 120.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        val data = listOf("Приклад збірки номер 1","Приклад збірки номер 1","Приклад збірки номер 1","Приклад збірки номер 1","Приклад збірки номер 1","Приклад збірки номер 1","Приклад збірки номер 1","Приклад збірки номер 1","Приклад збірки номер 1","Приклад збірки номер 1","Приклад збірки номер 1","Приклад збірки номер 1","Приклад збірки номер 1","Приклад збірки номер 1","Приклад збірки номер 1","Приклад збірки номер 1","Приклад збірки номер 1")

        LazyVerticalGrid(
            GridCells.Fixed(3),
            contentPadding = PaddingValues(8.dp)
        ) {
            items(data){
                Button(
                    onClick = {},
                    modifier = Modifier.padding(4.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColor = currentTheme.rightColumnColor)
                ) {
                    Box(Modifier.fillMaxSize()) {
//                        Image(
//                            icon,
//                            "",
//                            modifier = Modifier.height(60.dp).width(60.dp).align(Alignment.TopStart)
//                        )
                        Box(Modifier.fillMaxSize().padding(start = 70.dp)) {
                            Text(
                                text = it,
                                fontSize = 15.sp,
                                textAlign = TextAlign.Center,
                                color = currentTheme.textColor
                            )
                        }
                    }
                }
            }
        }
    }
}