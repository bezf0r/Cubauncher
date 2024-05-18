package ua.besf0r.cubauncher.window.theme

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
data class ThemeData(
    @Serializable(with = ColorSerializer::class) val fontColor: Color,
    @Serializable(with = ColorSerializer::class) val panelsColor: Color,
    @Serializable(with = ColorSerializer::class) val buttonColor: Color,
    @Serializable(with = ColorSerializer::class) val buttonIconColor: Color,
    @Serializable(with = ColorSerializer::class) val focusedBorderColor: Color,
    @Serializable(with = ColorSerializer::class) val unfocusedBorderColor: Color,
    @Serializable(with = ColorSerializer::class) val textColor: Color,
    @Serializable(with = ColorSerializer::class) val selectedButtonColor: Color
)
object UiTheme {
    val dark = ThemeData(
        fontColor = Color(0xFF1E1E1E),
        panelsColor = Color(0xFF2D2D2D),
        buttonColor = Color(0xFF464646),
        buttonIconColor = Color.White,
        focusedBorderColor = Color.LightGray,
        unfocusedBorderColor = Color.White,
        textColor = Color.White,
        selectedButtonColor = Color(0xFF9BA4B5)
    )
}

object ColorSerializer : KSerializer<Color> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Color", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Color) {
        val colorString = String.format("#%08X", (value.value.toLong() and 0xFFFFFFFF))
        encoder.encodeString(colorString)
    }

    override fun deserialize(decoder: Decoder): Color {
        val colorString = decoder.decodeString()
        val colorLong = colorString.removePrefix("#").toLong(16)
        return Color(colorLong)
    }
}