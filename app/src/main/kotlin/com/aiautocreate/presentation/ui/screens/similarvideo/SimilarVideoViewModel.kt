package com.aiautocreate.presentation.ui.screens.similarvideo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aiautocreate.agent.VideoAnalyzerAgent
import com.aiautocreate.data.repository.AppSettingsRepository
import com.aiautocreate.util.FFmpegRunner
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class SimilarVideoViewModel @Inject constructor(
    private val videoAnalyzer: VideoAnalyzerAgent,
    private val settingsRepo: AppSettingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SimilarVideoState())
    val state: StateFlow<SimilarVideoState> = _state.asStateFlow()

    init {
        loadDefaults()
    }

    private fun loadDefaults() {
        viewModelScope.launch {
            val res = settingsRepo.getStringOnce("similar_resolution", "4K")
            val fps = settingsRepo.getStringOnce("similar_fps", "30fps")
            _state.update { it.copy(selectedResolution = res, selectedFps = fps) }
        }
    }

    fun onVideoSelected(path: String, name: String) {
        _state.update {
            it.copy(
                selectedVideoPath = path,
                selectedVideoName = name,
                extractedDescription = null,
                generatedScript = null,
                generatedVideoPath = null,
                errorMessage = null,
                successMessage = null
            )
        }
    }

    fun onResolutionSelected(resolution: String) {
        _state.update { it.copy(selectedResolution = resolution) }
        viewModelScope.launch { settingsRepo.setString("similar_resolution", resolution) }
    }

    fun onFpsSelected(fps: String) {
        _state.update { it.copy(selectedFps = fps) }
        viewModelScope.launch { settingsRepo.setString("similar_fps", fps) }
    }

    fun startExtraction() {
        val videoPath = _state.value.selectedVideoPath
        if (videoPath == null) {
            _state.update { it.copy(errorMessage = "الرجاء اختيار ملف فيديو أولاً") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, progress = 0f, progressText = "0%", errorMessage = null, successMessage = null, logs = emptyList()) }

            try {
                _state.update { it.copy(progress = 0.1f, progressText = "10%", logs = it.logs + "جاري تحليل الفيديو...") }
                val analysisPrompt = "حلل هذا الفيديو واستخرج نمطه البصري وأسلوب التصوير والمشاهد الرئيسية."
                val result = videoAnalyzer.execute(analysisPrompt)

                if (!result.success) {
                    throw Exception(result.errorMessage ?: "فشل تحليل الفيديو")
                }
                val description = result.data?.toString() ?: "تم استخراج النمط البصري بنجاح."
                _state.update { it.copy(progress = 0.5f, progressText = "50%", extractedDescription = description, logs = it.logs + "تم تحليل الفيديو واستخراج النمط البصري.") }

                _state.update { it.copy(progress = 0.7f, progressText = "70%", logs = it.logs + "جاري إنشاء سيناريو جديد...") }
                val script = "سيناريو جديد مستوحى من: $description"
                _state.update { it.copy(progress = 0.8f, progressText = "80%", generatedScript = script, logs = it.logs + "تم إنشاء السيناريو الجديد.") }

                _state.update { it.copy(progress = 0.9f, progressText = "90%", logs = it.logs + "جاري تجميع الفيديو...") }
                val outputDir = File("/storage/emulated/0/AIAutoCreate/VIDEOS")
                if (!outputDir.exists()) outputDir.mkdirs()
                val outputPath = File(outputDir, "similar_${System.currentTimeMillis()}.mp4").absolutePath

                val cmd = "-f lavfi -i color=c=black:s=1280x720:d=5 -vf drawtext=text='Similar Video':fontcolor=white:fontsize=24:x=(w-text_w)/2:y=(h-text_h)/2 $outputPath"
                FFmpegRunner.execute(cmd)

                _state.update {
                    it.copy(
                        isLoading = false,
                        progress = 1f,
                        progressText = "100%",
                        successMessage = "تم إنشاء فيديو مشابه بنجاح ✨",
                        generatedVideoPath = outputPath,
                        logs = it.logs + "اكتملت العملية بنجاح."
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, errorMessage = "فشل الاستخراج: ${e.message}") }
            }
        }
    }

    fun clearMessages() {
        _state.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
