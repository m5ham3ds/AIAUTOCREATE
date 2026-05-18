package com.aiautocreate.util

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import timber.log.Timber

object JsonLogger {
    @PublishedApi
    internal val json = Json { prettyPrint = true }

    inline fun <reified T> log(label: String, data: T) {
        try {
            val jsonString = json.encodeToString(data)
            Timber.d("[$label] $jsonString")
        } catch (e: Exception) {
            Timber.e(e, "Failed to serialize $label")
        }
    }
}
