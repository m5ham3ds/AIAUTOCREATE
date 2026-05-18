package com.aiautocreate.presentation.ui.screens.results

data class ResultsState(
    val overallProgress: Float = 0f,
    val overallStep: Int = 0,
    val overallTotal: Int = 7,
    val overallStatusText: String = "بانتظار البدء...",
    val isProcessing: Boolean = false,
    val operations: List<OperationResult> = emptyList(),
    val logs: List<String> = emptyList(),   // إضافية لمن يريد عرض السجلات النصية
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

data class OperationResult(
    val id: String,              // "script", "image", "tts", "video"...
    val title: String,           // "Script", "Image Generation"...
    val status: String,          // "pending", "in_progress", "completed", "failed"
    val progress: Float = 0f,    // 0.0 to 1.0
    val detailText: String = ""
)