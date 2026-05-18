package com.aiautocreate.util

object ValidationUtils {
    fun isValidUrl(url: String): Boolean {
        return android.util.Patterns.WEB_URL.matcher(url).matches()
    }

    fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun isApiKeyFormat(key: String): Boolean {
        return key.isNotBlank() && key.length >= 20
    }
}