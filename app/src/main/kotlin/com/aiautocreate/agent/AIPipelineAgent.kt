package com.aiautocreate.agent

import com.aiautocreate.data.repository.AppSettingsRepository
import com.aiautocreate.domain.model.ModelConfig
import com.aiautocreate.domain.repository.IModelsRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AIPipelineAgent @Inject constructor(
    private val modelsRepo: IModelsRepository,
    private val settingsRepo: AppSettingsRepository
) {

    companion object {
        // تعريف المهام المدعومة (أضفنا المهام النصية)
        val TASKS = mapOf(
            "master" to listOf("text-generation", "text2text-generation"),
            "audio_fx" to listOf("audio-enhancement", "audio-to-audio", "text-to-speech"),
            "visual_fx" to listOf("image-to-image", "image-enhancement"),
            "transitions" to listOf("video-transition"),
            "subtitles" to listOf("automatic-speech-recognition", "text-to-video"),
            "music" to listOf("text-to-music", "music-generation"),
            "reviewer" to listOf("text-classification", "text-scoring"),
            "orchestrator" to listOf("text-generation", "text2text-generation"),
            // المهام النصية الجديدة
            "text_analysis" to listOf("text-generation", "text2text-generation", "text-classification"),
            "text_correction" to listOf("text-generation", "text2text-generation"),
            "text_processing" to listOf("text-generation", "text2text-generation")
        )
    }

    // ذاكرة مؤقتة لكل نمط ومهمة
    private val recommendedModels = mutableMapOf<String, String>()

    /**
     * يحصل على النموذج المناسب لمهمة معينة ونمط مونتاج معين.
     * يبحث أولاً في التخزين المحلي، ثم في HuggingFace (اختياري).
     */
    suspend fun getModelForTask(style: String, task: String): String? {
        val cacheKey = "${style}_$task"
        if (recommendedModels.containsKey(cacheKey)) {
            return recommendedModels[cacheKey]
        }

        // 1. البحث في النماذج المحلية النشطة والمناسبة للمهمة
        val localModel = findLocalModelForTask(task)
        if (localModel != null) {
            recommendedModels[cacheKey] = localModel.modelId
            return localModel.modelId
        }

        // 2. (اختياري) البحث عن نموذج موصى به من HuggingFace
        // سيتم تنفيذه لاحقاً إذا أردت

        return null
    }

    private suspend fun findLocalModelForTask(task: String): ModelConfig? {
        val allowedTags = TASKS[task] ?: emptyList()
        if (allowedTags.isEmpty()) return null

        val allModels = modelsRepo.getAllModelConfigs().first()
        return allModels.firstOrNull { model ->
            model.isEnabled && allowedTags.any { tag -> model.pipelineTag.equals(tag, ignoreCase = true) }
        }
    }

    /**
     * يحفظ النموذج المختار لمهمة معينة ونمط معين في DataStore.
     */
    suspend fun saveSelectedModel(style: String, task: String, modelId: String) {
        val key = "profile_${style}_${task}_model"
        settingsRepo.setString(key, modelId)
        recommendedModels["${style}_$task"] = modelId
    }

    /**
     * يقرأ النموذج المخزن لمهمة معينة ونمط معين.
     */
    suspend fun getSavedModel(style: String, task: String): String? {
        val key = "profile_${style}_${task}_model"
        return settingsRepo.getStringOnce(key, "").takeIf { it.isNotBlank() }
    }
}