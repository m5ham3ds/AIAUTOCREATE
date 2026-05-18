package com.aiautocreate.domain.model

/**
 * نموذج تفضيلات المستخدم المُستخدم في طبقة Domain.
 */
data class UserPreferences(
    val language: String = "ar",       // "ar" أو "en"
    val themeMode: String = "system",  // "light", "dark", "system"
    val dynamicColor: Boolean = true
)