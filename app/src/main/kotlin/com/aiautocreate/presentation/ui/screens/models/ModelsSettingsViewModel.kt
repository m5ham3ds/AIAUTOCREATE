package com.aiautocreate.presentation.ui.screens.models

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.aiautocreate.data.repository.AppSettingsRepository
import com.aiautocreate.domain.repository.IModelsRepository
import com.aiautocreate.domain.usecase.model.RefreshModelsStylesUseCase
import com.aiautocreate.worker.BackgroundStyleRefresher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ModelsSettingsViewModel @Inject constructor(
    private val settingsRepo: AppSettingsRepository,
    private val modelsRepo: IModelsRepository,
    private val refreshStylesUseCase: RefreshModelsStylesUseCase,
    private val application: Application
) : ViewModel() {

    private val _state = MutableStateFlow(ModelsSettingsState())
    val state: StateFlow<ModelsSettingsState> = _state.asStateFlow()

    private val categories = listOf(
        "image" to "نموذج توليد الصور",
        "video" to "نموذج تحويل الصورة إلى فيديو",
        "tts" to "نموذج تحويل النص إلى صوت",
        "analysis" to "نموذج التحليل والمعالجة",
        "reviewer" to "نموذج مراجعة وتصحيح",
        "orchestrator" to "نموذج التنسيق العام",
        "music" to "نموذج توليد موسيقى",
        "transition" to "نموذج الانتقالات",
        "subtitle" to "نموذج الترجمة"
    )

    init {
        loadSettings()
        loadModelsByCategory()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            val geminiKey = settingsRepo.getStringOnce("gemini_key")
            val geminiUrl = settingsRepo.getStringOnce("gemini_url")
            val hfToken = settingsRepo.getStringOnce("hf_token")
            val ttsUrl = settingsRepo.getStringOnce("tts_url")
            val ffmpegPath = settingsRepo.getStringOnce("ffmpeg_path")
            val elevenKey = settingsRepo.getStringOnce("elevenlabs_key")

            // ✅ قراءة المفاتيح الجديدة
            val lotsofsoundsKey = settingsRepo.getStringOnce("lotsofsounds_api_key", "")
            val openVfxKey = settingsRepo.getStringOnce("openvfx_api_key", "")
            val pixabayKey = settingsRepo.getStringOnce("pixabay_api_key", "")
            val pexelsKey = settingsRepo.getStringOnce("pexels_api_key", "")
            val freesoundKey = settingsRepo.getStringOnce("freesound_api_key", "")

            // قراءة النماذج المختارة لكل فئة
            val selected = categories.mapNotNull { (category, _) ->
                val modelId = settingsRepo.getStringOnce("selected_model_$category", "")
                if (modelId.isNotBlank()) category to modelId else null
            }.toMap()

            // قراءة إعدادات استنساخ الصوت
            val currentTtsModelId = selected["tts"] ?: ""
            val ttsSamplePath = if (currentTtsModelId.isNotBlank()) {
                settingsRepo.getStringOnce("tts_voice_sample_${currentTtsModelId}", "")
            } else ""
            val ttsUseClone = if (currentTtsModelId.isNotBlank()) {
                settingsRepo.getBoolOnce("tts_use_clone_${currentTtsModelId}", false)
            } else false

            _state.update {
                it.copy(
                    geminiApiKey = geminiKey,
                    geminiUrl = geminiUrl,
                    huggingFaceToken = hfToken,
                    ttsUrl = ttsUrl,
                    ffmpegPath = ffmpegPath,
                    elevenLabsApiKey = elevenKey,
                    lotsofsoundsApiKey = lotsofsoundsKey,
                    openVfxApiKey = openVfxKey,
                    pixabayApiKey = pixabayKey,
                    pexelsApiKey = pexelsKey,
                    freesoundApiKey = freesoundKey,
                    selectedModels = selected,
                    ttsVoiceSamplePath = ttsSamplePath,
                    ttsUseVoiceClone = ttsUseClone
                )
            }
        }
    }

    private fun loadModelsByCategory() {
        viewModelScope.launch {
            modelsRepo.getAllModelConfigs()
                .catch { e ->
                    _state.update { it.copy(errorMessage = "فشل تحميل النماذج: ${e.message}") }
                }
                .collect { allModels ->
                    val enabledModels = allModels.filter { it.isEnabled }
                    val grouped = categories.associate { (category, _) ->
                        category to enabledModels.filter { it.category == category }
                            .map { ModelInfo(it.modelId, it.modelName) }
                    }
                    _state.update { it.copy(availableModelsByCategory = grouped) }
                }
        }
    }

    // دوال تغيير القيم الأساسية
    fun onGeminiKeyChanged(v: String) = _state.update { it.copy(geminiApiKey = v) }
    fun onGeminiUrlChanged(v: String) = _state.update { it.copy(geminiUrl = v) }
    fun onHuggingFaceTokenChanged(v: String) = _state.update { it.copy(huggingFaceToken = v) }
    fun onTtsUrlChanged(v: String) = _state.update { it.copy(ttsUrl = v) }
    fun onFfmpegPathChanged(v: String) = _state.update { it.copy(ffmpegPath = v) }
    fun onElevenLabsKeyChanged(v: String) = _state.update { it.copy(elevenLabsApiKey = v) }

    // دوال تغيير القيم الجديدة
    fun onLotsOfSoundsKeyChanged(v: String) = _state.update { it.copy(lotsofsoundsApiKey = v) }
    fun onOpenVfxKeyChanged(v: String) = _state.update { it.copy(openVfxApiKey = v) }
    fun onPixabayKeyChanged(v: String) = _state.update { it.copy(pixabayApiKey = v) }
    fun onPexelsKeyChanged(v: String) = _state.update { it.copy(pexelsApiKey = v) }
    fun onFreesoundKeyChanged(v: String) = _state.update { it.copy(freesoundApiKey = v) }

    fun onVoiceSamplePathChanged(path: String) {
        _state.update { it.copy(ttsVoiceSamplePath = path) }
    }

    fun onUseVoiceCloneChanged(use: Boolean) {
        _state.update { it.copy(ttsUseVoiceClone = use) }
    }

    fun onModelSelected(category: String, modelId: String) {
        _state.update { state ->
            val newSelected = state.selectedModels.toMutableMap().apply { put(category, modelId) }
            state.copy(selectedModels = newSelected)
        }
        viewModelScope.launch {
            settingsRepo.setString("selected_model_$category", modelId)
            if (category == "tts") {
                val newTtsModelId = modelId
                val samplePath = if (newTtsModelId.isNotBlank()) {
                    settingsRepo.getStringOnce("tts_voice_sample_${newTtsModelId}", "")
                } else ""
                val useClone = if (newTtsModelId.isNotBlank()) {
                    settingsRepo.getBoolOnce("tts_use_clone_${newTtsModelId}", false)
                } else false
                _state.update { it.copy(ttsVoiceSamplePath = samplePath, ttsUseVoiceClone = useClone) }
            }
        }
    }

    fun saveSettings() {
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, errorMessage = null, saveSuccessMessage = null) }
            try {
                val s = _state.value
                // مفاتيح API الحالية
                settingsRepo.setString("gemini_key", s.geminiApiKey)
                settingsRepo.setString("gemini_url", s.geminiUrl)
                settingsRepo.setString("hf_token", s.huggingFaceToken)
                settingsRepo.setString("tts_url", s.ttsUrl)
                settingsRepo.setString("ffmpeg_path", s.ffmpegPath)
                settingsRepo.setString("elevenlabs_key", s.elevenLabsApiKey)

                // مفاتيح API الجديدة
                settingsRepo.setString("lotsofsounds_api_key", s.lotsofsoundsApiKey)
                settingsRepo.setString("openvfx_api_key", s.openVfxApiKey)
                settingsRepo.setString("pixabay_api_key", s.pixabayApiKey)
                settingsRepo.setString("pexels_api_key", s.pexelsApiKey)
                settingsRepo.setString("freesound_api_key", s.freesoundApiKey)

                // حفظ النماذج المختارة
                s.selectedModels.forEach { (category, modelId) ->
                    settingsRepo.setString("selected_model_$category", modelId)
                }

                // حفظ إعدادات استنساخ الصوت
                val currentTtsModelId = s.selectedModels["tts"] ?: ""
                if (currentTtsModelId.isNotBlank()) {
                    settingsRepo.setString("tts_voice_sample_${currentTtsModelId}", s.ttsVoiceSamplePath)
                    settingsRepo.setBoolean("tts_use_clone_${currentTtsModelId}", s.ttsUseVoiceClone)
                }

                _state.update { it.copy(isSaving = false, saveSuccessMessage = "تم حفظ الإعدادات بنجاح 💾") }
            } catch (e: Exception) {
                _state.update { it.copy(isSaving = false, errorMessage = "فشل الحفظ: ${e.message}") }
            }
        }
    }

    fun refreshModelsInBackground() {
        viewModelScope.launch {
            _state.update { it.copy(isRefreshing = true, errorMessage = null, saveSuccessMessage = null) }
            val workRequest = OneTimeWorkRequestBuilder<BackgroundStyleRefresher>().build()
            WorkManager.getInstance(application).enqueue(workRequest)
            delay(3000)
            loadModelsByCategory()
            _state.update { it.copy(isRefreshing = false, saveSuccessMessage = "تم تحديث القوائم في الخلفية") }
        }
    }

    fun clearMessages() {
        _state.update { it.copy(saveSuccessMessage = null, errorMessage = null) }
    }
}