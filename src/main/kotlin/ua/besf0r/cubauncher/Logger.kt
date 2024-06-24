package ua.besf0r.cubauncher

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

object Logger {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _events = MutableStateFlow("")
    private val events = _events.asStateFlow()

    private val lines = mutableListOf<String>()

    fun publish(event: String) = scope.launch {
        _events.emit(event)
        lines.add(event)
    }

    fun subscribe(onEvent: (List<String>) -> Unit) = scope.launch {
        events.collectLatest { onEvent(lines) }
    }
}