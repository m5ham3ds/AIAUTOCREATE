package com.aiautocreate.presentation.ui.screens.ffmpeg

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aiautocreate.agent.AgentInterventionHandler
import com.aiautocreate.agent.AgentOrchestrator
import com.aiautocreate.data.repository.AppSettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FfmpegViewModel @Inject constructor(
    private val settingsRepo: AppSettingsRepository,
    private val interventionHandler: AgentInterventionHandler,
    private val agentOrchestrator: AgentOrchestrator
) : ViewModel() {

    private val _state = MutableStateFlow(FfmpegState())
    val state: StateFlow<FfmpegState> = _state.asStateFlow()

    init {
        loadStylesAndDefaults()
    }

    private fun loadStylesAndDefaults() {
        viewModelScope.launch {
            val lastStyle = settingsRepo.getStringOnce("last_selected_style", "قصص وروايات")
            val montageStyles = settingsRepo.csvToList(settingsRepo.getStringOnce("montage_styles_csv"))

            val s = _state.value.copy(
                selectedMontageStyle = lastStyle,
                montageStyles = montageStyles.ifEmpty { listOf("قصص وروايات", "حماسي وجذاب", "احترافية وأنيق", "مخصص") }
            )
            _state.value = s
            loadProfile(lastStyle)
        }
    }

    private suspend fun loadProfile(style: String) {
        val prefix = getProfilePrefix(style)

        val minutes = settingsRepo.getStringOnce("${prefix}minutes", "01").toIntOrNull() ?: 1
        val seconds = settingsRepo.getStringOnce("${prefix}seconds", "30").toIntOrNull() ?: 30
        val aspect  = settingsRepo.getStringOnce("${prefix}aspect", "16:9")
        val quality = settingsRepo.getStringOnce("${prefix}quality", "1080p")
        val fps     = settingsRepo.getStringOnce("${prefix}fps", "30")

        val master   = settingsRepo.getBoolFlag("${prefix}master_on", false)
        val audio    = settingsRepo.getBoolFlag("${prefix}audio_on", false)
        val visual   = settingsRepo.getBoolFlag("${prefix}visual_on", false)
        val trans    = settingsRepo.getBoolFlag("${prefix}trans_on", false)
        val smart    = settingsRepo.getBoolFlag("${prefix}smart_on", false)
        val subs     = settingsRepo.getBoolFlag("${prefix}sub_on", false)
        val music    = settingsRepo.getBoolFlag("${prefix}music_on", false)
        val reviewer = settingsRepo.getBoolFlag("${prefix}reviewer_on", false)
        val orch     = settingsRepo.getBoolFlag("${prefix}orch_on", false)

        // قراءة العناصر الجديدة
        val externalVideo = settingsRepo.getBoolFlag("${prefix}external_video_on", false)
        val externalImage = settingsRepo.getBoolFlag("${prefix}external_image_on", false)

        val masterModel = settingsRepo.getStringOnce("${prefix}master_model", "")
        val audioFxModel = settingsRepo.getStringOnce("${prefix}audio_fx_model", "")
        val visualFxModel = settingsRepo.getStringOnce("${prefix}visual_fx_model", "")
        val transitionsModel = settingsRepo.getStringOnce("${prefix}transitions_model", "")
        val subtitlesModel = settingsRepo.getStringOnce("${prefix}subtitles_model", "")
        val musicModel = settingsRepo.getStringOnce("${prefix}music_model", "")
        val reviewerModel = settingsRepo.getStringOnce("${prefix}reviewer_model", "")
        val orchestratorModel = settingsRepo.getStringOnce("${prefix}orchestrator_model", "")

        _state.update {
            it.copy(
                selectedMontageStyle = style,
                videoMinutes = minutes,
                videoSeconds = seconds,
                selectedAspectRatio = aspect,
                selectedQuality = quality,
                selectedFps = fps,
                isMasterModelEnabled = master,
                isAudioFxEnabled = audio,
                isVisualFxEnabled = visual,
                isTransitionsEnabled = trans,
                isSmartCountEnabled = smart,
                isSubtitlesEnabled = subs,
                isMusicEnabled = music,
                isReviewerEnabled = reviewer,
                isMasterOrchestratorEnabled = orch,
                isExternalVideoEnabled = externalVideo,
                isExternalImageEnabled = externalImage,
                masterModelId = masterModel,
                audioFxModelId = audioFxModel,
                visualFxModelId = visualFxModel,
                transitionsModelId = transitionsModel,
                subtitlesModelId = subtitlesModel,
                musicModelId = musicModel,
                reviewerModelId = reviewerModel,
                orchestratorModelId = orchestratorModel,
                outputResolution = computeResolution(aspect, quality),
                outputEstimatedSize = estimateSize(quality, minutes, seconds)
            )
        }
    }

    // ========== أحداث الاختيار ==========
    fun onMontageStyleSelected(style: String) {
        viewModelScope.launch {
            settingsRepo.setString("last_selected_style", style)
            loadProfile(style)
        }
    }

    fun onMinutesChanged(minutes: Int) {
        _state.update { it.copy(videoMinutes = minutes.coerceIn(0, 10)) }
        updateOutputSize()
    }

    fun onSecondsChanged(seconds: Int) {
        _state.update { it.copy(videoSeconds = seconds.coerceIn(0, 59)) }
        updateOutputSize()
    }

    fun onQualitySelected(quality: String) {
        _state.update { it.copy(selectedQuality = quality) }
        updateOutputResolution()
    }

    fun onAspectRatioSelected(ratio: String) {
        _state.update { it.copy(selectedAspectRatio = ratio) }
        updateOutputResolution()
    }

    fun onFpsSelected(fps: String) {
        _state.update { it.copy(selectedFps = fps) }
    }

    // ========== Toggles ==========
    fun onMasterModelToggled(enabled: Boolean) {
        _state.update { it.copy(isMasterModelEnabled = enabled) }
        viewModelScope.launch {
            val style = _state.value.selectedMontageStyle
            if (enabled) {
                val modelId = settingsRepo.getStringOnce("selected_model_master", "")
                if (modelId.isNotBlank()) {
                    _state.update { it.copy(masterModelId = modelId) }
                    saveModelForTask("master", modelId, style)
                } else {
                    _state.update { it.copy(masterModelId = "") }
                }
            } else {
                _state.update { it.copy(masterModelId = "") }
                saveModelForTask("master", "", style)
            }
        }
    }

    fun onAudioFxToggled(enabled: Boolean) {
        _state.update { it.copy(isAudioFxEnabled = enabled) }
        viewModelScope.launch {
            val style = _state.value.selectedMontageStyle
            if (enabled) {
                val modelId = settingsRepo.getStringOnce("selected_model_audio_fx", "")
                if (modelId.isNotBlank()) _state.update { it.copy(audioFxModelId = modelId) }
                else _state.update { it.copy(audioFxModelId = "") }
            } else {
                _state.update { it.copy(audioFxModelId = "") }
                saveModelForTask("audio_fx", "", style)
            }
        }
    }

    fun onVisualFxToggled(enabled: Boolean) {
        _state.update { it.copy(isVisualFxEnabled = enabled) }
        viewModelScope.launch {
            val style = _state.value.selectedMontageStyle
            if (enabled) {
                val modelId = settingsRepo.getStringOnce("selected_model_visual_fx", "")
                if (modelId.isNotBlank()) _state.update { it.copy(visualFxModelId = modelId) }
                else _state.update { it.copy(visualFxModelId = "") }
            } else {
                _state.update { it.copy(visualFxModelId = "") }
                saveModelForTask("visual_fx", "", style)
            }
        }
    }

    fun onTransitionsToggled(enabled: Boolean) {
        _state.update { it.copy(isTransitionsEnabled = enabled) }
        viewModelScope.launch {
            val style = _state.value.selectedMontageStyle
            if (enabled) {
                val modelId = settingsRepo.getStringOnce("selected_model_transitions", "")
                if (modelId.isNotBlank()) _state.update { it.copy(transitionsModelId = modelId) }
                else _state.update { it.copy(transitionsModelId = "") }
            } else {
                _state.update { it.copy(transitionsModelId = "") }
                saveModelForTask("transitions", "", style)
            }
        }
    }

    fun onSubtitlesToggled(enabled: Boolean) {
        _state.update { it.copy(isSubtitlesEnabled = enabled) }
        viewModelScope.launch {
            val style = _state.value.selectedMontageStyle
            if (enabled) {
                val modelId = settingsRepo.getStringOnce("selected_model_subtitles", "")
                if (modelId.isNotBlank()) _state.update { it.copy(subtitlesModelId = modelId) }
                else _state.update { it.copy(subtitlesModelId = "") }
            } else {
                _state.update { it.copy(subtitlesModelId = "") }
                saveModelForTask("subtitles", "", style)
            }
        }
    }

    fun onMusicToggled(enabled: Boolean) {
        _state.update { it.copy(isMusicEnabled = enabled) }
        viewModelScope.launch {
            val style = _state.value.selectedMontageStyle
            if (enabled) {
                val modelId = settingsRepo.getStringOnce("selected_model_music", "")
                if (modelId.isNotBlank()) _state.update { it.copy(musicModelId = modelId) }
                else _state.update { it.copy(musicModelId = "") }
            } else {
                _state.update { it.copy(musicModelId = "") }
                saveModelForTask("music", "", style)
            }
        }
    }

    fun onReviewerToggled(enabled: Boolean) {
        _state.update { it.copy(isReviewerEnabled = enabled) }
        viewModelScope.launch {
            val style = _state.value.selectedMontageStyle
            if (enabled) {
                val modelId = settingsRepo.getStringOnce("selected_model_reviewer", "")
                if (modelId.isNotBlank()) _state.update { it.copy(reviewerModelId = modelId) }
                else _state.update { it.copy(reviewerModelId = "") }
            } else {
                _state.update { it.copy(reviewerModelId = "") }
                saveModelForTask("reviewer", "", style)
            }
        }
    }

    fun onMasterOrchToggled(enabled: Boolean) {
        _state.update { it.copy(isMasterOrchestratorEnabled = enabled) }
        viewModelScope.launch {
            val style = _state.value.selectedMontageStyle
            if (enabled) {
                val modelId = settingsRepo.getStringOnce("selected_model_orchestrator", "")
                if (modelId.isNotBlank()) _state.update { it.copy(orchestratorModelId = modelId) }
                else _state.update { it.copy(orchestratorModelId = "") }
            } else {
                _state.update { it.copy(orchestratorModelId = "") }
                saveModelForTask("orchestrator", "", style)
            }
        }
    }

    fun onSmartCountToggled(enabled: Boolean) = _state.update { it.copy(isSmartCountEnabled = enabled) }

    // ✅ دوال العناصر الجديدة
    fun onExternalVideoToggled(enabled: Boolean) {
        _state.update { it.copy(isExternalVideoEnabled = enabled) }
        viewModelScope.launch {
            val style = _state.value.selectedMontageStyle
            val prefix = getProfilePrefix(style)
            settingsRepo.setBooleanFlag(prefix + "external_video_on", enabled)
            // يمكن استدعاء الوكيل هنا لاقتراح فيديوهات خارجية إذا رغبت
        }
    }

    fun onExternalImageToggled(enabled: Boolean) {
        _state.update { it.copy(isExternalImageEnabled = enabled) }
        viewModelScope.launch {
            val style = _state.value.selectedMontageStyle
            val prefix = getProfilePrefix(style)
            settingsRepo.setBooleanFlag(prefix + "external_image_on", enabled)
            // يمكن استدعاء الوكيل لاقتراح صور خارجية
        }
    }

    private suspend fun saveModelForTask(task: String, modelId: String, style: String) {
        val prefix = getProfilePrefix(style)
        val key = when (task) {
            "master" -> "${prefix}master_model"
            "audio_fx" -> "${prefix}audio_fx_model"
            "visual_fx" -> "${prefix}visual_fx_model"
            "transitions" -> "${prefix}transitions_model"
            "subtitles" -> "${prefix}subtitles_model"
            "music" -> "${prefix}music_model"
            "reviewer" -> "${prefix}reviewer_model"
            "orchestrator" -> "${prefix}orchestrator_model"
            else -> return
        }
        settingsRepo.setString(key, modelId)
    }

    // ========== حفظ ==========
    fun saveSettings() {
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, errorMessage = null, saveSuccessMessage = null) }
            try {
                val s = _state.value
                val style = s.selectedMontageStyle
                val prefix = getProfilePrefix(style)

                settingsRepo.setString("last_selected_style", style)
                settingsRepo.setString("${prefix}minutes", s.videoMinutes.toString())
                settingsRepo.setString("${prefix}seconds", s.videoSeconds.toString())
                settingsRepo.setString("${prefix}aspect", s.selectedAspectRatio)
                settingsRepo.setString("${prefix}quality", s.selectedQuality)
                settingsRepo.setString("${prefix}fps", s.selectedFps)

                settingsRepo.setBooleanFlag("${prefix}master_on", s.isMasterModelEnabled)
                settingsRepo.setBooleanFlag("${prefix}audio_on", s.isAudioFxEnabled)
                settingsRepo.setBooleanFlag("${prefix}visual_on", s.isVisualFxEnabled)
                settingsRepo.setBooleanFlag("${prefix}trans_on", s.isTransitionsEnabled)
                settingsRepo.setBooleanFlag("${prefix}smart_on", s.isSmartCountEnabled)
                settingsRepo.setBooleanFlag("${prefix}sub_on", s.isSubtitlesEnabled)
                settingsRepo.setBooleanFlag("${prefix}music_on", s.isMusicEnabled)
                settingsRepo.setBooleanFlag("${prefix}reviewer_on", s.isReviewerEnabled)
                settingsRepo.setBooleanFlag("${prefix}orch_on", s.isMasterOrchestratorEnabled)

                // حفظ العناصر الجديدة
                settingsRepo.setBooleanFlag("${prefix}external_video_on", s.isExternalVideoEnabled)
                settingsRepo.setBooleanFlag("${prefix}external_image_on", s.isExternalImageEnabled)

                // حفظ النماذج
                settingsRepo.setString("${prefix}master_model", s.masterModelId)
                settingsRepo.setString("${prefix}audio_fx_model", s.audioFxModelId)
                settingsRepo.setString("${prefix}visual_fx_model", s.visualFxModelId)
                settingsRepo.setString("${prefix}transitions_model", s.transitionsModelId)
                settingsRepo.setString("${prefix}subtitles_model", s.subtitlesModelId)
                settingsRepo.setString("${prefix}music_model", s.musicModelId)
                settingsRepo.setString("${prefix}reviewer_model", s.reviewerModelId)
                settingsRepo.setString("${prefix}orchestrator_model", s.orchestratorModelId)

                _state.update { it.copy(isSaving = false, saveSuccessMessage = "تم حفظ إعدادات ${style} بنجاح") }
            } catch (e: Exception) {
                _state.update { it.copy(isSaving = false, errorMessage = "فشل الحفظ: ${e.message}") }
            }
        }
    }

    fun clearMessages() {
        _state.update { it.copy(saveSuccessMessage = null, errorMessage = null) }
    }

    // ========== دوال مساعدة ==========
    private fun getProfilePrefix(style: String) = "profile_${style}_"

    private suspend fun AppSettingsRepository.getBoolFlag(key: String, default: Boolean): Boolean {
        return try {
            getStringOnce(key, if (default) "true" else "false").toBoolean()
        } catch (_: Exception) { default }
    }

    private suspend fun AppSettingsRepository.setBooleanFlag(key: String, value: Boolean) {
        setString(key, if (value) "true" else "false")
    }

    private fun computeResolution(aspect: String, quality: String): String {
        val q = quality.lowercase().replace(" ", "")
        return when {
            aspect == "9:16" -> when {
                q.contains("4k") -> "2160×3840"
                q.contains("2k") -> "1440×2560"
                q.contains("720") -> "720×1280"
                else -> "1080×1920"
            }
            else -> when {
                q.contains("4k") -> "3840×2160"
                q.contains("2k") -> "2560×1440"
                q.contains("720") -> "1280×720"
                else -> "1920×1080"
            }
        }
    }

    private fun estimateSize(quality: String, minutes: Int, seconds: Int): String {
        val totalSeconds = (minutes * 60 + seconds).coerceAtLeast(1)
        val mbPerMinute = when {
            quality.contains("4k", ignoreCase = true) -> 400.0
            quality.contains("2k", ignoreCase = true) -> 200.0
            quality.contains("1080", ignoreCase = true) -> 100.0
            quality.contains("720", ignoreCase = true) -> 60.0
            else -> 30.0
        }
        val sizeMb = (mbPerMinute / 60) * totalSeconds
        return "~${sizeMb.toInt()} MB"
    }

    private fun updateOutputResolution() {
        val s = _state.value
        val res = computeResolution(s.selectedAspectRatio, s.selectedQuality)
        _state.update { it.copy(outputResolution = res) }
    }

    private fun updateOutputSize() {
        val s = _state.value
        val size = estimateSize(s.selectedQuality, s.videoMinutes, s.videoSeconds)
        _state.update { it.copy(outputEstimatedSize = size) }
    }
}