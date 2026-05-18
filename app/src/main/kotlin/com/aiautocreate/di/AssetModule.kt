package com.aiautocreate.di

import com.aiautocreate.data.asset.FreesoundAssetProvider
import com.aiautocreate.data.asset.LotsOfSoundsAssetProvider
import com.aiautocreate.data.asset.OpenVFXAssetProvider
import com.aiautocreate.data.asset.PexelsAssetProvider
import com.aiautocreate.data.asset.PixabayAssetProvider
import com.aiautocreate.domain.service.AssetProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AssetModule {

    @Binds
    @Singleton
    @IntoSet
    abstract fun bindPexelsAssetProvider(impl: PexelsAssetProvider): AssetProvider

    @Binds
    @Singleton
    @IntoSet
    abstract fun bindPixabayAssetProvider(impl: PixabayAssetProvider): AssetProvider

    @Binds
    @Singleton
    @IntoSet
    abstract fun bindLotsOfSoundsAssetProvider(impl: LotsOfSoundsAssetProvider): AssetProvider

    @Binds
    @Singleton
    @IntoSet
    abstract fun bindFreesoundAssetProvider(impl: FreesoundAssetProvider): AssetProvider

    @Binds
    @Singleton
    @IntoSet
    abstract fun bindOpenVFXAssetProvider(impl: OpenVFXAssetProvider): AssetProvider
}
