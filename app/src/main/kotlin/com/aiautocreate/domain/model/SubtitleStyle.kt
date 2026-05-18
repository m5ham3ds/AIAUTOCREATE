package com.aiautocreate.domain.model

/**
 * تنسيق الترجمة المستخدم في توليد الفيديو.
 */
data class SubtitleStyle(
    val id: Long = 0,
    val name: String,
    val fontName: String = "default",
    val fontSize: Float = 16f,
    val textColorHex: String = "#FFFFFF",
    val backgroundColorHex: String = "#00000000",
    val position: String = "bottom",
    val isDefault: Boolean = false
)