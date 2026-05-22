package com.aiautocreate.data.repository

import android.util.Base64
import androidx.datastore.preferences.core.*
import com.aiautocreate.AIAutoCreateApp
import com.aiautocreate.data.datasource.local.datastore.DataStoreManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppSettingsRepository @Inject constructor(
    private val dataStore: DataStoreManager
) {
    // ================== مفاتيح API ==================
    val geminiKey: Flow<String> = dataStore.getStringFlow("gemini_key", "")
    val geminiUrl: Flow<String> = dataStore.getStringFlow("gemini_url", "")
    val hfToken: Flow<String> = dataStore.getStringFlow("hf_token", "")
    val ttsUrl: Flow<String> = dataStore.getStringFlow("tts_url", "")
    val ffmpegPath: Flow<String> = dataStore.getStringFlow("ffmpeg_path", "")
    val elevenLabsApiKey: Flow<String> = dataStore.getStringFlow("elevenlabs_key", "")

    // ================== قوائم النماذج (CSV) ==================
    val imageModelsCsv: Flow<String> = dataStore.getStringFlow("image_models_csv", "")
    val videoModelsCsv: Flow<String> = dataStore.getStringFlow("video_models_csv", "")
    val ttsModelsCsv: Flow<String> = dataStore.getStringFlow("tts_models_csv", "")

    // ================== الأساليب (CSV) ==================
    val imageStylesCsv: Flow<String> = dataStore.getStringFlow("image_styles_csv", "واقعي,كرتوني,خيالي")
    val coverStylesCsv: Flow<String> = dataStore.getStringFlow("cover_styles_csv", "غلاف بسيط,غلاف ملون,غلاف فني")
    val videoStylesCsv: Flow<String> = dataStore.getStringFlow("video_styles_csv", "درامي,موسيقي,اكشن")
    val montageStylesCsv: Flow<String> = dataStore.getStringFlow("montage_styles_csv", "قصص وروايات,حماسي وجذاب,احترافية وأنيق")

    // ================== اختيارات المستخدم ==================
    val selectedImageStyle: Flow<String> = dataStore.getStringFlow("sel_image_style", "واقعي")
    val selectedCoverStyle: Flow<String> = dataStore.getStringFlow("sel_cover_style", "غلاف بسيط")
    val selectedVoice: Flow<String> = dataStore.getStringFlow("sel_voice", "صوت1")
    val selectedVideoStyle: Flow<String> = dataStore.getStringFlow("sel_video_style", "درامي")
    val selectedMontageStyle: Flow<String> = dataStore.getStringFlow("sel_montage_style", "قصص وروايات")

    // ================== إعدادات أخرى ==================
    val defaultMinutes: Flow<String> = dataStore.getStringFlow("def_minutes", "00")
    val defaultSeconds: Flow<String> = dataStore.getStringFlow("def_seconds", "30")
    val defaultAspect: Flow<String> = dataStore.getStringFlow("def_aspect", "16:9")
    val defaultQuality: Flow<String> = dataStore.getStringFlow("def_quality", "1080p")
    val voiceSamplePath: Flow<String> = dataStore.getStringFlow("voice_sample_path", "")
    val useVoiceClone: Flow<Boolean> = dataStore.getBoolFlow("use_voice_clone", false)

    // ================== دوال الكتابة ==================
    suspend fun setString(key: String, value: String) = dataStore.putString(key, value)
    suspend fun setBoolean(key: String, value: Boolean) = dataStore.putBoolean(key, value)

    // ================== دوال مساعدة ==================
    suspend fun getStringOnce(key: String, default: String = ""): String {
        return dataStore.getStringFlow(key, default).first()
    }

    suspend fun getBoolOnce(key: String, default: Boolean = false): Boolean {
        return dataStore.getBoolFlow(key, default).first()
    }

    // ✅ دوال النماذج المختارة
    suspend fun getSelectedModelForCategory(category: String): String {
        return getStringOnce("selected_model_$category", "")
    }

    suspend fun setSelectedModelForCategory(category: String, modelId: String) {
        setString("selected_model_$category", modelId)
    }

    // ✅ دوال استنساخ الصوت
    suspend fun setVoiceSampleForTtsModel(modelId: String, samplePath: String) {
        setString("tts_voice_sample_${modelId}", samplePath)
    }

    suspend fun getVoiceSampleForTtsModel(modelId: String): String? {
        val path = getStringOnce("tts_voice_sample_${modelId}", "")
        return if (path.isNotBlank()) path else null
    }

    suspend fun getVoiceSampleBase64(modelId: String): String? {
        val path = getVoiceSampleForTtsModel(modelId) ?: return null
        return try {
            val file = File(path)
            if (file.exists()) {
                val bytes = file.readBytes()
                Base64.encodeToString(bytes, Base64.NO_WRAP)
            } else null
        } catch (e: Exception) {
            null
        }
    }

    // ✅ دوال قائمة مفاتيح Gemini (CSV)
    suspend fun setGeminiKeysCsv(keysCsv: String) = setString("gemini_keys_csv", keysCsv)
    suspend fun getGeminiKeysCsv(): String = getStringOnce("gemini_keys_csv", "")
    suspend fun addToGeminiKeysCsv(newKey: String) {
        val current = getGeminiKeysCsv()
        val items = csvToList(current).toMutableList()
        if (!items.contains(newKey)) {
            items.add(newKey)
            setGeminiKeysCsv(items.joinToString(","))
        }
    }
    suspend fun removeFromGeminiKeysCsv(keyToRemove: String) {
        val current = getGeminiKeysCsv()
        val items = csvToList(current).toMutableList()
        if (items.remove(keyToRemove)) {
            setGeminiKeysCsv(items.joinToString(","))
        }
    }

// ✅ دوال قائمة توكنات HuggingFace (CSV)
suspend fun setHuggingFaceTokensCsv(tokensCsv: String) = setString("hf_tokens_csv", tokensCsv)
suspend fun getHuggingFaceTokensCsv(): String = getStringOnce("hf_tokens_csv", "")
suspend fun addToHuggingFaceTokensCsv(newToken: String) {
    val current = getHuggingFaceTokensCsv()
    val items = csvToList(current).toMutableList()
    if (!items.contains(newToken)) {
        items.add(newToken)
        setHuggingFaceTokensCsv(items.joinToString(","))
    }
}
suspend fun removeFromHuggingFaceTokensCsv(tokenToRemove: String) {
    val current = getHuggingFaceTokensCsv()
    val items = csvToList(current).toMutableList()
    if (items.remove(tokenToRemove)) {
        setHuggingFaceTokensCsv(items.joinToString(","))
    }
}
    
    // ✅ دوال المسارات الجديدة (باستخدام المجلد الخاص بالتطبيق)
    private fun getAppRoot(): String {
        val context = AIAutoCreateApp.instance
        return context.getExternalFilesDir(null)?.absolutePath ?: context.filesDir.absolutePath
    }

    suspend fun getProjectsRoot(): String {
        return getStringOnce("path_projects", "${getAppRoot()}/AIAutoCreate/PROJECTS")
    }

    suspend fun getProjectTempDir(projectId: Long): String {
        return "${getProjectsRoot()}/$projectId"
    }

    suspend fun getScriptsDir(projectId: Long): String {
        return "${getProjectTempDir(projectId)}/SCRIPT"
    }

    suspend fun getImagesDir(projectId: Long): String {
        return "${getProjectTempDir(projectId)}/IMAGES"
    }

    suspend fun getAudiosDir(projectId: Long): String {
        return "${getProjectTempDir(projectId)}/AUDIOS"
    }

    suspend fun getVideosDir(projectId: Long): String {
        return "${getProjectTempDir(projectId)}/VIDEOS"
    }

    suspend fun getFinalDir(projectId: Long): String {
        return "${getProjectTempDir(projectId)}/FINAL"
    }

    suspend fun getCacheFfmpegTempDir(): String {
        return getStringOnce("cache_ffmpeg_temp", "${getAppRoot()}/AIAutoCreate/CACHE/FFMPEG_TEMP")
    }

    suspend fun getAssetsMusicDir(): String {
        return getStringOnce("assets_music", "${getAppRoot()}/AIAutoCreate/ASSETS/MUSIC")
    }

    suspend fun getAssetsSfxDir(): String {
        return getStringOnce("assets_sfx", "${getAppRoot()}/AIAutoCreate/ASSETS/SFX")
    }

    suspend fun getAssetsTransitionsDir(): String {
        return getStringOnce("assets_transitions", "${getAppRoot()}/AIAutoCreate/ASSETS/TRANSITIONS")
    }

    fun csvToList(csv: String): List<String> {
        return csv.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    }

    suspend fun addToCsv(key: String, newItem: String) {
        val current = getStringOnce(key, "")
        val items = csvToList(current).toMutableList()
        if (!items.contains(newItem)) {
            items.add(newItem)
            setString(key, items.joinToString(","))
        }
    }

    suspend fun getCacheAssetsDir(): String {
        return getStringOnce("cache_assets", "${getAppRoot()}/AIAutoCreate/CACHE/ASSETS")
    }

    suspend fun getErrorsDir(): String {
        return getStringOnce("path_errors", "${getAppRoot()}/AIAutoCreate/ERRORS")
    }
    
    suspend fun removeFromCsv(key: String, itemToRemove: String) {
        val current = getStringOnce(key, "")
        val items = csvToList(current).toMutableList()
        if (items.remove(itemToRemove)) {
            setString(key, items.joinToString(","))
        }
    }
}

