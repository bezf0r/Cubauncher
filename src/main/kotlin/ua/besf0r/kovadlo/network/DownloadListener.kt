package ua.besf0r.kovadlo.network

interface DownloadListener {
    fun onStageChanged(stage: String)
    fun onProgress(value: Long, size: Long)
}
typealias DownloadProgress = (value:Long, size:Long) -> Unit
