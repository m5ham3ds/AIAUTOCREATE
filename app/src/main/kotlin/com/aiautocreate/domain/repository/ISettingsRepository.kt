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

    // ========== إدارة مفاتيح API (عام) ==========
    suspend fun getApiKey(provider: String): ApiKey
    suspend fun saveApiKey(provider: String, key: String)
    suspend fun deleteApiKey(provider: String)
    suspend fun hasAnyApiKey(): Boolean
    fun observeHasApiKeys(): Flow<Boolean>

    // ========== مفاتيح محددة لكل خدمة (لتسهيل الاستخدام) ==========
    suspend fun getGeminiKey(): String?
    suspend fun saveGeminiKey(key: String)
    suspend fun getHuggingFaceToken(): String?
    suspend fun saveHuggingFaceToken(token: String)
    suspend fun getElevenLabsKey(): String?
    suspend fun saveElevenLabsKey(key: String)
    suspend fun getLotsOfSoundsKey(): String?
    suspend fun saveLotsOfSoundsKey(key: String)
    suspend fun getOpenVfxKey(): String?
    suspend fun saveOpenVfxKey(key: String)
    suspend fun getPixabayKey(): String?
    suspend fun savePixabayKey(key: String)
    suspend fun getPexelsKey(): String?
    suspend fun savePexelsKey(key: String)
    suspend fun getFreesoundKey(): String?
    suspend fun saveFreesoundKey(key: String)

    // ========== البيومترية ==========
    suspend fun isBiometricEnabled(): Boolean
    suspend fun setBiometricEnabled(enabled: Boolean)

    // مسح جميع البيانات الحساسة
    suspend fun clearAllSecureData()
}
