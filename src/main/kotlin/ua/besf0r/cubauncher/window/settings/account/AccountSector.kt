package ua.besf0r.cubauncher.window.settings.account

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ua.besf0r.cubauncher.account.Account
import ua.besf0r.cubauncher.account.AccountsUpdateEvent
import ua.besf0r.cubauncher.accountsManager
import ua.besf0r.cubauncher.settingsManager

@Composable
fun AccountSector() {
    var accounts by remember { mutableStateOf(listOf<Account>()) }

    AccountsUpdateEvent.subscribe { accounts = it.toMutableList() }
    val currentAccount = remember {
        mutableStateOf(settingsManager.settings.selectedAccount)
    }

    Box(
        modifier = Modifier
            .requiredWidth(width = 525.dp)
            .requiredHeight(height = 513.dp)
            .offset(195.dp)
            .background(color = Color(0xff2d2d2d))
    ) {
        val onPirateButton = remember { mutableStateOf(false) }
        if (onPirateButton.value) OfflineAccountDialog {
            onPirateButton.value = false
        }
        TextButton(
            onClick = { onPirateButton.value = true },
            modifier = Modifier
                .align(alignment = Alignment.TopStart)
                .offset(x = 220.dp, y = 371.dp)
                .requiredWidth(width = 173.dp)
                .requiredHeight(height = 30.dp)
        ) {
            Box(
                modifier = Modifier
                    .requiredWidth(width = 173.dp)
                    .requiredHeight(height = 30.dp)
                    .clip(shape = RoundedCornerShape(4.5.dp))
                    .background(color = Color(0xff464646))
            ) {
                Text(
                    text = "Додати піратський",
                    color = Color.White,
                    style = TextStyle(fontSize = 14.sp),
                    modifier = Modifier
                        .align(alignment = Alignment.Center)
                        .offset(x = 17.dp)
                        .requiredWidth(width = 139.dp)
                        .requiredHeight(height = 20.dp)
                )
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "",
                    modifier = Modifier
                        .align(alignment = Alignment.TopStart)
                        .offset(x = 11.dp, y = 7.dp)
                        .requiredSize(size = 18.dp),
                    tint = settingsManager.settings.currentTheme.textColor
                )
            }
        }

        val onMicrosoftButton = remember { mutableStateOf(false) }
        if (onMicrosoftButton.value) {
            ParseMicrosoftAccountDialog (
                onDeviceCode = {
                    MicrosoftAccountDialog(it) { onMicrosoftButton.value = false }
                },
                onSuccess = {
                    onMicrosoftButton.value = false
                })
        }
        TextButton(
            onClick = { onMicrosoftButton.value = true },
            modifier = Modifier
                .align(alignment = Alignment.TopStart)
                .offset(x = 38.dp, y = 371.dp)
                .requiredWidth(width = 173.dp)
                .requiredHeight(height = 30.dp)
        ) {
            Box(
                modifier = Modifier
                    .requiredWidth(width = 173.dp)
                    .requiredHeight(height = 30.dp)
                    .clip(shape = RoundedCornerShape(4.5.dp))
                    .background(color = Color(0xff464646))
            ) {
                Text(
                    text = "Додати Microsoft",
                    color = Color.White,
                    style = TextStyle(fontSize = 14.sp),
                    modifier = Modifier
                        .align(alignment = Alignment.Center)
                        .offset(x = 11.3.dp)
                        .requiredWidth(width = 128.dp)
                        .requiredHeight(height = 20.dp)
                )
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "",
                    modifier = Modifier
                        .align(alignment = Alignment.CenterStart)
                        .offset(x = 11.dp, y = 1.dp)
                        .requiredSize(size = 18.dp),
                    tint = settingsManager.settings.currentTheme.textColor
                )
            }
        }
        TextButton(
            onClick = { accountsManager.deleteAccount(currentAccount.value?: "") },
            modifier = Modifier
                .align(alignment = Alignment.TopStart)
                .offset(x = 38.dp, y = 415.dp)
                .requiredWidth(width = 173.dp)
                .requiredHeight(height = 30.dp)
        ) {
            Box(
                modifier = Modifier
                    .requiredWidth(width = 173.dp)
                    .requiredHeight(height = 30.dp)
                    .clip(shape = RoundedCornerShape(4.5.dp))
                    .background(color = Color(0xff464646))
            ) {
                Text(
                    text = "Видалити аккаунт",
                    color = Color.White,
                    style = TextStyle(fontSize = 14.sp),
                    modifier = Modifier
                        .align(alignment = Alignment.Center)
                        .offset(x = 17.dp)
                        .requiredWidth(width = 139.dp)
                        .requiredHeight(height = 20.dp)
                )
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "",
                    modifier = Modifier
                        .align(alignment = Alignment.TopStart)
                        .offset(x = 11.dp, y = 7.dp)
                        .requiredSize(size = 18.dp),
                    tint = settingsManager.settings.currentTheme.textColor
                )
            }
        }
        Box(
            modifier = Modifier
                .offset(x = 35.dp, y = 41.dp)
                .requiredWidth(width = 450.dp)
                .requiredHeight(height = 316.dp)
                .clip(shape = RoundedCornerShape(5.dp))
                .background(color = Color(0xff1e1e1e))
        ) {
            Box(
                modifier = Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(x = 5.dp, y = 5.dp)
                    .requiredWidth(width = 440.dp)
                    .requiredHeight(height = 21.dp)
                    .clip(shape = RoundedCornerShape(5.dp))
                    .background(color = Color(0xff2d2d2d))
            )
            Text(
                text = "Ім’я",
                color = Color.White,
                textAlign = TextAlign.Center,
                style = TextStyle(fontSize = 15.sp),
                modifier = Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(x = 85.dp, y = 4.dp)
                    .requiredWidth(width = 45.dp)
                    .requiredHeight(height = 16.dp)
            )
            Text(
                text = "Тип",
                color = Color.White,
                textAlign = TextAlign.Center,
                style = TextStyle(fontSize = 15.sp),
                modifier = Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(x = 310.dp, y = 4.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(1),
                horizontalArrangement = Arrangement.spacedBy((-439).dp),
                verticalArrangement = Arrangement.spacedBy(1.dp),
                modifier = Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(x = 4.dp, y = 26.dp)
                    .requiredWidth(width = 441.dp)
                    .requiredHeight(height = 316.dp),
                content = {
                    items(accounts) {account ->
                        AccountGrid(currentAccount,account)
                    }
                }
            )
        }
    }
}

