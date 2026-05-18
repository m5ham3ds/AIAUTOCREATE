package com.aiautocreate.presentation.ui.screens.audio

data class AudioReconstructorState(
    val inputAudioPath: String? = null,
    val extractedText: String? = null,
    val generatedAudioPath: String? = null,
    
    // خيارات المعالجة
    val selectedOption: String = "noise", // "noise", "freq", "restore", "enhance"
    val processingStrength: Float = 85f,

    val isProcessing: Boolean = false,
    val isAnalyzing: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)