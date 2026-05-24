package com.aiautocreate.domain.usecase.model

import com.aiautocreate.data.datasource.remote.api.HuggingFaceApi
import com.aiautocreate.data.repository.AppSettingsRepository
import com.aiautocreate.domain.model.ModelConfig
import com.aiautocreate.domain.repository.IModelsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import timber.log.Timber
import javax.inject.Inject

class RefreshModelsStylesUseCase @Inject constructor(
    private val modelsRepository: IModelsRepository,
    private val settingsRepo: AppSettingsRepository,
    private val huggingFaceApi: HuggingFaceApi,
    private val okHttpClient: OkHttpClient
) {

    private val categories = listOf(
        "text", "image", "video", "tts", "analysis",
        "reviewer", "orchestrator", "music", "transition", "subtitle", "ffmpeg"
    )

    suspend fun refreshAll(): Int = withContext(Dispatchers.IO) {
        val selectedModels = getSelectedModelsFromSettings()
        if (selectedModels.isEmpty()) {
            Timber.d("لا توجد نماذج مختارة للتحديث")
            return@withContext 0
        }

        var updatedCount = 0
        for ((category, modelId) in selectedModels) {
            if (refreshSingleModel(modelId, category)) {
                updatedCount++
            }
        }
        updatedCount
    }

    private suspend fun getSelectedModelsFromSettings(): List<Pair<String, String>> {
        return categories.mapNotNull { category ->
            val modelId = settingsRepo.getSelectedModelForCategory(category)
            if (modelId.isNotBlank()) category to modelId else null
        }
    }

    private suspend fun refreshSingleModel(modelId: String, category: String): Boolean {
        return try {
            val token = settingsRepo.getHuggingFaceToken()
            if (token.isNullOrBlank()) return false

            val hfResponse = huggingFaceApi.getModelInfo(modelId, "Bearer $token")
            if (!hfResponse.isSuccessful || hfResponse.body() == null) return false
            val hfModel = hfResponse.body()!!

            val hfReadmeUrl = "https://huggingface.co/${modelId}/raw/main/README.md"
            val hfReadmeContent = fetchReadmeContent(hfReadmeUrl)
            val hfDescription = extractDescriptionFromReadme(hfReadmeContent)
            val hfTags = extractTagsFromReadme(hfReadmeContent)
            val hfStyles = extractStylesFromTags(hfTags)

            val existingModels = modelsRepository.getAllModelConfigs().first()
            val existing = existingModels.find { it.modelId == modelId }
            val savedGithubUrl = existing?.githubReadmeUrl
            val githubUrl = if (!savedGithubUrl.isNullOrBlank()) {
                savedGithubUrl
            } else {
                "https://github.com/${modelId}"
            }

            val githubContent = fetchReadmeContent(githubUrl)
            val githubDescription = extractDescriptionFromReadme(githubContent)
            val githubTags = extractTagsFromReadme(githubContent)
            val githubStyles = extractStylesFromTags(githubTags)

            val finalDescription = if (githubDescription.isNotBlank()) githubDescription else hfDescription
            val finalTags = (hfTags + githubTags).distinct()
            val finalStyles = (hfStyles + githubStyles).distinct()

            if (existing != null) {
                // ✅ استخدام updateModel (الموجود في IModelsRepository)
                val updatedModel = existing.copy(
                    description = finalDescription.take(500),
                    tags = finalTags,
                    supportedStyles = finalStyles,
                    readmeUrl = hfReadmeUrl,
                    settingsUrl = githubUrl,
                    githubReadmeUrl = githubUrl,
                    updatedAt = System.currentTimeMillis()
                )
                modelsRepository.updateModel(updatedModel)
                true
            } else {
                // ✅ استخدام addModel (الموجود في IModelsRepository)
                val newModel = ModelConfig(
                    modelId = modelId,
                    modelName = hfModel.cardData?.title?.takeIf { it.isNotBlank() } ?: modelId.split("/").last(),
                    provider = "huggingface",
                    isEnabled = true,
                    description = finalDescription,
                    pipelineTag = hfModel.pipelineTag ?: "",
                    tags = finalTags,
                    modelUrl = "https://huggingface.co/${modelId}",
                    category = category,
                    settingsUrl = githubUrl,
                    readmeUrl = hfReadmeUrl,
                    githubReadmeUrl = githubUrl,
                    supportedStyles = finalStyles,
                    supportsVoiceCloning = false,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                modelsRepository.addModel(newModel)
                true
            }
        } catch (e: Exception) {
            Timber.e(e, "فشل تحديث $modelId")
            false
        }
    }

    private suspend fun fetchReadmeContent(url: String): String = withContext(Dispatchers.IO) {
        if (url.isBlank()) return@withContext ""
        try {
            val request = Request.Builder().url(url).build()
            val response = okHttpClient.newCall(request).execute()
            if (response.isSuccessful && response.body != null) {
                response.body!!.string()
            } else ""
        } catch (e: Exception) {
            ""
        }
    }

    private fun extractDescriptionFromReadme(content: String): String {
        if (content.isBlank()) return ""
        val lines = content.lines()
        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isNotEmpty() && !trimmed.startsWith("#") && !trimmed.startsWith("[") && !trimmed.startsWith("```")) {
                return trimmed.take(300)
            }
        }
        return content.take(200).replace("\n", " ")
    }

    private fun extractTagsFromReadme(content: String): List<String> {
        val keywords = listOf(
            "text-to-image", "image-to-image", "text-to-video", "image-to-video",
            "text-to-speech", "tts", "LLM", "diffusion", "transformer", "vision",
            "multimodal", "audio", "music", "code", "translation", "summarization"
        )
        val contentLower = content.lowercase()
        return keywords.filter { contentLower.contains(it) }.distinct()
    }

    private fun extractStylesFromTags(tags: List<String>): List<String> {
        val styleMap = mapOf(
            "text-to-image" to listOf("واقعي", "فني", "إبداعي"),
            "image-to-video" to listOf("حركي", "سينمائي"),
            "text-to-speech" to listOf("طبيعي", "واضح"),
            "LLM" to listOf("محادثة", "إبداعي")
        )
        val styles = mutableSetOf<String>()
        tags.forEach { tag ->
            styleMap[tag]?.let { styles.addAll(it) }
        }
        return styles.toList().ifEmpty { listOf("عام") }
    }
}
