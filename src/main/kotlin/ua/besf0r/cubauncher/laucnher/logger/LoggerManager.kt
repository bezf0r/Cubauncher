package ua.besf0r.cubauncher.laucnher.logger

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import kotlinx.coroutines.*
import org.jetbrains.skiko.MainUIDispatcher
import java.io.*

object LoggerManager {
    @Composable
    fun runLogger(currentLog: MutableState<String>) {
        LaunchedEffect(Unit) {
            val pipedOutputStream = PipedOutputStream()
            val pipedInputStream = PipedInputStream(pipedOutputStream)

            CoroutineScope(Dispatchers.IO).launch {
                val reader = BufferedReader(InputStreamReader(pipedInputStream))
                val logBuffer = StringBuilder()
                val logLines = ArrayDeque<String>()
                var line: String?

                while (isActive) {
                    line = reader.readLine()
                    if (line == null) break

                    withContext(MainUIDispatcher) {
                        logLines.add(line)
                        if (logLines.size > 20000) {
                            logLines.removeFirst()
                        }

                        logBuffer.setLength(0)
                        logLines.forEach { logBuffer.append(it).append('\n') }
                        currentLog.value = logBuffer.toString()
                    }
                }
            }
            System.setOut(PrintStream(pipedOutputStream))
        }
    }
}