package com.aiautocreate.data.repository

import com.aiautocreate.data.datasource.local.datastore.DataStoreManager
import com.aiautocreate.data.datasource.local.secure.SecureStorageManager
import com.aiautocreate.domain.model.ApiKey
import com.aiautocreate.domain.model.UserPreferences
import com.aiautocreate.domain.repository.ISettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    private val dataStoreManager: DataStoreManager,
    private val secureStorage: SecureStorageManager
) : ISettingsRepository {

    override val userPreferences: Flow<UserPreferences> = dataStoreManager.userPreferences

    override suspend fun setLanguage(language: String) = dataStoreManager.setLanguage(language)
    override suspend fun setThemeMode(themeMode: String) = dataStoreManager.setThemeMode(themeMode)
    override suspend fun setDynamicColor(enabled: Boolean) = dataStoreManager.setDynamicColor(enabled)
    override suspend fun updateAll(preferences: UserPreferences) = dataStoreManager.updateAll(preferences)

    override suspend fun getApiKey(provider: String): ApiKey {
        val key = when (provider.lowercase()) {
            "gemini" -> secureStorage.getGeminiApiKey()
            "huggingface" -> secureStorage.getHuggingFaceApiKey()
            else -> null
        }
        return ApiKey(provider = provider, keyValue = key, isStored = !key.isNullOrBlank())
    }

    override suspend fun saveApiKey(provider: String, key: String) {
        when (provider.lowercase()) {
            "gemini" -> secureStorage.saveGeminiApiKeyAndRefresh(key)
            "huggingface" -> secureStorage.saveHuggingFaceApiKeyAndRefresh(key)
        }
    }

    override suspend fun deleteApiKey(provider: String) {
        when (provider.lowercase()) {
            "gemini" -> secureStorage.removeKey("gemini_api_key")
            "huggingface" -> secureStorage.removeKey("huggingface_api_key")
        }
    }

    override suspend fun hasAnyApiKey(): Boolean = secureStorage.hasAnyApiKey()
    override fun observeHasApiKeys(): Flow<Boolean> = secureStorage.hasKeysState

    override suspend fun isBiometricEnabled(): Boolean = secureStorage.isBiometricEnabled()
    override suspend fun setBiometricEnabled(enabled: Boolean) = secureStorage.setBiometricEnabled(enabled)

    override suspend fun clearAllSecureData() = secureStorage.clearAll()
}