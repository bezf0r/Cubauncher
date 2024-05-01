package ua.besf0r.cubauncher.minecraft

interface MinecraftDownloadListener {
    fun onStageChanged(stage: String)
    fun onProgress(value: Long, size: Long)
}
