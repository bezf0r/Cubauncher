package ua.besf0r.cubauncher.util

object TimeUtils {
    fun getHoursMinutesSeconds(totalSeconds: Long): String {
        val hours = totalSeconds / 3600
        val minutes = totalSeconds % 3600 / 60
        val seconds = totalSeconds % 60
        var time = ""
        if (hours != 0L) {
            time = "$hours годин"
        }
        if (minutes != 0L) {
            time = if (hours == 0L) {
                "$time$minutes хвилин"
            } else {
                "$time, $minutes хвилин"
            }
        }
        if (seconds != 0L) {
            time = if (minutes == 0L && hours == 0L) {
                "$time$seconds секунд"
            } else {
                "$time, $seconds секунд"
            }
        }
        return time
    }
}