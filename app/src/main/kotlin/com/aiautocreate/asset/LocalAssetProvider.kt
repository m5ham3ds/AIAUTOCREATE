package com.aiautocreate.asset

import com.aiautocreate.AIAutoCreateApp
import com.aiautocreate.agent.Asset
import com.aiautocreate.data.repository.AppSettingsRepository
import com.aiautocreate.domain.service.AssetProvider
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * مزود الأصول المحلية (يقرأ من مجلدات ASSETS داخل التطبيق)
 * المسارات:
 * - ASSETS/MUSIC (للموسيقى)
 * - ASSETS/SFX (للمؤثرات الصوتية)
 * - ASSETS/TRANSITIONS (للانتقالات)
 * - ASSETS/IMAGES (للصور)
 * - ASSETS/VIDEOS (للفيديوهات)
 */
@Singleton
class LocalAssetProvider @Inject constructor(
    private val settingsRepo: AppSettingsRepository
) : AssetProvider {

    private fun getAppRoot(): String {
        val context = AIAutoCreateApp.instance
        return context.getExternalFilesDir(null)?.absolutePath ?: context.filesDir.absolutePath
    }

    private suspend fun getAssetFiles(type: String): List<File> {
        val dirPath = when (type) {
            "music" -> settingsRepo.getAssetsMusicDir()
            "sfx" -> settingsRepo.getAssetsSfxDir()
            "transition" -> settingsRepo.getAssetsTransitionsDir()
            "image" -> settingsRepo.getStringOnce("assets_images", "${getAppRoot()}/AIAutoCreate/ASSETS/IMAGES")
            "video" -> settingsRepo.getStringOnce("assets_videos", "${getAppRoot()}/AIAutoCreate/ASSETS/VIDEOS")
            else -> return emptyList()
        }
        val dir = File(dirPath)
        return if (dir.exists() && dir.isDirectory) {
            dir.listFiles()?.filter { it.isFile } ?: emptyList()
        } else emptyList()
    }

    private fun fileToAsset(file: File, type: String): Asset {
        val name = file.nameWithoutExtension
        val id = "local_${type}_${name.hashCode()}"
        return Asset(
            id = id,
            name = name,
            command = when (type) {
                "transition" -> name.lowercase()
                else -> null
            },
            fileUrl = null,
            localPath = file.absolutePath
        )
    }

    override suspend fun searchVideos(query: String, limit: Int): List<Asset> {
        return getAssetFiles("video")
            .filter { it.name.contains(query, ignoreCase = true) }
            .take(limit)
            .map { fileToAsset(it, "video") }
    }

    override suspend fun searchImages(query: String, limit: Int): List<Asset> {
        return getAssetFiles("image")
            .filter { it.name.contains(query, ignoreCase = true) }
            .take(limit)
            .map { fileToAsset(it, "image") }
    }

    override suspend fun searchMusic(query: String, limit: Int): List<Asset> {
        return getAssetFiles("music")
            .filter { it.name.contains(query, ignoreCase = true) }
            .take(limit)
            .map { fileToAsset(it, "music") }
    }

    override suspend fun searchSoundEffects(sceneDescription: String, limit: Int): List<Asset> {
        val keywords = sceneDescription
            .split(" ", ",", "،", ".", "!", "؟")
            .map { it.trim() }
            .filter { it.length > 2 }
            .distinct()
        return getAssetFiles("sfx")
            .filter { file ->
                keywords.any { keyword -> file.name.contains(keyword, ignoreCase = true) }
            }
            .take(limit)
            .map { fileToAsset(it, "sfx") }
    }

    override suspend fun getTransitions(limit: Int): List<Asset> {
        return getAssetFiles("transition")
            .take(limit)
            .map { fileToAsset(it, "transition") }
    }
}
