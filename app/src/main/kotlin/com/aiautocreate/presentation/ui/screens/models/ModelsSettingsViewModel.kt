package com.aiautocreate.presentation.ui.screens.models

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import androidx.lifecycle.Observer
import com.aiautocreate.agent.GeminiKeyManager
import com.aiautocreate.agent.HuggingFaceTokenManager
import com.aiautocreate.data.repository.AppSettingsRepository
import com.aiautocreate.domain.repository.IModelsRepository
import com.aiautocreate.domain.repository.ISettingsRepository
import com.aiautocreate.domain.usecase.model.RefreshModelsStylesUseCase
import com.aiautocreate.util.NetworkUtils
import com.aiautocreate.worker.BackgroundStyleRefresher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class ModelsSettingsViewModel @Inject constructor(
    private val appSettingsRepo: AppSettingsRepository,
    private val secureSettingsRepo: ISettingsRepository,
    private val modelsRepo: IModelsRepository,
    private val refreshStylesUseCase: RefreshModelsStylesUseCase,
    private val application: Application,
    private val geminiKeyManager: GeminiKeyManager,
    private val hfTokenManager: HuggingFaceTokenManager
) : ViewModel() {

    private val _state = MutableStateFlow(ModelsSettingsState())
    val state: StateFlow<ModelsSettingsState> = _state.asStateFlow()

    private val categories = listOf(
        "text" to "نموذج نصوص",
        "image" to "نموذج توليد الصور",
        "video" to "نموذج تحويل الصورة إلى فيديو",
        "tts" to "نموذج تحويل النص إلى صوت",
        "analysis" to "نموذج التحليل والمعالجة",
        "reviewer" to "نموذج مراجعة وتصحيح",
        "orchestrator" to "نموذج التنسيق العام",
        "music" to "نموذج توليد موسيقى",
        "transition" to "نموذج الانتقالات",
        "subtitle" to "نموذج الترجمة",
        "ffmpeg" to "نموذج أوامر FFmpeg"
    )

    init {
        loadSettings()
        loadModelsByCategory()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            val geminiKey = secureSettingsRepo.getGeminiKey() ?: ""
            val hfToken = secureSettingsRepo.getHuggingFaceToken() ?: ""
            val elevenKey = secureSettingsRepo.getElevenLabsKey() ?: ""
            val lotsofsoundsKey = secureSettingsRepo.getLotsOfSoundsKey() ?: ""
            val openVfxKey = secureSettingsRepo.getOpenVfxKey() ?: ""
            val pixabayKey = secureSettingsRepo.getPixabayKey() ?: ""
            val pexelsKey = secureSettingsRepo.getPexelsKey() ?: ""
            val freesoundKey = secureSettingsRepo.getFreesoundKey() ?: ""

            val geminiUrl = appSettingsRepo.getStringOnce("gemini_url", "")
            val ttsUrl = appSettingsRepo.getStringOnce("tts_url", "")
            val ffmpegPath = appSettingsRepo.getStringOnce("ffmpeg_path", "")
            val geminiKeysCsv = appSettingsRepo.getGeminiKeysCsv()
            val huggingFaceTokensCsv = appSettingsRepo.getHuggingFaceTokensCsv()
            val defaultAgentModelId = appSettingsRepo.getDefaultAgentModelId()
            val fallbackOrder = appSettingsRepo.getFallbackAgentModelsOrder()

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
                    geminiKeysCsv = geminiKeysCsv,
                    huggingFaceTokensCsv = huggingFaceTokensCsv,
                    defaultAgentModelId = defaultAgentModelId,
                    fallbackAgentModelsOrder = fallbackOrder,
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
                        val modelsForCategory = enabledModels.filter { model ->
                            val modelCategories = model.category.split(",").map { it.trim() }
                            modelCategories.contains(category)
                        }.map { ModelInfo(it.modelId, it.modelName) }
                        category to modelsForCategory
                    }
                    _state.update { it.copy(availableModelsByCategory = grouped) }
                }
        }
    }

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
    fun onGeminiKeysChanged(csv: String) = _state.update { it.copy(geminiKeysCsv = csv) }
    fun onHuggingFaceTokensChanged(csv: String) = _state.update { it.copy(huggingFaceTokensCsv = csv) }
    fun onDefaultAgentModelChanged(modelId: String) = _state.update { it.copy(defaultAgentModelId = modelId) }
    fun onFallbackAgentModelsOrderChanged(order: List<String>) = _state.update { it.copy(fallbackAgentModelsOrder = order) }

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
                secureSettingsRepo.saveGeminiKey(s.geminiApiKey)
                secureSettingsRepo.saveHuggingFaceToken(s.huggingFaceToken)
                secureSettingsRepo.saveElevenLabsKey(s.elevenLabsApiKey)
                secureSettingsRepo.saveLotsOfSoundsKey(s.lotsofsoundsApiKey)
                secureSettingsRepo.saveOpenVfxKey(s.openVfxApiKey)
                secureSettingsRepo.savePixabayKey(s.pixabayApiKey)
                secureSettingsRepo.savePexelsKey(s.pexelsApiKey)
                secureSettingsRepo.saveFreesoundKey(s.freesoundApiKey)

                appSettingsRepo.setString("gemini_url", s.geminiUrl)
                appSettingsRepo.setString("tts_url", s.ttsUrl)
                appSettingsRepo.setString("ffmpeg_path", s.ffmpegPath)
                appSettingsRepo.setGeminiKeysCsv(s.geminiKeysCsv)
                appSettingsRepo.setHuggingFaceTokensCsv(s.huggingFaceTokensCsv)
                appSettingsRepo.setDefaultAgentModelId(s.defaultAgentModelId)
                appSettingsRepo.setFallbackAgentModelsOrder(s.fallbackAgentModelsOrder)

                geminiKeyManager.refreshKeys()
                hfTokenManager.refreshTokens()

                s.selectedModels.forEach { (category, modelId) ->
                    appSettingsRepo.setString("selected_model_$category", modelId)
                }

                val currentTtsModelId = s.selectedModels["tts"] ?: ""
                if (currentTtsModelId.isNotBlank()) {
                    appSettingsRepo.setString("tts_voice_sample_${currentTtsModelId}", s.ttsVoiceSamplePath)
                    appSettingsRepo.setBoolean("tts_use_clone_${currentTtsModelId}", s.ttsUseVoiceClone)
                }

                _state.update { it.copy(isSaving = false, saveSuccessMessage = "✅ تم حفظ الإعدادات بنجاح") }
            } catch (e: Exception) {
                _state.update { it.copy(isSaving = false, errorMessage = "❌ فشل الحفظ: ${e.message}") }
            }
        }
    }

    /**
     * تحديث القوائم في الخلفية باستخدام WorkManager ومراقبة النتيجة عبر Flow.
     * يتم عرض عدد العناصر المحدثة عند الانتهاء.
     */
    fun refreshModelsInBackground() {
    viewModelScope.launch {
        if (!NetworkUtils.isOnline(application)) {
            _state.update { it.copy(errorMessage = "📡 لا يوجد اتصال بالإنترنت") }
            return@launch
        }

        _state.update {
            it.copy(
                isRefreshing = true,
                errorMessage = null,
                saveSuccessMessage = "🔄 جاري تحديث القوائم... قد يستغرق بضع ثوانٍ."
            )
        }

        try {
            val workRequest = BackgroundStyleRefresher.createWorkRequest()
            WorkManager.getInstance(application).enqueue(workRequest)

            var updatedCount = 0
            WorkManager.getInstance(application)
                .getWorkInfoByIdFlow(workRequest.id)
                .filter { it.state.isFinished }
                .firstOrNull()
                ?.let { workInfo ->
                    updatedCount = workInfo.outputData.getInt(BackgroundStyleRefresher.KEY_UPDATED_MODELS_COUNT, 0)
                }

            loadModelsByCategory()

            val message = if (updatedCount > 0) {
                "✅ تم تحديث $updatedCount عنصر بنجاح"
            } else {
                "⚠️ لم يتم تحديث أي عنصر. تأكد من اختيار نماذج في الإعدادات ومن وجود توكن HuggingFace صالح."
            }
            _state.update { it.copy(isRefreshing = false, saveSuccessMessage = message) }
        } catch (e: Exception) {
            _state.update {
                it.copy(
                    isRefreshing = false,
                    errorMessage = "❌ فشل التحديث: ${e.message}"
                )
            }
        }
    }
}
