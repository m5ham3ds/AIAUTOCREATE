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

            // قائمة النماذج التي نريد التأكد من وجودها (مع بياناتها الكاملة)
            val requiredModels = listOf(
                ModelConfig(
                    id = 0,
                    modelId = "gemini-2.0-flash",
                    modelName = "Gemini 2.0 Flash",
                    provider = "gemini",
                    isEnabled = true,
                    description = "نموذج Gemini من Google لتوليد النصوص وتحليلها ومعالجة المهام المتعددة.",
                    pipelineTag = "text-generation",
                    tags = listOf("google", "llm", "chat"),
                    modelUrl = "https://ai.google.dev/gemini-api",
                    category = "text",
                    settingsUrl = "",
                    readmeUrl = "",
                    supportedStyles = listOf(
                        "تحليل النصوص", "تلخيص", "توليد النصوص", "مراجعة", "تصحيح لغوي", "تنسيق عام"
                    ),
                    supportsVoiceCloning = false,
                    apiEndpoint = null,
                    parametersJson = null,
                    createdAt = System.currentTimeMillis()
                ),
                ModelConfig(
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
                ),
                ModelConfig(
                    id = 0,
                    modelId = "Qwen/Qwen2.5-7B-Instruct",
                    modelName = "Qwen 2.5 7B Instruct",
                    provider = "huggingface",
                    isEnabled = true,
                    description = "نموذج Qwen من Alibaba Cloud، متخصص في توليد النصوص والفهم العميق.",
                    pipelineTag = "text-generation",
                    tags = listOf("qwen", "llm", "chat", "instruct"),
                    modelUrl = "https://huggingface.co/Qwen/Qwen2.5-7B-Instruct",
                    category = "text",
                    readmeUrl = "https://huggingface.co/Qwen/Qwen2.5-7B-Instruct/raw/main/README.md",
                    supportedStyles = listOf("أسئلة وأجوبة", "برمجة", "تحليل", "إبداع"),
                    supportsVoiceCloning = false,
                    createdAt = System.currentTimeMillis()
                ),
                ModelConfig(
                    id = 0,
                    modelId = "deepseek-ai/DeepSeek-R1",
                    modelName = "DeepSeek R1",
                    provider = "huggingface",
                    isEnabled = true,
                    description = "نموذج DeepSeek المتقدم في الاستدلال وحل المشكلات.",
                    pipelineTag = "text-generation",
                    tags = listOf("deepseek", "llm", "reasoning", "chat"),
                    modelUrl = "https://huggingface.co/deepseek-ai/DeepSeek-R1",
                    category = "text",
                    readmeUrl = "https://huggingface.co/deepseek-ai/DeepSeek-R1/raw/main/README.md",
                    supportedStyles = listOf("استدلال منطقي", "رياضيات", "برمجة", "تحليل"),
                    supportsVoiceCloning = false,
                    createdAt = System.currentTimeMillis()
                ),
                ModelConfig(
                    id = 0,
                    modelId = "meta-llama/Llama-3.2-3B-Instruct",
                    modelName = "Llama 3.2 3B Instruct",
                    provider = "huggingface",
                    isEnabled = true,
                    description = "نموذج Llama من Meta، خفيف وسريع لمهام المحادثة.",
                    pipelineTag = "text-generation",
                    tags = listOf("llama", "meta", "llm", "chat"),
                    modelUrl = "https://huggingface.co/meta-llama/Llama-3.2-3B-Instruct",
                    category = "text",
                    readmeUrl = "https://huggingface.co/meta-llama/Llama-3.2-3B-Instruct/raw/main/README.md",
                    supportedStyles = listOf("محادثة", "أسئلة وأجوبة", "تلخيص"),
                    supportsVoiceCloning = false,
                    createdAt = System.currentTimeMillis()
                )
            )

            // 1. حذف النماذج المطلوبة فقط (إذا كانت موجودة)
            requiredModels.forEach { required ->
                existingModels.find { it.modelId == required.modelId }?.let { existing ->
                    modelsRepository.deleteModelConfig(existing)
                    Timber.d("تم حذف النموذج القديم: ${existing.modelName}")
                }
            }

            // 2. إضافة النماذج من جديد (ببياناتها الجديدة)
            requiredModels.forEach { model ->
                modelsRepository.insertModelConfig(model)
                Timber.d("تم إضافة النموذج: ${model.modelName}")
            }
        }
    }
}
