package ua.besf0r.kovadlo

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import kotlinx.coroutines.*
import org.jetbrains.skiko.MainUIDispatcher
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


data class LogEntry(
    val source: String,
    val message: String,
    val timestamp: String = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
)

class Logger(
    private val scope: CoroutineScope,
) {
    private val maxLines: Int = 1500

    private val _lines = mutableStateListOf<LogEntry>()
    val lines: SnapshotStateList<LogEntry> get() = _lines

    private val buffer = mutableListOf<LogEntry>()
    private val bufferLock = Any()

    private var flushJob: Job? = null

    fun publish(source: String, message: String) {
        val entry = LogEntry(source, message)
        synchronized(bufferLock) { buffer.add(entry) }
        scheduleFlush()
    }

    private fun scheduleFlush() {
        if (flushJob?.isActive != true) {
            flushJob = scope.launch(MainUIDispatcher) {
                delay(200)
                flushBuffer()
            }
        }
    }

    private fun flushBuffer() {
        val toAdd = synchronized(bufferLock) {
            val copy = buffer.toList()
            buffer.clear()
            copy
        }
        _lines.addAll(toAdd)

        if (_lines.size > maxLines) {
            _lines.removeRange(0, _lines.size - maxLines)
        }
    }

    fun getLogsBySource(source: String): SnapshotStateList<LogEntry> =
        mutableStateListOf(*lines.filter { it.source == source }.toTypedArray())
}