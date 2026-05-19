package com.aiautocreate.domain.service

import com.aiautocreate.agent.Asset

interface AssetProvider {

    suspend fun searchVideos(query: String, limit: Int = 10): List<Asset>

    suspend fun searchImages(query: String, limit: Int = 10): List<Asset>

    suspend fun searchMusic(query: String, limit: Int = 10): List<Asset>

    suspend fun searchSoundEffects(sceneDescription: String, limit: Int = 5): List<Asset>

    suspend fun getTransitions(limit: Int = 10): List<Asset>
}
