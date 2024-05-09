package ua.besf0r.cubauncher.window.theme

import androidx.compose.ui.graphics.Color


data class ThemeData(
    val fontColor: Color,
    val panelsColor: Color,
    val buttonColor: Color,
    val buttonIconColor: Color,
    val focusedBorderColor: Color,
    val unfocusedBorderColor: Color,
    val textColor: Color,
    val selectedButtonColor: Color
)
object UiTheme {
    val dark = ThemeData(
        fontColor =  Color(0xFF1E1E1E),
        panelsColor =  Color(0xFF2D2D2D),
        buttonColor = Color(0xFF464646),
        buttonIconColor =  Color.White,
        focusedBorderColor =  Color.LightGray,
        unfocusedBorderColor =  Color.White,
        textColor =  Color.White,
        selectedButtonColor = Color(0xFF9BA4B5)
    )
}