package com.aiautocreate.presentation.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aiautocreate.AIAutoCreateApp
import com.aiautocreate.data.repository.AppSettingsRepository
import com.aiautocreate.domain.pipeline.PipelineConfig
import com.aiautocreate.domain.pipeline.PipelineEvent
import com.aiautocreate.domain.pipeline.PipelineOrchestrator
import com.aiautocreate.domain.repository.IModelsRepository
import com.aiautocreate.util.NetworkUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val settingsRepo: AppSettingsRepository,
    private val modelsRepo: IModelsRepository,
    private val orchestrator: PipelineOrchestrator
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    private var processingJob: Job? = null

    init {
        loadStylesAndSelections()
        checkConnection()
    }

    fun loadStylesAndSelections() {
        viewModelScope.launch {
            try {
                val imageStyles = getSupportedStylesForCategory("image")
                val videoStyles = getSupportedStylesForCategory("video")
                val ttsOptions = getSupportedStylesForCategory("tts")
                val montageStyles = getMontageStylesFromSettings()

                val selImage = settingsRepo.getStringOnce("sel_image_style", imageStyles.firstOrNull() ?: "واقعي")
                val selCover = settingsRepo.getStringOnce("sel_cover_style", "غلاف بسيط")
                val selVoice = settingsRepo.getStringOnce("sel_voice", ttsOptions.firstOrNull() ?: "صوت1")
                val selVideo = settingsRepo.getStringOnce("sel_video_style", videoStyles.firstOrNull() ?: "درامي")
                val selMontage = settingsRepo.getStringOnce("sel_montage_style", montageStyles.firstOrNull() ?: "قصص وروايات")

                _state.update {
                    it.copy(
                        imageStyles = imageStyles.ifEmpty { listOf("لا توجد أنماط") },
                        coverStyles = listOf("غلاف بسيط", "غلاف عصري", "غلاف سينمائي"),
                        videoStyles = videoStyles.ifEmpty { listOf("لا توجد أنماط") },
                        montageStyles = montageStyles.ifEmpty { listOf("قصص وروايات", "حماسي وجذاب", "احترافية وأنيق", "مخصص") },
                        voiceOptions = ttsOptions.ifEmpty { listOf("صوت1", "صوت2", "استنساخ العينة") },
                        selectedImageStyle = selImage,
                        selectedCoverStyle = selCover,
                        selectedVoice = selVoice,
                        selectedVideoStyle = selVideo,
                        selectedMontageStyle = selMontage,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, errorMessage = "فشل تحميل الإعدادات: ${e.message}") }
            }
        }
    }

    private suspend fun getSupportedStylesForCategory(category: String): List<String> {
        val selectedModelId = settingsRepo.getSelectedModelForCategory(category)
        if (selectedModelId.isBlank()) return emptyList()
        val allModels = modelsRepo.getAllModelConfigs().first()
        val model = allModels.find { it.modelId == selectedModelId && it.isEnabled }
        return model?.supportedStyles ?: emptyList()
    }

    private suspend fun getMontageStylesFromSettings(): List<String> {
        val csv = settingsRepo.getStringOnce("montage_styles_csv", "قصص وروايات,حماسي وجذاب,احترافية وأنيق,مخصص")
        return settingsRepo.csvToList(csv)
    }

    private fun checkConnection() {
        viewModelScope.launch {
            val connected = NetworkUtils.isOnline(AIAutoCreateApp.instance)
            _state.update {
                it.copy(
                    isConnected = connected,
                    connectionStatus = if (connected) "متصل بالخادم الذكي" else "غير متصل بالإنترنت"
                )
            }
        }
    }

    private suspend fun getMontageProfile(style: String): MontageProfile {
        val prefix = "profile_${style}_"
        return MontageProfile(
            minutes = settingsRepo.getStringOnce("${prefix}minutes", "01").toIntOrNull() ?: 1,
            seconds = settingsRepo.getStringOnce("${prefix}seconds", "30").toIntOrNull() ?: 30,
            aspect = settingsRepo.getStringOnce("${prefix}aspect", "16:9"),
            quality = settingsRepo.getStringOnce("${prefix}quality", "1080p"),
            fps = settingsRepo.getStringOnce("${prefix}fps", "30"),
            masterModelEnabled = settingsRepo.getBoolOnce("${prefix}master_on", false),
            audioFxEnabled = settingsRepo.getBoolOnce("${prefix}audio_on", false),
            visualFxEnabled = settingsRepo.getBoolOnce("${prefix}visual_on", false),
            transitionsEnabled = settingsRepo.getBoolOnce("${prefix}trans_on", false),
            smartCountEnabled = settingsRepo.getBoolOnce("${prefix}smart_on", false),
            subtitlesEnabled = settingsRepo.getBoolOnce("${prefix}sub_on", false),
            musicEnabled = settingsRepo.getBoolOnce("${prefix}music_on", false),
            reviewerEnabled = settingsRepo.getBoolOnce("${prefix}reviewer_on", false),
            orchestratorEnabled = settingsRepo.getBoolOnce("${prefix}orch_on", false)
        )
    }

    private suspend fun AppSettingsRepository.getBoolOnce(key: String, default: Boolean): Boolean {
        return try {
            getStringOnce(key, if (default) "true" else "false").toBoolean()
        } catch (_: Exception) { default }
    }

    fun onPromptChanged(text: String) {
        _state.update { it.copy(promptText = text, errorMessage = null) }
    }

    fun onImageStyleSelected(style: String) {
        _state.update { it.copy(selectedImageStyle = style) }
        viewModelScope.launch { settingsRepo.setString("sel_image_style", style) }
    }

    fun onCoverStyleSelected(style: String) {
        _state.update { it.copy(selectedCoverStyle = style) }
        viewModelScope.launch { settingsRepo.setString("sel_cover_style", style) }
    }

    fun onVoiceSelected(voice: String) {
        _state.update { it.copy(selectedVoice = voice) }
        viewModelScope.launch { settingsRepo.setString("sel_voice", voice) }
    }

    fun onVideoStyleSelected(style: String) {
        _state.update { it.copy(selectedVideoStyle = style) }
        viewModelScope.launch { settingsRepo.setString("sel_video_style", style) }
    }

    fun onMontageStyleSelected(style: String) {
        _state.update { it.copy(selectedMontageStyle = style) }
        viewModelScope.launch { settingsRepo.setString("sel_montage_style", style) }
    }

    fun showCancelDialog() {
        _state.update { it.copy(showCancelDialog = true) }
    }

    fun hideCancelDialog() {
        _state.update { it.copy(showCancelDialog = false) }
    }

    fun cancelProcessing() {
        processingJob?.cancel()
        processingJob = null
        _state.update {
            it.copy(
                isProcessing = false,
                progress = 0f,
                progressText = "0%",
                logs = emptyList(),
                outputVideoPath = null,
                errorMessage = "تم إلغاء العملية بواسطة المستخدم",
                showCancelDialog = false
            )
        }
    }

    fun startProcessing() {
        val s = _state.value
        if (s.promptText.isBlank()) {
            _state.update { it.copy(errorMessage = "⚠️ الرجاء إدخال نص الفكرة") }
            return
        }
        if (s.isProcessing) return

        if (!NetworkUtils.isOnline(AIAutoCreateApp.instance)) {
            _state.update {
                it.copy(
                    errorMessage = "📡 لا يوجد اتصال بالإنترنت. يرجى التحقق من اتصالك وإعادة المحاولة.",
                    isProcessing = false
                )
            }
            return
        }

        processingJob?.cancel()

        processingJob = viewModelScope.launch {
            _state.update {
                it.copy(
                    isProcessing = true,
                    progress = 0f,
                    progressText = "0%",
                    logs = emptyList(),
                    outputVideoPath = null,
                    errorMessage = null,
                    showCancelDialog = false
                )
            }

            try {
                val profile = getMontageProfile(s.selectedMontageStyle)

                val sdModel = settingsRepo.getSelectedModelForCategory("image")
                val img2VidModel = settingsRepo.getSelectedModelForCategory("video")
                val ttsModel = settingsRepo.getSelectedModelForCategory("tts")

                val masterModelId = settingsRepo.getSelectedModelForCategory("master")
                val audioFxModelId = settingsRepo.getSelectedModelForCategory("audio_fx")
                val visualFxModelId = settingsRepo.getSelectedModelForCategory("visual_fx")
                val transitionsModelId = settingsRepo.getSelectedModelForCategory("transitions")
                val subtitlesModelId = settingsRepo.getSelectedModelForCategory("subtitles")
                val musicModelId = settingsRepo.getSelectedModelForCategory("music")
                val reviewerModelId = settingsRepo.getSelectedModelForCategory("reviewer")
                val orchestratorModelId = settingsRepo.getSelectedModelForCategory("orchestrator")

                val config = PipelineConfig(
                    prompt = s.promptText,
                    imageStyle = s.selectedImageStyle,
                    coverStyle = s.selectedCoverStyle,
                    voiceChoice = s.selectedVoice,
                    videoStyle = s.selectedVideoStyle,
                    montageStyle = s.selectedMontageStyle,
                    minutes = profile.minutes.toString(),
                    seconds = profile.seconds.toString(),
                    aspect = profile.aspect,
                    quality = profile.quality,
                    sdModel = sdModel,
                    img2VidModel = img2VidModel,
                    ttsModel = ttsModel,
                    selectedFps = profile.fps,
                    masterModelId = masterModelId,
                    audioFxModelId = audioFxModelId,
                    visualFxModelId = visualFxModelId,
                    transitionsModelId = transitionsModelId,
                    subtitlesModelId = subtitlesModelId,
                    musicModelId = musicModelId,
                    reviewerModelId = reviewerModelId,
                    orchestratorModelId = orchestratorModelId
                )

                orchestrator.events.collect { event ->
                    if (!currentCoroutineContext().isActive) return@collect
                    when (event) {
                        is PipelineEvent.Progress -> _state.update {
                            it.copy(progress = event.percent / 100f, progressText = "${event.percent}%")
                        }
                        is PipelineEvent.Log -> _state.update {
                            it.copy(logs = it.logs + event.message)
                        }
                        is PipelineEvent.Error -> _state.update {
                            it.copy(errorMessage = event.message, isProcessing = false)
                        }
                        is PipelineEvent.FinalResult -> _state.update {
                            it.copy(outputVideoPath = event.outputFile, isProcessing = false, progress = 1f, progressText = "100%")
                        }
                    }
                }

                orchestrator.execute(config)
            } catch (e: Exception) {
                if (currentCoroutineContext().isActive) {
                    _state.update { it.copy(isProcessing = false, errorMessage = e.message) }
                }
            } finally {
                processingJob = null
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }
}

data class MontageProfile(
    val minutes: Int,
    val seconds: Int,
    val aspect: String,
    val quality: String,
    val fps: String,
    val masterModelEnabled: Boolean,
    val audioFxEnabled: Boolean,
    val visualFxEnabled: Boolean,
    val transitionsEnabled: Boolean,
    val smartCountEnabled: Boolean,
    val subtitlesEnabled: Boolean,
    val musicEnabled: Boolean,
    val reviewerEnabled: Boolean,
    val orchestratorEnabled: Boolean
)
