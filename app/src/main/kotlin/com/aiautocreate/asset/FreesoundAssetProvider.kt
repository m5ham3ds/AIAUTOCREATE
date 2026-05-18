package com.aiautocreate.data.asset

import com.aiautocreate.domain.pipeline.Asset
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
class FreesoundAssetProvider @Inject constructor(
    private val settingsRepo: AppSettingsRepository,
    private val okHttpClient: OkHttpClient
) : AssetProvider {

    private val baseUrl = "https://freesound.org/apiv2/"
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun searchSoundEffects(sceneDescription: String, limit: Int): List<Asset> = withContext(Dispatchers.IO) {
        val apiKey = settingsRepo.getStringOnce("freesound_api_key", "")
        if (apiKey.isBlank()) return@withContext emptyList()
        val url = "${baseUrl}search/text/?query=${java.net.URLEncoder.encode(sceneDescription, "UTF-8")}&fields=id,name,previews&page_size=$limit"
        val request = Request.Builder().url(url).header("Authorization", "Token $apiKey").build()
        try {
            val response = okHttpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string() ?: return@withContext emptyList()
                val result = json.decodeFromString<FreesoundResponse>(body)
                result.results.map { sound ->
                    Asset(
                        id = "freesound_${sound.id}",
                        name = sound.name,
                        type = "sfx",
                        fileUrl = sound.previews?.previewHqMp3,
                        command = null,
                        localPath = null
                    )
                }
            } else emptyList()
        } catch (e: Exception) { emptyList() }
    }

    override suspend fun searchMusic(query: String, limit: Int): List<Asset> = emptyList() // Freesound يحتوي على موسيقى لكنها أقل جودة
    override suspend fun searchVideos(query: String, limit: Int): List<Asset> = emptyList()
    override suspend fun searchImages(query: String, limit: Int): List<Asset> = emptyList()
    override suspend fun getTransitions(limit: Int): List<Asset> = emptyList()

    @Serializable
    internal data class FreesoundResponse(val results: List<FreesoundResult>)
    @Serializable
    internal data class FreesoundResult(val id: Long, val name: String, val previews: FreesoundPreviews?)
    @Serializable
    internal data class FreesoundPreviews(val previewHqMp3: String)
}