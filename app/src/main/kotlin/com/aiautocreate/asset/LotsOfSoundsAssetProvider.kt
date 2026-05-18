package com.aiautocreate.data.asset

import com.aiautocreate.agent.Asset
import com.aiautocreate.domain.service.AssetProvider
import com.aiautocreate.data.repository.AppSettingsRepository
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Singleton
class LotsOfSoundsAssetProvider @Inject constructor(
    private val settingsRepo: AppSettingsRepository,
    private val okHttpClient: OkHttpClient
) : AssetProvider {

    private val baseUrl = "https://api.lotsofsounds.com/v2/"
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun searchMusic(query: String, limit: Int): List<Asset> = withContext(Dispatchers.IO) {
        val apiKey = settingsRepo.getStringOnce("lotsofsounds_api_key", "")
        if (apiKey.isBlank()) return@withContext emptyList()
        val url = "${baseUrl}search?q=${java.net.URLEncoder.encode(query, "UTF-8")}&type=music&limit=$limit"
        val request = Request.Builder().url(url).header("X-API-Key", apiKey).build()
        try {
            val response = okHttpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string() ?: return@withContext emptyList()
                val result = json.decodeFromString<LosResponse>(body)
                result.results.map { sound ->
                    Asset(
                        id = "los_${sound.id}",
                        name = sound.title,
                        type = "music",
                        fileUrl = sound.previewUrl,
                        command = null,
                        localPath = null
                    )
                }
            } else emptyList()
        } catch (e: Exception) { emptyList() }
    }

    override suspend fun searchSoundEffects(sceneDescription: String, limit: Int): List<Asset> = withContext(Dispatchers.IO) {
        val apiKey = settingsRepo.getStringOnce("lotsofsounds_api_key", "")
        if (apiKey.isBlank()) return@withContext emptyList()
        val url = "${baseUrl}search?q=${java.net.URLEncoder.encode(sceneDescription, "UTF-8")}&type=sfx&limit=$limit"
        val request = Request.Builder().url(url).header("X-API-Key", apiKey).build()
        try {
            val response = okHttpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string() ?: return@withContext emptyList()
                val result = json.decodeFromString<LosResponse>(body)
                result.results.map { sound ->
                    Asset(
                        id = "los_${sound.id}",
                        name = sound.title,
                        type = "sfx",
                        fileUrl = sound.previewUrl,
                        command = null,
                        localPath = null
                    )
                }
            } else emptyList()
        } catch (e: Exception) { emptyList() }
    }

    override suspend fun searchVideos(query: String, limit: Int): List<Asset> = emptyList()
    override suspend fun searchImages(query: String, limit: Int): List<Asset> = emptyList()
    override suspend fun getTransitions(limit: Int): List<Asset> = emptyList()

    @Serializable
    internal data class LosResponse(val results: List<LosResult>)
    @Serializable
    internal data class LosResult(val id: String, val title: String, val previewUrl: String)
}