@Composable
private fun AccountGrid(
    currentAccount: MutableState<String?>,
    account: Account,
) {
    TextButton(
        onClick = {
            currentAccount.value = account.username

            val settings = settingsManager.settings
            settings.selectedAccount = account.username
        },
        modifier = Modifier
            .requiredWidth(width = 440.dp)
            .requiredHeight(height = 25.dp)
    ) {
        Box(
            modifier = Modifier
                .requiredWidth(width = 440.dp)
                .requiredHeight(height = 25.dp)
                .background(
                    color = if (currentAccount.value == account.username)
                        settingsManager.settings.currentTheme.selectedButtonColor
                    else Color.Transparent
                )
        ) {
            Text(
                text = account.username,
                color = Color.White,
                textAlign = TextAlign.Center,
                style = TextStyle(
                    fontSize = 13.sp
                ),
                modifier = Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(x = 3.dp, y = 5.dp)
                    .requiredWidth(width = 200.dp)
            )
            Text(
                text = if (account.accessToken == "-") "Піратський"  else "Microsoft",
                color = Color.White,
                textAlign = TextAlign.Center,
                style = TextStyle(
                    fontSize = 13.sp
                ),
                modifier = Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(x = 220.dp, y = 5.dp)
                    .requiredWidth(width = 200.dp)
            )
        }
    }
}
