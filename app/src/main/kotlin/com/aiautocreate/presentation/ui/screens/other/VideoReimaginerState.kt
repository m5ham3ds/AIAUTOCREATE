package com.aiautocreate.presentation.ui.screens.other

data class VideoReimaginerState(
    // الفيديو المصدر
    val selectedVideoPath: String? = null,
    val selectedVideoName: String? = null,

    // إعدادات التحسين
    val selectedResolution: String = "4K",
    val selectedFps: String = "60fps",
    val enhanceColors: Boolean = true,
    val reduceNoise: Boolean = true,

    // حالة المعالجة
    val isProcessing: Boolean = false,
    val progress: Float = 0f,
    val progressText: String = "",
    val logs: List<String> = emptyList(),
    val enhancedVideoPath: String? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null
)