package ua.besf0r.kovadlo.localization

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import kotlinx.serialization.json.*
import ua.besf0r.kovadlo.Logger
import java.net.URL
import java.nio.file.Path
import java.util.*

class Strings(private val map: Map<String, String>) {
    fun get(key: String, vararg args: Any): String {
        val value = map[key] ?: key
        return if (args.isNotEmpty()) value.format(*args) else value
    }
}

class LocalizationManager(
    private val logger: Logger,
    private val cacheDir: Path,
) {
    private val serverUrl: String? = null

    private val _locale = mutableStateOf(detectSystemLocale())
    private val _strings = mutableStateOf(loadStrings(_locale.value))

    val locale: State<String> get() = _locale
    val strings: State<Strings> get() = _strings

    fun setLocale(lang: String) {
        _locale.value = lang
        _strings.value = loadStrings(lang)
    }

    init {
        loadStrings(detectSystemLocale())
    }

    private fun loadStrings(lang: String): Strings {
        val file = cacheDir.resolve("$lang.json").toFile()

        if (file.exists()) {
            return parseStrings(file.readText())
        }

        if (serverUrl != null) {
            try {
                val url = URL("$serverUrl/$lang.json")
                val text = url.readText()
                file.writeText(text)
                return parseStrings(text)
            } catch (e: Exception) {
                logger.publish("launcher", "Не вдалося завантажити локалізацію з сервера: ${e.message}")
            }
        }

        val resource = {}.javaClass.getResource("/lang/$lang.json")
        if (resource != null) {
            val text = resource.readText()
            return parseStrings(text)
        }

        val fallback = {}.javaClass.getResource("/lang/uk.json")!!.readText()
        return parseStrings(fallback)
    }

    private fun parseStrings(text: String): Strings {
        val jsonElement = Json.parseToJsonElement(text)
        val map = if (jsonElement is JsonObject) flattenJson(jsonElement) else emptyMap()
        return Strings(map)
    }

    private fun flattenJson(json: JsonObject, parentKey: String = ""): Map<String, String> {
        val result = mutableMapOf<String, String>()

        for ((key, value) in json) {
            val fullKey = if (parentKey.isEmpty()) key else "$parentKey.$key"

            when (value) {
                is JsonObject -> result.putAll(flattenJson(value, fullKey))
                is JsonPrimitive -> {
                    if (value.isString) {
                        result[fullKey] = value.content
                    } else {
                        result[fullKey] = value.toString()
                    }
                }
                is JsonArray -> {
                    // масиви теж можна серіалізувати як JSON рядок
                    result[fullKey] = value.toString()
                }
            }
        }

        return result
    }

    private fun detectSystemLocale(): String {
        val sysLang = Locale.getDefault().language
        return sysLang
    }
}
