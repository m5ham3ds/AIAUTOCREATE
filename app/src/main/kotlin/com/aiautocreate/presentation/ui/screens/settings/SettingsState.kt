package com.aiautocreate.presentation.ui.screens.settings

/**
 * حالة شاشة الإعدادات.
 *
 * @property language اللغة المختارة حاليًا ("ar" أو "en").
 * @property themeMode وضع السمة ("dark", "light"). (اختيار system موجود في dataStore لكن الشاشة ثنائية التبديل).
 * @property dynamicColor هل الألوان الديناميكية مفعلة؟ (Android 12+).
 * @property isLoading جاري تحميل الإعدادات.
 * @property isSaving جاري حفظ التغييرات.
 * @property errorMessage رسالة خطأ اختيارية.
 */
data class SettingsState(
    val language: String = "ar",
    val themeMode: String = "dark",
    val dynamicColor: Boolean = true,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)