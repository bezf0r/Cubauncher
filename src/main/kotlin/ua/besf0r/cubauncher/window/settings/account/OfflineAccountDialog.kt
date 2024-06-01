package ua.besf0r.cubauncher.window.settings.account

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import ua.besf0r.cubauncher.account.OfflineAccount
import ua.besf0r.cubauncher.accountsManager
import ua.besf0r.cubauncher.settingsManager

@Composable
fun OfflineAccountDialog(
    onDismissRequest: () -> Unit
){
    val nickname = remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = { }
    ) {
        Box(
            modifier = Modifier
                .requiredWidth(width = 350.dp)
                .requiredHeight(height = 200.dp)
                .clip(shape = RoundedCornerShape(10.dp))
                .background(color = Color(0xff2d2d2d))
        ) {
            Text(
                text = "Введіть ім’я користувача:",
                color = Color.White,
                textAlign = TextAlign.Center,
                style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Medium),
                modifier = Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(x = 82.dp, y = 40.dp)
                    .wrapContentHeight(align = Alignment.CenterVertically)
            )
            TextButton(
                onClick = { onDismissRequest() },
                modifier = Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(x = 230.dp, y = 140.dp)
                    .wrapContentHeight(align = Alignment.CenterVertically)
            ) {
                Text(
                    text = "Скасувати",
                    color = Color(0xffec6262),
                    textAlign = TextAlign.Center,
                    style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Medium),
                    modifier = Modifier
                        .wrapContentHeight(align = Alignment.CenterVertically))
            }

            TextButton(
                onClick = {
                    onDismissRequest()
                    if (nickname.value.length >= 4){
                        accountsManager.createAccount(OfflineAccount(nickname.value))
                    }
                },
                modifier = Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(x = 40.dp, y = 140.dp)
                    .wrapContentHeight(align = Alignment.CenterVertically)
            ) {
                Text(
                    text = "Створити аккаунт",
                    color = Color(0xff75ea6b),
                    textAlign = TextAlign.Center,
                    style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Medium),
                    modifier = Modifier
                        .wrapContentHeight(align = Alignment.CenterVertically))
            }
            val maxLength = 16

            TextField(
                value = nickname.value,
                onValueChange = {
                    if (it.length <= maxLength) nickname.value = validateMinecraftNickname(it)
                },
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = settingsManager.settings.currentTheme.focusedBorderColor,
                    unfocusedBorderColor = settingsManager.settings.currentTheme.unfocusedBorderColor,
                    textColor = settingsManager.settings.currentTheme.textColor,
                    disabledTextColor = settingsManager.settings.currentTheme.textColor,
                    focusedLabelColor = settingsManager.settings.currentTheme.textColor,
                    unfocusedLabelColor = settingsManager.settings.currentTheme.textColor
                ),
                textStyle = TextStyle(fontSize = 17.sp, textAlign = TextAlign.Center),
                modifier = Modifier
                    .align(alignment = Alignment.TopCenter)
                    .offset(y = 70.dp)
                    .requiredWidth(width = 275.dp)
                    .requiredHeight(height = 55.dp)
            )
        }
    }
}
private fun validateMinecraftNickname(input: String): String {
    val allowedChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_"
    return input.filter { it in allowedChars }
}