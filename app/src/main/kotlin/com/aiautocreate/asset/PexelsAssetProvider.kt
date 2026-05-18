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
class PexelsAssetProvider @Inject constructor(
    private val settingsRepo: AppSettingsRepository,
    private val okHttpClient: OkHttpClient
) : AssetProvider {

    private val baseUrl = "https://api.pexels.com/v1/"
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun searchVideos(query: String, limit: Int): List<Asset> = withContext(Dispatchers.IO) {
        val apiKey = settingsRepo.getStringOnce("pexels_api_key", "")
        if (apiKey.isBlank()) return@withContext emptyList()
        val url = "${baseUrl}videos/search?query=${java.net.URLEncoder.encode(query, "UTF-8")}&per_page=$limit"
        val request = Request.Builder().url(url).header("Authorization", apiKey).build()
        try {
            val response = okHttpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string() ?: return@withContext emptyList()
                val result = json.decodeFromString<PexelsVideoResponse>(body)
                result.videos.map { video ->
                    // نأخذ أول ملف فيديو متاح (عادةً يكون أفضل جودة)
                    val videoFile = video.videoFiles.firstOrNull()
                    Asset(
                        id = "pexels_${video.id}",
                        name = video.user?.name ?: "فيديو من Pexels",
                        type = "video",
                        fileUrl = videoFile?.link,
                        command = null,
                        localPath = null
                    )
                }
            } else emptyList()
        } catch (e: Exception) { emptyList() }
    }

    override suspend fun searchImages(query: String, limit: Int): List<Asset> = withContext(Dispatchers.IO) {
        val apiKey = settingsRepo.getStringOnce("pexels_api_key", "")
        if (apiKey.isBlank()) return@withContext emptyList()
        val url = "${baseUrl}search?query=${java.net.URLEncoder.encode(query, "UTF-8")}&per_page=$limit"
        val request = Request.Builder().url(url).header("Authorization", apiKey).build()
        try {
            val response = okHttpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string() ?: return@withContext emptyList()
                val result = json.decodeFromString<PexelsImageResponse>(body)
                result.photos.map { photo ->
                    Asset(
                        id = "pexels_${photo.id}",
                        name = photo.photographer ?: "صورة من Pexels",
                        type = "image",
                        fileUrl = photo.src?.large,
                        command = null,
                        localPath = null
                    )
                }
            } else emptyList()
        } catch (e: Exception) { emptyList() }
    }

    override suspend fun searchMusic(query: String, limit: Int): List<Asset> = emptyList() // Pexels لا يوفر موسيقى
    override suspend fun searchSoundEffects(sceneDescription: String, limit: Int): List<Asset> = emptyList()
    override suspend fun getTransitions(limit: Int): List<Asset> = emptyList()
}

// دوال JSON الداخلية (يمكن نقلها إلى ملف منفصل)
@Serializable
internal data class PexelsVideoResponse(val videos: List<PexelsVideo>)
@Serializable
internal data class PexelsVideo(val id: Int, val user: PexelsUser?, val videoFiles: List<PexelsVideoFile>)
@Serializable
internal data class PexelsVideoFile(val link: String)
@Serializable
internal data class PexelsUser(val name: String)
@Serializable
internal data class PexelsImageResponse(val photos: List<PexelsPhoto>)
@Serializable
internal data class PexelsPhoto(val id: Int, val photographer: String?, val src: PexelsSrc?)
@Serializable
internal data class PexelsSrc(val large: String)