// ✅ دوال إعدادات الوكيل
suspend fun getDefaultAgentModelId(): String = getStringOnce("default_agent_model_id", "")
suspend fun setDefaultAgentModelId(modelId: String) = setString("default_agent_model_id", modelId)

suspend fun getFallbackAgentModelsOrder(): List<String> {
    val csv = getStringOnce("fallback_agent_models_order", "")
    return if (csv.isNotBlank()) csvToList(csv) else emptyList()
}
suspend fun setFallbackAgentModelsOrder(modelIds: List<String>) {
    setString("fallback_agent_models_order", modelIds.joinToString(","))
}

// ================== Extensions ==================
fun DataStoreManager.getBoolFlow(key: String, default: Boolean): Flow<Boolean> {
    val prefKey = booleanPreferencesKey(key)
    return dataStore.data.map { it[prefKey] ?: default }
}

fun DataStoreManager.getStringFlow(key: String, default: String): Flow<String> {
    val prefKey = stringPreferencesKey(key)
    return dataStore.data.map { it[prefKey] ?: default }
}

suspend fun DataStoreManager.putString(key: String, value: String) =
    dataStore.edit { it[stringPreferencesKey(key)] = value }

suspend fun DataStoreManager.putBoolean(key: String, value: Boolean) =
    dataStore.edit { it[booleanPreferencesKey(key)] = value }
