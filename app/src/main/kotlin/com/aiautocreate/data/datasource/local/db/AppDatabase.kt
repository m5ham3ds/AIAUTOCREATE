package com.aiautocreate.data.datasource.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.aiautocreate.data.datasource.local.db.dao.*
import com.aiautocreate.data.datasource.local.db.entities.*

@Database(
    entities = [
        ProjectEntity::class,
        ModelConfigEntity::class,
        ActivityLogEntity::class,
        SubtitlePresetEntity::class,
        MediaFileEntity::class
    ],
    version = 2,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun projectDao(): ProjectDao
    abstract fun modelConfigDao(): ModelConfigDao
    abstract fun activityLogDao(): ActivityLogDao
    abstract fun subtitlePresetDao(): SubtitlePresetDao
    abstract fun mediaFileDao(): MediaFileDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // ✅ Migration من الإصدار 1 إلى 2: إضافة عمودي github_readme_url و updated_at
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // إضافة عمود github_readme_url (نصي، يمكن أن يكون فارغاً)
                database.execSQL("ALTER TABLE model_configs ADD COLUMN github_readme_url TEXT")
                // إضافة عمود updated_at (عدد صحيح، قيمة افتراضية 0)
                database.execSQL("ALTER TABLE model_configs ADD COLUMN updated_at INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ai_auto_create.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
