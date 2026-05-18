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

    // ✅ الحقول الجديدة
    val category: String = "",              // فئة النموذج: "image", "video", "tts", "analysis", "reviewer", "orchestrator", "music", "transition", "subtitle"
    val settingsUrl: String = "",           // رابط صفحة الإعدادات الخاصة بالنموذج (اختياري)
    val readmeUrl: String = "",             // رابط ملف README.md (لجلب الأنماط والوسوم)
    val supportedStyles: List<String> = emptyList(),  // الأنماط المدعومة (مثل "واقعي", "كرتوني")
    val supportsVoiceCloning: Boolean = false,       // دعم استنساخ الصوت (لنماذج TTS)

    val apiEndpoint: String? = null,        // نقطة نهاية مخصصة (اختياري)
    val parametersJson: String? = null,     // إعدادات إضافية بصيغة JSON
    val createdAt: Long = System.currentTimeMillis()
)