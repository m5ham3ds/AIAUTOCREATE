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

    // ========== التفضيلات العامة ==========
    override val userPreferences: Flow<UserPreferences> = dataStoreManager.userPreferences
    override suspend fun setLanguage(language: String) = dataStoreManager.setLanguage(language)
    override suspend fun setThemeMode(themeMode: String) = dataStoreManager.setThemeMode(themeMode)
    override suspend fun setDynamicColor(enabled: Boolean) = dataStoreManager.setDynamicColor(enabled)
    override suspend fun updateAll(preferences: UserPreferences) = dataStoreManager.updateAll(preferences)

    // ========== إدارة مفاتيح API (عام) ==========
    override suspend fun getApiKey(provider: String): ApiKey {
        val key = when (provider.lowercase()) {
            "gemini" -> secureStorage.getGeminiApiKey()
            "huggingface" -> secureStorage.getHuggingFaceApiKey()
            "elevenlabs" -> secureStorage.getElevenLabsApiKey()
            "lotsofsounds" -> secureStorage.getLotsOfSoundsApiKey()
            "openvfx" -> secureStorage.getOpenVfxApiKey()
            "pixabay" -> secureStorage.getPixabayApiKey()
            "pexels" -> secureStorage.getPexelsApiKey()
            "freesound" -> secureStorage.getFreesoundApiKey()
            else -> null
        }
        return ApiKey(provider = provider, keyValue = key, isStored = !key.isNullOrBlank())
    }

    override suspend fun saveApiKey(provider: String, key: String) {
        when (provider.lowercase()) {
            "gemini" -> secureStorage.saveGeminiApiKeyAndRefresh(key)
            "huggingface" -> secureStorage.saveHuggingFaceApiKeyAndRefresh(key)
            "elevenlabs" -> secureStorage.saveElevenLabsApiKeyAndRefresh(key)
            "lotsofsounds" -> secureStorage.saveLotsOfSoundsApiKeyAndRefresh(key)
            "openvfx" -> secureStorage.saveOpenVfxApiKeyAndRefresh(key)
            "pixabay" -> secureStorage.savePixabayApiKeyAndRefresh(key)
            "pexels" -> secureStorage.savePexelsApiKeyAndRefresh(key)
            "freesound" -> secureStorage.saveFreesoundApiKeyAndRefresh(key)
        }
    }

    override suspend fun deleteApiKey(provider: String) {
        val key = when (provider.lowercase()) {
            "gemini" -> "gemini_api_key"
            "huggingface" -> "huggingface_api_key"
            "elevenlabs" -> "elevenlabs_api_key"
            "lotsofsounds" -> "lotsofsounds_api_key"
            "openvfx" -> "openvfx_api_key"
            "pixabay" -> "pixabay_api_key"
            "pexels" -> "pexels_api_key"
            "freesound" -> "freesound_api_key"
            else -> return
        }
        secureStorage.removeKey(key)
    }

    override suspend fun hasAnyApiKey(): Boolean = secureStorage.hasAnyApiKey()
    override fun observeHasApiKeys(): Flow<Boolean> = secureStorage.hasKeysState

    // ========== دوال محددة لكل مفتاح (للتسهيل) ==========
    override suspend fun getGeminiKey(): String? = secureStorage.getGeminiApiKey()
    override suspend fun saveGeminiKey(key: String) = secureStorage.saveGeminiApiKeyAndRefresh(key)

    override suspend fun getHuggingFaceToken(): String? = secureStorage.getHuggingFaceApiKey()
    override suspend fun saveHuggingFaceToken(token: String) = secureStorage.saveHuggingFaceApiKeyAndRefresh(token)

    override suspend fun getElevenLabsKey(): String? = secureStorage.getElevenLabsApiKey()
    override suspend fun saveElevenLabsKey(key: String) = secureStorage.saveElevenLabsApiKeyAndRefresh(key)

    override suspend fun getLotsOfSoundsKey(): String? = secureStorage.getLotsOfSoundsApiKey()
    override suspend fun saveLotsOfSoundsKey(key: String) = secureStorage.saveLotsOfSoundsApiKeyAndRefresh(key)

    override suspend fun getOpenVfxKey(): String? = secureStorage.getOpenVfxApiKey()
    override suspend fun saveOpenVfxKey(key: String) = secureStorage.saveOpenVfxApiKeyAndRefresh(key)

    override suspend fun getPixabayKey(): String? = secureStorage.getPixabayApiKey()
    override suspend fun savePixabayKey(key: String) = secureStorage.savePixabayApiKeyAndRefresh(key)

    override suspend fun getPexelsKey(): String? = secureStorage.getPexelsApiKey()
    override suspend fun savePexelsKey(key: String) = secureStorage.savePexelsApiKeyAndRefresh(key)

    override suspend fun getFreesoundKey(): String? = secureStorage.getFreesoundApiKey()
    override suspend fun saveFreesoundKey(key: String) = secureStorage.saveFreesoundApiKeyAndRefresh(key)

    // ========== البيومترية ==========
    override suspend fun isBiometricEnabled(): Boolean = secureStorage.isBiometricEnabled()
    override suspend fun setBiometricEnabled(enabled: Boolean) = secureStorage.setBiometricEnabled(enabled)

    override suspend fun clearAllSecureData() = secureStorage.clearAll()
}
