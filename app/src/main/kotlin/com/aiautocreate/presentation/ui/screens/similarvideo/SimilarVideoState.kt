package com.aiautocreate.presentation.ui.screens.similarvideo

data class SimilarVideoState(
    val selectedVideoPath: String? = null,
    val selectedVideoName: String? = null,

    // إعدادات التصدير
    val selectedResolution: String = "4K",       // "4K" or "1080p"
    val selectedFps: String = "30fps",           // "30fps" or "60fps"

    // نتيجة الاستخراج
    val extractedDescription: String? = null,
    val generatedScript: String? = null,
    val generatedVideoPath: String? = null,

    // حالة المعالجة
    val isLoading: Boolean = false,
    val progress: Float = 0f,
    val progressText: String = "",
    val logs: List<String> = emptyList(),
    val errorMessage: String? = null,
    val successMessage: String? = null
)