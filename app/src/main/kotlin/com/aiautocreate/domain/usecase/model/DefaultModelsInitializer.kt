package com.aiautocreate.domain.usecase.model

import com.aiautocreate.domain.model.ModelConfig
import com.aiautocreate.domain.repository.IModelsRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultModelsInitializer @Inject constructor(
    private val modelsRepository: IModelsRepository
) {

    /**
     * يجب استدعاء هذه الدالة عند بدء التشغيل (في `Application` أو `SplashScreen`).
     * تقوم بإضافة النماذج الافتراضية إذا لم يكن هناك أي نموذج في قاعدة البيانات.
     */
    fun initializeIfNeeded() {
        runBlocking {
            val existingModels = modelsRepository.getAllModelConfigs().first()
            if (existingModels.isNotEmpty()) {
                // يوجد نماذج بالفعل، لا داعي للإضافة
                return@runBlocking
            }

            // إضافة نموذج Gemini (Google)
            val geminiModel = ModelConfig(
                id = 0,
                modelId = "gemini-2.0-flash",
                modelName = "Gemini 2.0 Flash",
                provider = "google",
                isEnabled = true,
                description = "نموذج Gemini من Google لتوليد النصوص وتحليلها ومعالجة المهام المتعددة.",
                pipelineTag = "text-generation",
                tags = listOf("google", "llm", "chat"),
                modelUrl = "https://ai.google.dev/gemini-api",
                category = "analysis",          // مناسب للتحليل والمعالجة
                settingsUrl = "",
                readmeUrl = "",
                supportedStyles = emptyList(),
                supportsVoiceCloning = false,
                apiEndpoint = null,
                parametersJson = null,
                createdAt = System.currentTimeMillis()
            )
            modelsRepository.insertModelConfig(geminiModel)

            // يمكنك إضافة نماذج HuggingFace أساسية هنا إذا أردت
            // مثلاً نموذج افتراضي للصور، فيديو، إلخ.
            // لكن الأفضل ترك المستخدم يضيفها بنفسه.
        }
    }
}