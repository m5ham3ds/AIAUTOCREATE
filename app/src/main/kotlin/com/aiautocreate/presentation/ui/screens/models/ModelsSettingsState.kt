package com.aiautocreate.presentation.ui.screens.models

data class ModelsSettingsState(
    val geminiApiKey: String = "",
    val geminiUrl: String = "",
    val huggingFaceToken: String = "",
    val ttsUrl: String = "",
    val ffmpegPath: String = "",
    val elevenLabsApiKey: String = "",
    val lotsofsoundsApiKey: String = "",
    val openVfxApiKey: String = "",
    val pixabayApiKey: String = "",
    val pexelsApiKey: String = "",
    val freesoundApiKey: String = "",
    val geminiKeysCsv: String = "",
    val huggingFaceTokensCsv: String = "",
    val defaultAgentModelId: String = "",
    val fallbackAgentModelsOrder: List<String> = emptyList(),
    val selectedModels: Map<String, String> = emptyMap(),
    val availableModelsByCategory: Map<String, List<ModelInfo>> = emptyMap(),
    val ttsVoiceSamplePath: String = "",
    val ttsUseVoiceClone: Boolean = false,
    val isSaving: Boolean = false,
    val isRefreshing: Boolean = false,
    val saveSuccessMessage: String? = null,
    val errorMessage: String? = null
)

data class ModelInfo(
    val modelId: String,
    val modelName: String
)
