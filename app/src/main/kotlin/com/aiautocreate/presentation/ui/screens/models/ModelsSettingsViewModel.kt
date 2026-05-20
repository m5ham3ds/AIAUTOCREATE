package com.aiautocreate.presentation.ui.screens.models

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.aiautocreate.data.repository.AppSettingsRepository
import com.aiautocreate.domain.repository.IModelsRepository
import com.aiautocreate.domain.repository.ISettingsRepository
import com.aiautocreate.domain.usecase.model.RefreshModelsStylesUseCase
import com.aiautocreate.worker.BackgroundStyleRefresher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import javax.inject.Inject

@HiltViewModel
class ModelsSettingsViewModel @Inject constructor(
    private val appSettingsRepo: AppSettingsRepository,           // للإعدادات العامة (الروابط، المسارات، الاختيارات)
    private val secureSettingsRepo: ISettingsRepository,          // ✅ للمفاتيح الآمنة
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
            // قراءة المفاتيح من ISettingsRepository (مشفرة)
            val geminiKey = secureSettingsRepo.getGeminiKey() ?: ""
            val hfToken = secureSettingsRepo.getHuggingFaceToken() ?: ""
            val elevenKey = secureSettingsRepo.getElevenLabsKey() ?: ""
            val lotsofsoundsKey = secureSettingsRepo.getLotsOfSoundsKey() ?: ""
            val openVfxKey = secureSettingsRepo.getOpenVfxKey() ?: ""
            val pixabayKey = secureSettingsRepo.getPixabayKey() ?: ""
            val pexelsKey = secureSettingsRepo.getPexelsKey() ?: ""
            val freesoundKey = secureSettingsRepo.getFreesoundKey() ?: ""

            // قراءة الإعدادات العامة من AppSettingsRepository
            val geminiUrl = appSettingsRepo.getStringOnce("gemini_url", "")
            val ttsUrl = appSettingsRepo.getStringOnce("tts_url", "")
            val ffmpegPath = appSettingsRepo.getStringOnce("ffmpeg_path", "")

            // قراءة النماذج المختارة (هذه إعدادات عامة)
            val selected = categories.mapNotNull { (category, _) ->
                val modelId = appSettingsRepo.getStringOnce("selected_model_$category", "")
                if (modelId.isNotBlank()) category to modelId else null
            }.toMap()

            val currentTtsModelId = selected["tts"] ?: ""
            val ttsSamplePath = if (currentTtsModelId.isNotBlank()) {
                appSettingsRepo.getStringOnce("tts_voice_sample_${currentTtsModelId}", "")
            } else ""
            val ttsUseClone = if (currentTtsModelId.isNotBlank()) {
                appSettingsRepo.getBoolOnce("tts_use_clone_${currentTtsModelId}", false)
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

    // دوال تغيير القيم في الـ UI
    fun onGeminiKeyChanged(v: String) = _state.update { it.copy(geminiApiKey = v) }
    fun onGeminiUrlChanged(v: String) = _state.update { it.copy(geminiUrl = v) }
    fun onHuggingFaceTokenChanged(v: String) = _state.update { it.copy(huggingFaceToken = v) }
    fun onTtsUrlChanged(v: String) = _state.update { it.copy(ttsUrl = v) }
    fun onFfmpegPathChanged(v: String) = _state.update { it.copy(ffmpegPath = v) }
    fun onElevenLabsKeyChanged(v: String) = _state.update { it.copy(elevenLabsApiKey = v) }
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
            appSettingsRepo.setString("selected_model_$category", modelId)
            if (category == "tts") {
                val newTtsModelId = modelId
                val samplePath = if (newTtsModelId.isNotBlank()) {
                    appSettingsRepo.getStringOnce("tts_voice_sample_${newTtsModelId}", "")
                } else ""
                val useClone = if (newTtsModelId.isNotBlank()) {
                    appSettingsRepo.getBoolOnce("tts_use_clone_${newTtsModelId}", false)
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
                // حفظ المفاتيح في ISettingsRepository (مشفر)
                secureSettingsRepo.saveGeminiKey(s.geminiApiKey)
                secureSettingsRepo.saveHuggingFaceToken(s.huggingFaceToken)
                secureSettingsRepo.saveElevenLabsKey(s.elevenLabsApiKey)
                secureSettingsRepo.saveLotsOfSoundsKey(s.lotsofsoundsApiKey)
                secureSettingsRepo.saveOpenVfxKey(s.openVfxApiKey)
                secureSettingsRepo.savePixabayKey(s.pixabayApiKey)
                secureSettingsRepo.savePexelsKey(s.pexelsApiKey)
                secureSettingsRepo.saveFreesoundKey(s.freesoundApiKey)

                // حفظ الإعدادات العامة في AppSettingsRepository
                appSettingsRepo.setString("gemini_url", s.geminiUrl)
                appSettingsRepo.setString("tts_url", s.ttsUrl)
                appSettingsRepo.setString("ffmpeg_path", s.ffmpegPath)

                // حفظ النماذج المختارة
                s.selectedModels.forEach { (category, modelId) ->
                    appSettingsRepo.setString("selected_model_$category", modelId)
                }

                // حفظ إعدادات استنساخ الصوت
                val currentTtsModelId = s.selectedModels["tts"] ?: ""
                if (currentTtsModelId.isNotBlank()) {
                    appSettingsRepo.setString("tts_voice_sample_${currentTtsModelId}", s.ttsVoiceSamplePath)
                    appSettingsRepo.setBoolean("tts_use_clone_${currentTtsModelId}", s.ttsUseVoiceClone)
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
