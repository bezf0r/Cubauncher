package ua.besf0r.cubauncher.network.file

object MavenUtil {
    fun createUrl(name: String): String {
        val parts = name.split(":")
        if (parts.size != 3) {
            throw IllegalArgumentException("Name must be in the format 'group:name:version'")
        }

        val (group, artifact, version) = parts
        val groupPath = group.replace('.', '/')

        return "$groupPath/$artifact/$version/$artifact-$version.jar"
    }
}