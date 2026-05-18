package com.aiautocreate.util

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

object DateTimeUtils {
    fun formatTimestamp(timestamp: Long, pattern: String = "yyyy/MM/dd HH:mm"): String {
        val instant = Instant.ofEpochMilli(timestamp)
        val formatter = DateTimeFormatter.ofPattern(pattern, Locale.getDefault())
            .withZone(ZoneId.systemDefault())
        return formatter.format(instant)
    }

    fun getRelativeTime(timestamp: Long): String {
        val now = Instant.now()
        val then = Instant.ofEpochMilli(timestamp)
        val diff = ChronoUnit.MILLIS.between(then, now)

        return when {
            diff < 60_000 -> "الآن"
            diff < 3600_000 -> "${diff / 60_000} د"
            diff < 86_400_000 -> "${diff / 3600_000} س"
            else -> "${diff / 86_400_000} يوم"
        }
    }
}