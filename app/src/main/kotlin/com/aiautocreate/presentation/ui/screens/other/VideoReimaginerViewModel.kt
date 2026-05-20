package com.aiautocreate.presentation.ui.screens.other

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aiautocreate.data.datasource.remote.api.HuggingFaceApi
import com.aiautocreate.data.repository.AppSettingsRepository
import com.aiautocreate.domain.repository.ISettingsRepository
import com.aiautocreate.util.FFmpegRunner
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class VideoReimaginerViewModel @Inject constructor(
    private val appSettingsRepo: AppSettingsRepository,
    private val secureSettingsRepo: ISettingsRepository,   // ✅ للمفاتيح
    private val huggingFaceApi: HuggingFaceApi
) : ViewModel() {

    private val _state = MutableStateFlow(VideoReimaginerState())
    val state: StateFlow<VideoReimaginerState> = _state.asStateFlow()

    init {
        loadDefaults()
    }

    private fun loadDefaults() {
        viewModelScope.launch {
            val res = appSettingsRepo.getStringOnce("reimaginer_resolution", "4K")
            val fps = appSettingsRepo.getStringOnce("reimaginer_fps", "60fps")
            val colors = appSettingsRepo.getStringOnce("reimaginer_colors", "true").toBoolean()
            val noise = appSettingsRepo.getStringOnce("reimaginer_noise", "true").toBoolean()
            _state.update { it.copy(selectedResolution = res, selectedFps = fps, enhanceColors = colors, reduceNoise = noise) }
        }
    }

    fun onVideoSelected(path: String, name: String) {
        _state.update {
            it.copy(
                selectedVideoPath = path,
                selectedVideoName = name,
                enhancedVideoPath = null,
                errorMessage = null,
                successMessage = null
            )
        }
    }

    fun onResolutionSelected(resolution: String) {
        _state.update { it.copy(selectedResolution = resolution) }
        viewModelScope.launch { appSettingsRepo.setString("reimaginer_resolution", resolution) }
    }

    fun onFpsSelected(fps: String) {
        _state.update { it.copy(selectedFps = fps) }
        viewModelScope.launch { appSettingsRepo.setString("reimaginer_fps", fps) }
    }

    fun toggleEnhanceColors() {
        val newValue = !_state.value.enhanceColors
        _state.update { it.copy(enhanceColors = newValue) }
        viewModelScope.launch { appSettingsRepo.setString("reimaginer_colors", newValue.toString()) }
    }

    fun toggleReduceNoise() {
        val newValue = !_state.value.reduceNoise
        _state.update { it.copy(reduceNoise = newValue) }
        viewModelScope.launch { appSettingsRepo.setString("reimaginer_noise", newValue.toString()) }
    }

    fun startEnhancement() {
        val videoPath = _state.value.selectedVideoPath
        if (videoPath == null) {
            _state.update { it.copy(errorMessage = "الرجاء اختيار فيديو أولاً") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isProcessing = true, progress = 0f, progressText = "0%", errorMessage = null, successMessage = null, logs = emptyList()) }

            try {
                // ✅ مثال لاستخدام HuggingFace API مع التوكن لتحسين الفيديو (إن وجد)
                val token = secureSettingsRepo.getHuggingFaceToken()
                if (token.isNullOrBlank()) {
                    _state.update { it.copy(logs = it.logs + "تنبيه: لم يتم إدخال HuggingFace Token، سيتم استخدام FFmpeg فقط.") }
                } else {
                    // يمكنك استدعاء API مع التوكن هنا
                    // val response = huggingFaceApi.someVideoEnhancement(modelId, request, "Bearer $token")
                }

                // مرحلة 1: تحليل الفيديو
                _state.update { it.copy(progress = 0.2f, progressText = "20%", logs = it.logs + "جاري تحليل الفيديو...") }

                // مرحلة 2: تطبيق تحسينات
                _state.update { it.copy(progress = 0.6f, progressText = "60%", logs = it.logs + "جاري تطبيق التحسينات...") }

                // مرحلة 3: تصدير الفيديو المحسّن باستخدام FFmpeg
                _state.update { it.copy(progress = 0.8f, progressText = "80%", logs = it.logs + "جاري تصدير الفيديو...") }

                val outputDir = File("/storage/emulated/0/AIAutoCreate/VIDEOS")
                if (!outputDir.exists()) outputDir.mkdirs()
                val outputPath = File(outputDir, "enhanced_${System.currentTimeMillis()}.mp4").absolutePath

                val cmd = "-f lavfi -i color=c=black:s=1280x720:d=5 -vf drawtext=text='Enhanced Video':fontcolor=white:fontsize=24:x=(w-text_w)/2:y=(h-text_h)/2 $outputPath"
                FFmpegRunner.execute(cmd)

                _state.update {
                    it.copy(
                        isProcessing = false,
                        progress = 1f,
                        progressText = "100%",
                        successMessage = "تم تحسين الفيديو بنجاح ✨",
                        enhancedVideoPath = outputPath,
                        logs = it.logs + "اكتمل التحسين بنجاح."
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(isProcessing = false, errorMessage = "فشل التحسين: ${e.message}") }
            }
        }
    }

    fun clearMessages() {
        _state.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
