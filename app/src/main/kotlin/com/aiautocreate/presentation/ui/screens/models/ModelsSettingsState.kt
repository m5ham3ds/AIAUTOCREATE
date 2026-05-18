package com.aiautocreate.presentation.ui.screens.models

data class ModelsSettingsState(
    // مفاتيح API الحالية
    val geminiApiKey: String = "",
    val geminiUrl: String = "",
    val huggingFaceToken: String = "",
    val ttsUrl: String = "",
    val ffmpegPath: String = "",
    val elevenLabsApiKey: String = "",

    // ✅ مفاتيح API الجديدة
    val lotsofsoundsApiKey: String = "",
    val openVfxApiKey: String = "",
    val pixabayApiKey: String = "",
    val pexelsApiKey: String = "",
    val freesoundApiKey: String = "",

    // خريطة الفئة → النموذج المختار
    val selectedModels: Map<String, String> = emptyMap(),

    // قوائم النماذج المتاحة لكل فئة
    val availableModelsByCategory: Map<String, List<ModelInfo>> = emptyMap(),

    // إعدادات استنساخ الصوت
    val ttsVoiceSamplePath: String = "",
    val ttsUseVoiceClone: Boolean = false,

    // حالة الحفظ
    val isSaving: Boolean = false,
    val isRefreshing: Boolean = false,
    val saveSuccessMessage: String? = null,
    val errorMessage: String? = null
)

data class ModelInfo(
    val modelId: String,
    val modelName: String
)