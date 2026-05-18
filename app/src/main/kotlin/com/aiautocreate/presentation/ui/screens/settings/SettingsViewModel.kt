package com.aiautocreate.presentation.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aiautocreate.data.datasource.local.datastore.DataStoreManager
import com.aiautocreate.di.Dispatcher
import com.aiautocreate.di.DispatcherType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel لشاشة الإعدادات.
 * يدير حالة اللغة والسمة والألوان الديناميكية، ويتيح تحديثها مباشرة في DataStore.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataStoreManager: DataStoreManager,
    @Dispatcher(DispatcherType.IO) private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState(isLoading = true))
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    init {
        loadCurrentSettings()
    }

    /**
     * يقرأ كل إعداد من DataStore. عند أي تغيير خارجي ينعكس فوراً.
     */
    private fun loadCurrentSettings() {
        viewModelScope.launch(ioDispatcher) {
            try {
                combine(
                    dataStoreManager.language,
                    dataStoreManager.themeMode,
                    dataStoreManager.dynamicColor
                ) { language, themeMode, dynamicColor ->
                    SettingsState(
                        language = language,
                        themeMode = themeMode,
                        dynamicColor = dynamicColor,
                        isLoading = false,
                        isSaving = false
                    )
                }.catch { e ->
                    emit(
                        SettingsState(
                            isLoading = false,
                            errorMessage = "فشل تحميل الإعدادات: ${e.message}"
                        )
                    )
                }.collect { newState ->
                    _state.value = newState
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(isLoading = false, errorMessage = "فشل تحميل الإعدادات: ${e.message}")
                }
            }
        }
    }

    // ---------- دوال التحديث ----------

    /**
     * تغيير اللغة (مثلاً "ar" أو "en"). يحفظ فوراً.
     */
    fun onLanguageChanged(newLanguage: String) {
        viewModelScope.launch(ioDispatcher) {
            try {
                _state.update { it.copy(isSaving = true, errorMessage = null) }
                dataStoreManager.setLanguage(newLanguage)
                // بعد نجاح الحفظ، سيتم تحديث الحالة تلقائياً عبر loadCurrentSettings → يتم ضبط isSaving = false هناك.
            } catch (e: Exception) {
                _state.update { it.copy(isSaving = false, errorMessage = "فشل تغيير اللغة") }
            }
        }
    }

    /**
     * مناسبة للـ Toggle في الشاشة (العربية ↔ English).
     */
    fun onLanguageToggled(useArabic: Boolean) {
        onLanguageChanged(if (useArabic) "ar" else "en")
    }

    /**
     * تغيير وضع السمة. في الشاشة نرسل "dark" أو "light" مباشرة (أو "system" إن أضفنا لاحقاً).
     */
    fun onThemeModeChanged(newThemeMode: String) {
        viewModelScope.launch(ioDispatcher) {
            try {
                _state.update { it.copy(isSaving = true, errorMessage = null) }
                dataStoreManager.setThemeMode(newThemeMode)
            } catch (e: Exception) {
                _state.update { it.copy(isSaving = false, errorMessage = "فشل تغيير السمة") }
            }
        }
    }

    /**
     * تفعيل/تعطيل الألوان الديناميكية.
     */
    fun onDynamicColorChanged(enabled: Boolean) {
        viewModelScope.launch(ioDispatcher) {
            try {
                _state.update { it.copy(isSaving = true, errorMessage = null) }
                dataStoreManager.setDynamicColor(enabled)
            } catch (e: Exception) {
                _state.update { it.copy(isSaving = false, errorMessage = "فشل تغيير إعداد الألوان") }
            }
        }
    }

    /**
     * مسح رسالة الخطأ المعروضة.
     */
    fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }
}