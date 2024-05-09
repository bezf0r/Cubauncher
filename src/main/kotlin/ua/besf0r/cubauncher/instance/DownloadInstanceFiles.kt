package ua.besf0r.cubauncher.instance

import ua.besf0r.cubauncher.instanceManager

class DownloadInstanceFiles(
    private val name: String?,
    private val version: String?
) {
    fun downloadWindow(): Instance? {

        if (version == null) return null

        val correctName = name ?: version
        val instance = instanceManager.createInstance(correctName,version)

        return instance
    }
}