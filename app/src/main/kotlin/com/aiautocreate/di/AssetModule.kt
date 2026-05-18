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
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AssetModule {   // ✅ يجب أن تكون abstract class

    @Binds
    @Singleton
    abstract fun bindPexelsAssetProvider(impl: PexelsAssetProvider): AssetProvider

    @Binds
    @Singleton
    abstract fun bindPixabayAssetProvider(impl: PixabayAssetProvider): AssetProvider

    @Binds
    @Singleton
    abstract fun bindLotsOfSoundsAssetProvider(impl: LotsOfSoundsAssetProvider): AssetProvider

    @Binds
    @Singleton
    abstract fun bindFreesoundAssetProvider(impl: FreesoundAssetProvider): AssetProvider

    @Binds
    @Singleton
    abstract fun bindOpenVFXAssetProvider(impl: OpenVFXAssetProvider): AssetProvider
}
