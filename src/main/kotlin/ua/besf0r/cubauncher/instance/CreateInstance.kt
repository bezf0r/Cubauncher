package ua.besf0r.cubauncher.instance

import ua.besf0r.cubauncher.instanceManager

class CreateInstance(
    private val name: String?,
    private val version: String?
) {
    fun createFiles(): Instance? {

        if (version == null) return null

        val correctName = name ?: version
        val instance = instanceManager.createInstance(correctName,version)

        return instance
    }
}