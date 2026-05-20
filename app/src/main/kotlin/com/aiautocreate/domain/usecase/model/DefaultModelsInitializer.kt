package com.aiautocreate.domain.usecase.model

import com.aiautocreate.domain.model.ModelConfig
import com.aiautocreate.domain.repository.IModelsRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultModelsInitializer @Inject constructor(
    private val modelsRepository: IModelsRepository
) {

    fun initializeIfNeeded() {
        runBlocking {
            val existingModels = modelsRepository.getAllModelConfigs().first()
            
            val geminiExists = existingModels.any { it.modelId == "gemini-2.0-flash" }
            val flanExists = existingModels.any { it.modelId == "google/flan-t5-base" }

            // 1. نموذج Gemini (فئة النصوص) - أضفه إذا لم يكن موجوداً
            if (!geminiExists) {
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
                    category = "text",
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
                modelsRepository.insertModelConfig(geminiModel)
                Timber.d("تم إضافة نموذج Gemini (لم يكن موجوداً)")
            } else {
                Timber.d("نموذج Gemini موجود مسبقاً، تم تخطي الإضافة")
            }

            // 2. نموذج Flan T5 Base (فئة التحليل) - أضفه إذا لم يكن موجوداً
            if (!flanExists) {
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
                Timber.d("تم إضافة نموذج Flan T5 Base (لم يكن موجوداً)")
            } else {
                Timber.d("نموذج Flan T5 Base موجود مسبقاً، تم تخطي الإضافة")
            }
        }
    }
}
