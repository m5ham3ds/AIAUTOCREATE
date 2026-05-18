package com.aiautocreate.util

object StringUtils {
    fun sanitizeFileName(name: String): String {
        return name.replace(Regex("[\\\\/:*?\"<>|]"), "_").trim()
    }

    fun truncate(text: String, maxLength: Int = 100): String {
        return if (text.length > maxLength) text.take(maxLength) + "..." else text
    }

    fun isValidHexColor(hex: String): Boolean {
        return Regex("^#([0-9A-Fa-f]{6}|[0-9A-Fa-f]{8})$").matches(hex)
    }
}