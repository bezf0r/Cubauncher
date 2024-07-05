package ua.besf0r.kovadlo.window.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ua.besf0r.kovadlo.Logger
import java.io.IOException
import javax.swing.JFrame
import javax.swing.JOptionPane

@Composable
inline fun <T> RenderAsync(
    crossinline load: suspend () -> (T),
    crossinline itemContent: @Composable ((T) -> Unit)
) {
    val data by produceState<T?>(null) {
        value = withContext(Dispatchers.IO) {
            try {
                load()
            } catch (e: IOException) {
                JOptionPane.showMessageDialog(
                    JFrame(),
                    "Здається ця дія потребує підключення до інтернету. Будь ласка, підключіться до інтернету та спробуйте ще раз.",
                    "Kovadlo ERROR",
                    JOptionPane.ERROR_MESSAGE
                )
                Logger.publish(e.stackTraceToString())
                null
            }
        }
    }

    if (data != null) { itemContent(data!!) }
}
