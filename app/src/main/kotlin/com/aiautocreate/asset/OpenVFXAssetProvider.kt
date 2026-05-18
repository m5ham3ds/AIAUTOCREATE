package com.aiautocreate.data.asset

import com.aiautocreate.agent.Asset
import com.aiautocreate.domain.service.AssetProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OpenVFXAssetProvider @Inject constructor() : AssetProvider {

    private val transitions = listOf(
        Asset(id = "openvfx_fade", name = "تلاشي (Fade)", command = "fade"),
        Asset(id = "openvfx_slide_left", name = "انزلاق لليسار", command = "slideleft"),
        Asset(id = "openvfx_slide_right", name = "انزلاق لليمين", command = "slideright"),
        Asset(id = "openvfx_wipe", name = "مسح", command = "wipe"),
        Asset(id = "openvfx_zoom", name = "تكبير", command = "zoom"),
        Asset(id = "openvfx_circle_open", name = "فتح دائري", command = "circleopen"),
        Asset(id = "openvfx_cross_zoom", name = "تكبير متقاطع", command = "crosszoom")
    )

    override suspend fun getTransitions(limit: Int): List<Asset> = withContext(Dispatchers.IO) {
        transitions.take(limit)
    }

    override suspend fun searchVideos(query: String, limit: Int): List<Asset> = emptyList()
    override suspend fun searchImages(query: String, limit: Int): List<Asset> = emptyList()
    override suspend fun searchMusic(query: String, limit: Int): List<Asset> = emptyList()
    override suspend fun searchSoundEffects(sceneDescription: String, limit: Int): List<Asset> = emptyList()
}
