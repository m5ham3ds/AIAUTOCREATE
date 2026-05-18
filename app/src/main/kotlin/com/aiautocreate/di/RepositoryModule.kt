package com.aiautocreate.di

import com.aiautocreate.data.repository.*
import com.aiautocreate.domain.repository.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindProjectRepository(
        projectRepository: ProjectRepository
    ): IProjectRepository

    @Binds
    @Singleton
    abstract fun bindModelsRepository(
        modelsRepository: ModelsRepository
    ): IModelsRepository

    @Binds
    @Singleton
    abstract fun bindMediaFileRepository(
        mediaFileRepository: MediaFileRepository
    ): IMediaFileRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        settingsRepository: SettingsRepository
    ): ISettingsRepository

    @Binds
    @Singleton
    abstract fun bindSyncRepository(
        syncRepository: SyncRepository
    ): ISyncRepository

    @Binds
    @Singleton
    abstract fun bindCacheRepository(
        cacheRepository: CacheRepository
    ): ICacheRepository
}
