package com.aiautocreate.data.datasource.local.db

import androidx.room.TypeConverter
import java.util.Date

/**
 * محولات الأنواع المخصصة لـ Room.
 */
class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}
