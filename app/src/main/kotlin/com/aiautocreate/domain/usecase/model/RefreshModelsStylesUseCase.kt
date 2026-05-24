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

    // الفئات التي يمكن تحديثها (نصوص لا تحتاج تحديث عادة)
    private val updatableCategories = setOf(
        "image", "video", "tts", "analysis", "reviewer",
        "orchestrator", "music", "transition", "subtitle", "ffmpeg"
    )

    private val categories = listOf(
        "text", "image", "video", "tts", "analysis",
        "reviewer", "orchestrator", "music", "transition", "subtitle", "ffmpeg"
    )

    /**
     * @return عدد النماذج التي تم تحديثها بنجاح
     */
    suspend fun refreshAll(): Int = withContext(Dispatchers.IO) {
        val selectedModels = getSelectedModelsFromSettings()
        if (selectedModels.isEmpty()) {
            Timber.w("لا توجد نماذج مختارة. الرجاء اختيار نماذج في إعدادات النماذج.")
            return@withContext 0
        }

        var updatedCount = 0
        for ((category, modelId) in selectedModels) {
            // تخطي النماذج من فئة "text" لأنها لا تحتوي على أنماط README عادة
            if (category == "text") {
                Timber.d("تخطي نموذج نصوص $modelId (لا يحتاج تحديث أنماط)")
                continue
            }
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
            // إذا لم يكن النموذج من HuggingFace (لا يحتوي على "/" وليس gemini-2.0-flash)، نتخطى
            if (!modelId.contains("/") && modelId != "gemini-2.0-flash") {
                Timber.d("النموذج $modelId ليس من HuggingFace، تخطي التحديث")
                return false
            }

            val token = settingsRepo.getHuggingFaceToken()
            if (token.isNullOrBlank()) {
                Timber.e("لا يوجد توكن HuggingFace لتحديث $modelId")
                return false
            }

            // 1. جلب البيانات من HuggingFace API
            val hfResponse = huggingFaceApi.getModelInfo(modelId, "Bearer $token")
            if (!hfResponse.isSuccessful || hfResponse.body() == null) {
                Timber.w("فشل جلب بيانات HuggingFace للنموذج $modelId (كود ${hfResponse.code()})")
                // محاولة تحديث من README المخزن فقط إذا كان موجوداً مسبقاً
                return updateFromExistingReadme(modelId, category)
            }
            val hfModel = hfResponse.body()!!

            // 2. جلب README من HuggingFace
            val hfReadmeUrl = "https://huggingface.co/${modelId}/raw/main/README.md"
            val hfReadmeContent = fetchReadmeContent(hfReadmeUrl)
            val hfDescription = extractDescriptionFromReadme(hfReadmeContent)
            val hfTags = extractTagsFromReadme(hfReadmeContent)
            val hfStyles = extractStylesFromTags(hfTags)

            // 3. البحث عن النموذج الحالي
            val existingModels = modelsRepository.getAllModelConfigs().first()
            val existing = existingModels.find { it.modelId == modelId }
            val savedGithubUrl = existing?.githubReadmeUrl
            val githubUrl = if (!savedGithubUrl.isNullOrBlank()) {
                savedGithubUrl
            } else {
                "https://github.com/${modelId}"
            }

            // 4. جلب README من GitHub
            val githubContent = fetchReadmeContent(githubUrl)
            val githubDescription = extractDescriptionFromReadme(githubContent)
            val githubTags = extractTagsFromReadme(githubContent)
            val githubStyles = extractStylesFromTags(githubTags)

            // 5. دمج البيانات
            val finalDescription = if (githubDescription.isNotBlank()) githubDescription else hfDescription
            val finalTags = (hfTags + githubTags).distinct()
            val finalStyles = (hfStyles + githubStyles).distinct()

            // 6. حفظ أو تحديث النموذج
            if (existing != null) {
                val updatedModel = existing.copy(
                    description = finalDescription.take(500),
                    tags = finalTags,
                    supportedStyles = finalStyles,
                    readmeUrl = hfReadmeUrl,
                    settingsUrl = githubUrl,
                    githubReadmeUrl = githubUrl,
                    updatedAt = System.currentTimeMillis()
                )
                modelsRepository.updateModelConfig(updatedModel)
                Timber.d("تم تحديث النموذج $modelId")
            } else {
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
                modelsRepository.insertModelConfig(newModel)
                Timber.d("تم إضافة النموذج الجديد $modelId")
            }
            true
        } catch (e: Exception) {
            Timber.e(e, "استثناء أثناء تحديث $modelId")
            updateFromExistingReadme(modelId, category)
        }
    }

    private suspend fun updateFromExistingReadme(modelId: String, category: String): Boolean {
        return try {
            val existingModels = modelsRepository.getAllModelConfigs().first()
            val existing = existingModels.find { it.modelId == modelId }
            if (existing == null) {
                Timber.e("النموذج $modelId غير موجود في قاعدة البيانات")
                return false
            }
            val githubUrl = existing.githubReadmeUrl
            if (githubUrl.isNullOrBlank()) {
                Timber.d("لا يوجد GitHub README للنموذج $modelId")
                return false
            }
            val content = fetchReadmeContent(githubUrl)
            if (content.isBlank()) return false
            val tags = extractTagsFromReadme(content)
            val styles = extractStylesFromTags(tags)
            val description = extractDescriptionFromReadme(content)
            val updatedModel = existing.copy(
                description = description.take(500),
                tags = tags,
                supportedStyles = styles,
                updatedAt = System.currentTimeMillis()
            )
            modelsRepository.updateModelConfig(updatedModel)
            Timber.d("تم تحديث $modelId من GitHub README فقط")
            true
        } catch (e: Exception) {
            Timber.e(e, "فشل التحديث الاحتياطي لـ $modelId")
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
