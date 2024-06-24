package ua.besf0r.cubauncher.window.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

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
                e.printStackTrace()
                null
            }
        }
    }

    if (data != null) {
        itemContent(data!!)
    }
}