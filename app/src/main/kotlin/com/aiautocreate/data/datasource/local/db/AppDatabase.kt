package com.aiautocreate.data.datasource.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.aiautocreate.data.datasource.local.db.dao.*
import com.aiautocreate.data.datasource.local.db.entities.*

/**
 * قاعدة البيانات المحلية لتطبيق AI AutoCreate.
 */
@Database(
    entities = [
        ProjectEntity::class,
        ModelConfigEntity::class,
        ActivityLogEntity::class,
        SubtitlePresetEntity::class,
        MediaFileEntity::class,
        SyncLogEntity::class
    ],
    version = 2,  // ✅ تم زيادة رقم الإصدار لتطبيق التغييرات
    exportSchema = true
)
@TypeConverters(Converters::class)  // ✅ تم إضافة المحولات
abstract class AppDatabase : RoomDatabase() {

    abstract fun projectDao(): ProjectDao
    abstract fun modelConfigDao(): ModelConfigDao
    abstract fun activityLogDao(): ActivityLogDao
    abstract fun subtitlePresetDao(): SubtitlePresetDao
    abstract fun mediaFileDao(): MediaFileDao
    abstract fun syncLogDao(): SyncLogDao

    companion object {
        const val DATABASE_NAME = "aiautocreate.db"
    }
}
