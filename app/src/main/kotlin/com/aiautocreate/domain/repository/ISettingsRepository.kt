package com.aiautocreate.domain.repository

import com.aiautocreate.domain.model.ApiKey
import com.aiautocreate.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow

interface ISettingsRepository {
    // التفضيلات العامة
    val userPreferences: Flow<UserPreferences>
    suspend fun setLanguage(language: String)
    suspend fun setThemeMode(themeMode: String)
    suspend fun setDynamicColor(enabled: Boolean)
    suspend fun updateAll(preferences: UserPreferences)

    // إدارة مفاتيح API
    suspend fun getApiKey(provider: String): ApiKey
    suspend fun saveApiKey(provider: String, key: String)
    suspend fun deleteApiKey(provider: String)
    suspend fun hasAnyApiKey(): Boolean
    fun observeHasApiKeys(): Flow<Boolean>

    // إعدادات البيومترية (اختياري)
    suspend fun isBiometricEnabled(): Boolean
    suspend fun setBiometricEnabled(enabled: Boolean)

    // سلة المحو
    suspend fun clearAllSecureData()
}