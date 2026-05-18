package com.aiautocreate.di

import android.content.Context
import androidx.room.Room
import com.aiautocreate.data.datasource.local.db.AppDatabase
import com.aiautocreate.data.datasource.local.db.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "aiautocreate.db"
        )
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    @Singleton
    fun provideProjectDao(db: AppDatabase): ProjectDao = db.projectDao()

    @Provides
    @Singleton
    fun provideModelConfigDao(db: AppDatabase): ModelConfigDao = db.modelConfigDao()

    @Provides
    @Singleton
    fun provideActivityLogDao(db: AppDatabase): ActivityLogDao = db.activityLogDao()

    @Provides
    @Singleton
    fun provideSubtitlePresetDao(db: AppDatabase): SubtitlePresetDao = db.subtitlePresetDao()

    @Provides
    @Singleton
    fun provideMediaFileDao(db: AppDatabase): MediaFileDao = db.mediaFileDao()

    // ❌ تم إزالة provideSyncLogDao نهائياً
}
