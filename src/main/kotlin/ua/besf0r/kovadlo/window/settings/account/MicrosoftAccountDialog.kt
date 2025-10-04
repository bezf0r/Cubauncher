package ua.besf0r.kovadlo.window.settings.account

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import ua.besf0r.kovadlo.Logger
import ua.besf0r.kovadlo.account.microsoft.MicrosoftDeviceCode
import ua.besf0r.kovadlo.account.microsoft.MicrosoftOAuthUtils
import ua.besf0r.kovadlo.accountsManager
import ua.besf0r.kovadlo.logger
import ua.besf0r.kovadlo.window.component.HyperlinkText
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

@Composable
fun MicrosoftAccountDialog(
    deviceCode: MicrosoftDeviceCode,
    onDismissRequest: () -> Unit
){
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
            HyperlinkText(
                text = "Перейдіть за посиланням \n ${deviceCode.verificationUri} " +
                        "\n та введіть код: ${deviceCode.userCode}",
                url = deviceCode.verificationUri,
                color = Color.White,
                textAlign = TextAlign.Center,
                style = TextStyle(fontSize = 14.5.sp, fontWeight = FontWeight.Medium),
                modifier = Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(x = 18.dp, y = 40.dp)
                    .requiredWidth(width = 315.dp)
                    .requiredHeight(height = 65.dp)
                    .wrapContentHeight(align = Alignment.CenterVertically)
            )

            val onCopy = remember { mutableStateOf(false) }
            if (onCopy.value) {
                copyToClipboard(deviceCode.userCode)
                onCopy.value = false
            }

            TextButton(
                onClick = { onCopy.value = true },
                modifier = Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(x = 55.dp, y = 125.dp)
                    .wrapContentHeight(align = Alignment.CenterVertically)
            ) {
                Text(
                    text = "Скопіювати код",
                    color = Color(0xffd9d9d9),
                    textAlign = TextAlign.Center,
                    style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Medium),
                    modifier = Modifier.wrapContentHeight(align = Alignment.CenterVertically)
                )
            }
            TextButton(
                onClick = { onDismissRequest() },
                modifier = Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(x = 210.dp, y = 125.dp)
                    .wrapContentHeight(align = Alignment.CenterVertically)
            ) {
                Text(
                    text = "Скасувати",
                    color = Color(0xffec6262),
                    textAlign = TextAlign.Center,
                    style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Medium),
                    modifier = Modifier.wrapContentHeight(align = Alignment.CenterVertically)
                )
            }
        }
    }
}
fun copyToClipboard(text: String) {
    val stringSelection = StringSelection(text)
    val clipboard = Toolkit.getDefaultToolkit().systemClipboard
    clipboard.setContents(stringSelection, null)
}
@Composable
fun ParseMicrosoftAccountDialog(
    di: DI,
    onDeviceCode: @Composable (deviceCode: MicrosoftDeviceCode) -> Unit,
    onSuccess: () -> Unit
){
    val microsoftOAuthUtils = di.direct.instance<MicrosoftOAuthUtils>()
    val isCallBack = remember { mutableStateOf(false) }
    if (isCallBack.value) onDeviceCode(microsoftOAuthUtils.currentCallBack!!)

    LaunchedEffect(Unit){
        microsoftOAuthUtils.obtainDeviceCodeAsync(
            deviceCodeCallback = {
                microsoftOAuthUtils.currentCallBack = it
                isCallBack.value = true
            },
            errorCallback = {},
            successCallback = {
                onSuccess()
                di.logger().publish("launcher","Авторизація успішна!")

                microsoftOAuthUtils.loginToMicrosoftAccount(it) { account ->
                    di.accountsManager().createAccount(account)
                    di.logger().publish("launcher","Успішний вхід для користувача: ${account.username}")
                }
            }
        )
    }
}