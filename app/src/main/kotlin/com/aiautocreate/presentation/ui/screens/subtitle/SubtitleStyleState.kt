package com.aiautocreate.presentation.ui.screens.subtitle

data class SubtitleStyleState(
    // خيارات الخط
    val selectedFont: String = "Cairo",
    val availableFonts: List<String> = listOf("Cairo", "Amiri", "Tajawal", "Rubik", "default", "serif", "sans-serif", "monospace"),
    val fontSize: Int = 32,
    val selectedWeight: String = "عريض",
    val availableWeights: List<String> = listOf("عادي", "متوسط", "عريض", "أسود"),

    // خيارات الألوان
    val textColorHex: String = "#FFFFFF",
    val backgroundColorHex: String = "#000000",  // ✅ لون الخلفية (أسود افتراضي)
    val backgroundColorOpacity: Int = 40,        // 0-100

    // الظل
    val selectedShadow: String = "قوي",
    val shadowOptions: List<String> = listOf("بدون", "خفيف", "قوي"),

    // الموضع
    val selectedAlignment: String = "center", // top, center, bottom

    // نص المعاينة
    val previewText: String = "نص تجريبي للترجمة",

    // حالة الحفظ
    val isSaving: Boolean = false,
    val saveSuccessMessage: String? = null,
    val errorMessage: String? = null
)