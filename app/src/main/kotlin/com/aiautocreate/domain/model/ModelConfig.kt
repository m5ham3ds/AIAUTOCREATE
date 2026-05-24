package com.aiautocreate.domain.model

data class ModelConfig(
    val id: Long = 0,
    val modelId: String,                    // المعرف الفريد في HuggingFace
    val modelName: String,                  // اسم العرض
    val provider: String,                   // "huggingface", "google", "openai", ...
    val isEnabled: Boolean = true,
    val description: String = "",
    val pipelineTag: String = "",           // تصنيف المهمة (text-generation, image-to-image...)
    val tags: List<String> = emptyList(),   // وسوم إضافية
    val modelUrl: String = "",              // رابط النموذج على HuggingFace

    // الحقول الأساسية
    val category: String = "",              // فئة النموذج
    val settingsUrl: String = "",           // رابط صفحة الإعدادات (قد يكون HuggingFace أو GitHub)
    val readmeUrl: String = "",             // رابط README من HuggingFace
    val supportedStyles: List<String> = emptyList(),
    val supportsVoiceCloning: Boolean = false,

    // ✅ حقل جديد: رابط README من GitHub
    val githubReadmeUrl: String? = null,    // رابط GitHub README (اختياري)

    val apiEndpoint: String? = null,
    val parametersJson: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()  // إضافة حقل للتتبع
)
