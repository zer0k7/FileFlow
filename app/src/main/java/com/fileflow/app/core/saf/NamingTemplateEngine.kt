package com.fileflow.app.core.saf

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object NamingTemplateEngine {

    fun generateName(
        template: String,
        prefix: String,
        toolName: String,
        originalName: String? = null,
        extension: String
    ): String {
        val now = Date()
        val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(now)
        val timeStr = SimpleDateFormat("HHmmss", Locale.US).format(now)
        val cleanPrefix = prefix.ifBlank { "FileFlow" }.replace(Regex("[^a-zA-Z0-9_-]"), "_")
        val cleanTool = toolName.replace(Regex("[^a-zA-Z0-9_-]"), "_")
        val cleanOriginal = (originalName ?: "file")
            .substringBeforeLast('.')
            .replace(Regex("[^a-zA-Z0-9_-]"), "_")
        val cleanExt = extension.removePrefix(".")

        var result = template.ifBlank { "{PREFIX}_{DATE}_{TIME}" }
            .replace("{PREFIX}", cleanPrefix, ignoreCase = true)
            .replace("{TOOL}", cleanTool, ignoreCase = true)
            .replace("{DATE}", dateStr, ignoreCase = true)
            .replace("{TIME}", timeStr, ignoreCase = true)
            .replace("{ORIGINAL}", cleanOriginal, ignoreCase = true)

        result = result.replace(Regex("[^a-zA-Z0-9_(). -]"), "_")
        return if (result.endsWith(".$cleanExt", ignoreCase = true)) {
            result
        } else {
            "$result.$cleanExt"
        }
    }
}
