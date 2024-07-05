package ua.besf0r.kovadlo.window.component

import androidx.compose.foundation.text.ClickableText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration

@Composable
fun HyperlinkText(
    text: String,
    url: String,
    color: Color,
    textAlign: TextAlign,
    style: TextStyle,
    modifier: Modifier = Modifier
) {
    val annotatedText = buildAnnotatedString {
        val startIndex = text.indexOf(url)
        val endIndex = startIndex + url.length
        append(text)
        addStyle(
            style = SpanStyle(color = Color.LightGray,
                textDecoration = TextDecoration.Underline),
            start = startIndex, end = endIndex
        )
        addStringAnnotation(
            tag = "URL",
            annotation = url,
            start = startIndex,
            end = endIndex
        )
    }

    val uriHandler = LocalUriHandler.current

    ClickableText(
        text = annotatedText,
        style = style.copy(color = color, textAlign = textAlign),
        onClick = { offset ->
            annotatedText.getStringAnnotations(
                tag = "URL",
                start = offset,
                end = offset
            ).firstOrNull()?.let { annotation ->
                uriHandler.openUri(annotation.item)
            }
        },
        modifier = modifier
    )
}

