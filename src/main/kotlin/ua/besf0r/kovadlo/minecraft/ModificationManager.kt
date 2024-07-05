package ua.besf0r.kovadlo.minecraft

import ua.besf0r.kovadlo.instance.Instance
import ua.besf0r.kovadlo.network.DownloadListener

interface ModificationManager {
    fun download(progress: DownloadListener, version: String, instance: Instance)
}