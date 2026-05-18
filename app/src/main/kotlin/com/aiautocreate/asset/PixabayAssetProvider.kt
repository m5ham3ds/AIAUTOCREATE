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
class PixabayAssetProvider @Inject constructor(
    private val settingsRepo: AppSettingsRepository,
    private val okHttpClient: OkHttpClient
) : AssetProvider {

    private val baseUrl = "https://pixabay.com/api/"
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun searchVideos(query: String, limit: Int): List<Asset> = withContext(Dispatchers.IO) {
        val apiKey = settingsRepo.getStringOnce("pixabay_api_key", "")
        if (apiKey.isBlank()) return@withContext emptyList()
        val url = "${baseUrl}videos/?key=$apiKey&q=${java.net.URLEncoder.encode(query, "UTF-8")}&per_page=$limit"
        val request = Request.Builder().url(url).build()
        try {
            val response = okHttpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string() ?: return@withContext emptyList()
                val result = json.decodeFromString<PixabayVideoResponse>(body)
                result.hits.map { video ->
                    val videoUrl = video.videos?.large?.url ?: video.videos?.medium?.url ?: video.videos?.small?.url
                    Asset(
                        id = "pixabay_video_${video.id}",
                        name = video.tags?.take(50) ?: "فيديو من Pixabay",
                        type = "video",
                        fileUrl = videoUrl,
                        command = null,
                        localPath = null
                    )
                }
            } else emptyList()
        } catch (e: Exception) { emptyList() }
    }

    override suspend fun searchImages(query: String, limit: Int): List<Asset> = withContext(Dispatchers.IO) {
        val apiKey = settingsRepo.getStringOnce("pixabay_api_key", "")
        if (apiKey.isBlank()) return@withContext emptyList()
        val url = "${baseUrl}?key=$apiKey&q=${java.net.URLEncoder.encode(query, "UTF-8")}&image_type=photo&per_page=$limit"
        val request = Request.Builder().url(url).build()
        try {
            val response = okHttpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string() ?: return@withContext emptyList()
                val result = json.decodeFromString<PixabayImageResponse>(body)
                result.hits.map { image ->
                    Asset(
                        id = "pixabay_image_${image.id}",
                        name = image.tags?.take(50) ?: "صورة من Pixabay",
                        type = "image",
                        fileUrl = image.largeImageURL,
                        command = null,
                        localPath = null
                    )
                }
            } else emptyList()
        } catch (e: Exception) { emptyList() }
    }

    override suspend fun searchMusic(query: String, limit: Int): List<Asset> = emptyList()
    override suspend fun searchSoundEffects(sceneDescription: String, limit: Int): List<Asset> = emptyList()
    override suspend fun getTransitions(limit: Int): List<Asset> = emptyList()

    @Serializable
    internal data class PixabayVideoResponse(val hits: List<PixabayVideo>)
    @Serializable
    internal data class PixabayVideo(val id: Long, val tags: String?, val videos: PixabayVideoUrls?)
    @Serializable
    internal data class PixabayVideoUrls(val large: PixabayVideoSize?, val medium: PixabayVideoSize?, val small: PixabayVideoSize?)
    @Serializable
    internal data class PixabayVideoSize(val url: String)
    @Serializable
    internal data class PixabayImageResponse(val hits: List<PixabayImage>)
    @Serializable
    internal data class PixabayImage(val id: Long, val tags: String?, val largeImageURL: String)
}
