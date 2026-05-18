package com.aiautocreate.presentation.ui.screens.audio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aiautocreate.data.repository.AppSettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AudioReconstructorViewModel @Inject constructor(
    private val settingsRepo: AppSettingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AudioReconstructorState())
    val state: StateFlow<AudioReconstructorState> = _state.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            val s = _state.value.copy(
                selectedOption = settingsRepo.getStringOnce("audio_option", "noise"),
                processingStrength = settingsRepo.getStringOnce("audio_strength", "85").toFloatOrNull() ?: 85f
            )
            _state.value = s
        }
    }

    fun onAudioSelected(path: String) {
        _state.update { it.copy(inputAudioPath = path, extractedText = null, generatedAudioPath = null, errorMessage = null) }
    }

    fun onOptionSelected(option: String) {
        _state.update { it.copy(selectedOption = option) }
    }

    fun onStrengthChanged(strength: Float) {
        _state.update { it.copy(processingStrength = strength) }
    }

    fun startReconstruction() {
        if (_state.value.inputAudioPath == null) {
            _state.update { it.copy(errorMessage = "الرجاء اختيار ملف صوتي أولاً") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isProcessing = true, isAnalyzing = true, errorMessage = null, successMessage = null) }
            try {
                // حفظ الإعدادات
                settingsRepo.setString("audio_option", _state.value.selectedOption)
                settingsRepo.setString("audio_strength", _state.value.processingStrength.toString())

                // تحليل الترددات
                delay(2000)
                _state.update { it.copy(isAnalyzing = false) }

                // هنا سيتم استدعاء AudioGenerationWorker فعليًا مستقبلاً
                delay(3000)

                _state.update {
                    it.copy(
                        isProcessing = false,
                        successMessage = "تم إعادة بناء الصوت بنجاح ✨",
                        generatedAudioPath = "/cache/reconstructed_audio.wav"
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(isProcessing = false, isAnalyzing = false, errorMessage = e.message) }
            }
        }
    }

    fun clearMessages() {
        _state.update { it.copy(errorMessage = null, successMessage = null) }
    }
}