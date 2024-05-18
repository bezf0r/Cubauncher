package ua.besf0r.cubauncher.laucnher.logger

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import kotlinx.coroutines.*
import org.jetbrains.skiko.MainUIDispatcher
import java.io.*

object LoggerManager {
    @Composable
    fun runLogger(currentLog: MutableState<String>){

        LaunchedEffect(Unit) {
            val pipedOutputStream = PipedOutputStream()
            val pipedInputStream = PipedInputStream(pipedOutputStream)

            CoroutineScope(Dispatchers.IO).launch {
                val reader = BufferedReader(InputStreamReader(pipedInputStream))
                var line: String?

                while (isActive) {
                    line = reader.readLine()
                    if (line == null) break

                    withContext(MainUIDispatcher) {
                        currentLog.value += "$line\n"

                        val lines = currentLog.value.lineSequence().toList()
                        if (lines.size > 20000) {
                            currentLog.value = lines.drop(1).joinToString("\n")
                        }
                    }
                }
            }
            System.setOut(PrintStream(pipedOutputStream))
        }
    }
}