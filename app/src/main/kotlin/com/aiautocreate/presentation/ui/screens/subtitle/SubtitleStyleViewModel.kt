package com.aiautocreate.presentation.ui.screens.subtitle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aiautocreate.data.repository.AppSettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SubtitleStyleViewModel @Inject constructor(
    private val settingsRepo: AppSettingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SubtitleStyleState())
    val state: StateFlow<SubtitleStyleState> = _state.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            val s = _state.value.copy(
                selectedFont = settingsRepo.getStringOnce("sub_font", "Cairo"),
                fontSize = settingsRepo.getStringOnce("sub_font_size", "32").toIntOrNull() ?: 32,
                selectedWeight = settingsRepo.getStringOnce("sub_weight", "عريض"),
                textColorHex = settingsRepo.getStringOnce("sub_text_color", "#FFFFFF"),
                backgroundColorHex = settingsRepo.getStringOnce("sub_bg_color", "#000000"),
                backgroundColorOpacity = settingsRepo.getStringOnce("sub_bg_opacity", "40").toIntOrNull() ?: 40,
                selectedShadow = settingsRepo.getStringOnce("sub_shadow", "قوي"),
                selectedAlignment = settingsRepo.getStringOnce("sub_alignment", "center"),
                previewText = settingsRepo.getStringOnce("sub_preview_text", "نص تجريبي للترجمة")
            )
            _state.value = s
        }
    }

    fun onFontSelected(font: String) = _state.update { it.copy(selectedFont = font) }
    fun onWeightSelected(weight: String) = _state.update { it.copy(selectedWeight = weight) }
    fun increaseFontSize() = _state.update { it.copy(fontSize = (it.fontSize + 1).coerceAtMost(80)) }
    fun decreaseFontSize() = _state.update { it.copy(fontSize = (it.fontSize - 1).coerceAtLeast(10)) }
    
    fun onTextColorChanged(hex: String) = _state.update { it.copy(textColorHex = hex) }
    fun onBackgroundColorChanged(hex: String) = _state.update { it.copy(backgroundColorHex = hex) }
    fun onBackgroundOpacityChanged(opacity: Int) = _state.update { it.copy(backgroundColorOpacity = opacity.coerceIn(0, 100)) }
    
    fun onShadowSelected(shadow: String) = _state.update { it.copy(selectedShadow = shadow) }
    fun onAlignmentSelected(alignment: String) = _state.update { it.copy(selectedAlignment = alignment) }
    fun onPreviewTextChanged(text: String) = _state.update { it.copy(previewText = text) }

    fun saveSettings() {
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, errorMessage = null, saveSuccessMessage = null) }
            try {
                val s = _state.value
                settingsRepo.setString("sub_font", s.selectedFont)
                settingsRepo.setString("sub_font_size", s.fontSize.toString())
                settingsRepo.setString("sub_weight", s.selectedWeight)
                settingsRepo.setString("sub_text_color", s.textColorHex)
                settingsRepo.setString("sub_bg_color", s.backgroundColorHex)
                settingsRepo.setString("sub_bg_opacity", s.backgroundColorOpacity.toString())
                settingsRepo.setString("sub_shadow", s.selectedShadow)
                settingsRepo.setString("sub_alignment", s.selectedAlignment)
                settingsRepo.setString("sub_preview_text", s.previewText)

                _state.update { it.copy(isSaving = false, saveSuccessMessage = "تم حفظ إعدادات الترجمة بنجاح") }
            } catch (e: Exception) {
                _state.update { it.copy(isSaving = false, errorMessage = "فشل الحفظ: ${e.message}") }
            }
        }
    }

    fun clearMessages() {
        _state.update { it.copy(saveSuccessMessage = null, errorMessage = null) }
    }
}