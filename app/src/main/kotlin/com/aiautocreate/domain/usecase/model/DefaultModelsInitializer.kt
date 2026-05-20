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

            // ✅ إضافة نموذج Gemini (Google) مع أنماط نصية افتراضية
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
                category = "text",   // ✅ فئة النصوص
                settingsUrl = "",
                readmeUrl = "",
                supportedStyles = listOf(
                    "تحليل النصوص",
                    "تلخيص",
                    "توليد النصوص",
                    "مراجعة",
                    "تصحيح لغوي",
                    "تنسيق عام"
                ),
                supportsVoiceCloning = false,
                apiEndpoint = null,
                parametersJson = null,
                createdAt = System.currentTimeMillis()
            )
            // ✅ إدراج نموذج Gemini في قاعدة البيانات (كانت هذه هي المشكلة الأساسية)
            modelsRepository.insertModelConfig(geminiModel)

            // (اختياري) نموذج إضافي لتحليل النصوص من HuggingFace
            val hfAnalysisModel = ModelConfig(
                id = 0,
                modelId = "google/flan-t5-base",
                modelName = "Flan T5 Base",
                provider = "huggingface",
                isEnabled = true,
                description = "نموذج تحليل نصوص متعدد المهام",
                pipelineTag = "text2text-generation",
                tags = listOf("text-generation", "analysis"),
                modelUrl = "https://huggingface.co/google/flan-t5-base",
                category = "analysis",
                readmeUrl = "https://huggingface.co/google/flan-t5-base/raw/main/README.md",
                supportedStyles = listOf("أسئلة وأجوبة", "تلخيص", "ترجمة"),
                supportsVoiceCloning = false,
                createdAt = System.currentTimeMillis()
            )
            modelsRepository.insertModelConfig(hfAnalysisModel)
        }
    }
}
