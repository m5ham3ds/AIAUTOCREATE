package com.aiautocreate.di

import com.aiautocreate.data.asset.FreesoundAssetProvider
import com.aiautocreate.data.asset.LotsOfSoundsAssetProvider
import com.aiautocreate.data.asset.OpenVFXAssetProvider
import com.aiautocreate.data.asset.PexelsAssetProvider
import com.aiautocreate.data.asset.PixabayAssetProvider
import com.aiautocreate.domain.service.AssetProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AssetModule {

    @Provides
    @Singleton
    @IntoSet
    fun providePexelsAssetProvider(impl: PexelsAssetProvider): AssetProvider = impl

    @Provides
    @Singleton
    @IntoSet
    fun providePixabayAssetProvider(impl: PixabayAssetProvider): AssetProvider = impl

    @Provides
    @Singleton
    @IntoSet
    fun provideLotsOfSoundsAssetProvider(impl: LotsOfSoundsAssetProvider): AssetProvider = impl

    @Provides
    @Singleton
    @IntoSet
    fun provideFreesoundAssetProvider(impl: FreesoundAssetProvider): AssetProvider = impl

    @Provides
    @Singleton
    @IntoSet
    fun provideOpenVFXAssetProvider(impl: OpenVFXAssetProvider): AssetProvider = impl
}
