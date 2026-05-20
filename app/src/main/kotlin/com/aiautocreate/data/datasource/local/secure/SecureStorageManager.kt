package com.aiautocreate.data.datasource.local.secure

import androidx.security.crypto.EncryptedSharedPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SecureStorageManager(
    private val encryptedPrefs: EncryptedSharedPreferences
) {

    private object Keys {
        // المفاتيح الأساسية
        const val GEMINI_API_KEY = "gemini_api_key"
        const val HUGGINGFACE_API_KEY = "huggingface_api_key"
        
        // المفاتيح الجديدة للإضافات
        const val ELEVENLABS_API_KEY = "elevenlabs_api_key"
        const val LOTSOFSOUNDS_API_KEY = "lotsofsounds_api_key"
        const val OPENVFX_API_KEY = "openvfx_api_key"
        const val PIXABAY_API_KEY = "pixabay_api_key"
        const val PEXELS_API_KEY = "pexels_api_key"
        const val FREESOUND_API_KEY = "freesound_api_key"
        
        // رموز إضافية
        const val REFRESH_TOKEN = "refresh_token"
        const val ACCESS_TOKEN = "access_token"
        const val HAS_BIOMETRIC_ENABLED = "has_biometric_enabled"
    }

    // ========== Gemini ==========
    fun saveGeminiApiKey(apiKey: String) {
        encryptedPrefs.edit().putString(Keys.GEMINI_API_KEY, apiKey).apply()
    }
    fun getGeminiApiKey(): String? = encryptedPrefs.getString(Keys.GEMINI_API_KEY, null)

    // ========== HuggingFace ==========
    fun saveHuggingFaceApiKey(apiKey: String) {
        encryptedPrefs.edit().putString(Keys.HUGGINGFACE_API_KEY, apiKey).apply()
    }
    fun getHuggingFaceApiKey(): String? = encryptedPrefs.getString(Keys.HUGGINGFACE_API_KEY, null)

    // ========== ElevenLabs ==========
    fun saveElevenLabsApiKey(key: String) {
        encryptedPrefs.edit().putString(Keys.ELEVENLABS_API_KEY, key).apply()
    }
    fun getElevenLabsApiKey(): String? = encryptedPrefs.getString(Keys.ELEVENLABS_API_KEY, null)

    // ========== LotsOfSounds ==========
    fun saveLotsOfSoundsApiKey(key: String) {
        encryptedPrefs.edit().putString(Keys.LOTSOFSOUNDS_API_KEY, key).apply()
    }
    fun getLotsOfSoundsApiKey(): String? = encryptedPrefs.getString(Keys.LOTSOFSOUNDS_API_KEY, null)

    // ========== OpenVFX ==========
    fun saveOpenVfxApiKey(key: String) {
        encryptedPrefs.edit().putString(Keys.OPENVFX_API_KEY, key).apply()
    }
    fun getOpenVfxApiKey(): String? = encryptedPrefs.getString(Keys.OPENVFX_API_KEY, null)

    // ========== Pixabay ==========
    fun savePixabayApiKey(key: String) {
        encryptedPrefs.edit().putString(Keys.PIXABAY_API_KEY, key).apply()
    }
    fun getPixabayApiKey(): String? = encryptedPrefs.getString(Keys.PIXABAY_API_KEY, null)

    // ========== Pexels ==========
    fun savePexelsApiKey(key: String) {
        encryptedPrefs.edit().putString(Keys.PEXELS_API_KEY, key).apply()
    }
    fun getPexelsApiKey(): String? = encryptedPrefs.getString(Keys.PEXELS_API_KEY, null)

    // ========== Freesound ==========
    fun saveFreesoundApiKey(key: String) {
        encryptedPrefs.edit().putString(Keys.FREESOUND_API_KEY, key).apply()
    }
    fun getFreesoundApiKey(): String? = encryptedPrefs.getString(Keys.FREESOUND_API_KEY, null)

    // ========== إدارة الرموز (Tokens) ==========
    fun saveAccessToken(token: String) {
        encryptedPrefs.edit().putString(Keys.ACCESS_TOKEN, token).apply()
    }
    fun getAccessToken(): String? = encryptedPrefs.getString(Keys.ACCESS_TOKEN, null)

    fun saveRefreshToken(token: String) {
        encryptedPrefs.edit().putString(Keys.REFRESH_TOKEN, token).apply()
    }
    fun getRefreshToken(): String? = encryptedPrefs.getString(Keys.REFRESH_TOKEN, null)

    // ========== البيومترية ==========
    fun setBiometricEnabled(enabled: Boolean) {
        encryptedPrefs.edit().putBoolean(Keys.HAS_BIOMETRIC_ENABLED, enabled).apply()
    }
    fun isBiometricEnabled(): Boolean = encryptedPrefs.getBoolean(Keys.HAS_BIOMETRIC_ENABLED, false)

    // ========== دوال عامة ==========
    fun removeKey(key: String) {
        encryptedPrefs.edit().remove(key).apply()
    }

    fun clearAll() {
        encryptedPrefs.edit().clear().apply()
        refreshKeysState()
    }

    fun hasAnyApiKey(): Boolean {
        return !getGeminiApiKey().isNullOrBlank() ||
                !getHuggingFaceApiKey().isNullOrBlank() ||
                !getElevenLabsApiKey().isNullOrBlank() ||
                !getLotsOfSoundsApiKey().isNullOrBlank() ||
                !getOpenVfxApiKey().isNullOrBlank() ||
                !getPixabayApiKey().isNullOrBlank() ||
                !getPexelsApiKey().isNullOrBlank() ||
                !getFreesoundApiKey().isNullOrBlank()
    }

    // ========== حالة تفاعلية ==========
    private val _hasKeysState = MutableStateFlow(hasAnyApiKey())
    val hasKeysState: Flow<Boolean> = _hasKeysState.asStateFlow()

    private fun refreshKeysState() {
        _hasKeysState.value = hasAnyApiKey()
    }

    // دوال الحفظ المعدّلة لتنعش الحالة تلقائياً
    fun saveGeminiApiKeyAndRefresh(apiKey: String) {
        saveGeminiApiKey(apiKey)
        refreshKeysState()
    }
    fun saveHuggingFaceApiKeyAndRefresh(apiKey: String) {
        saveHuggingFaceApiKey(apiKey)
        refreshKeysState()
    }
    fun saveElevenLabsApiKeyAndRefresh(key: String) {
        saveElevenLabsApiKey(key)
        refreshKeysState()
    }
    fun saveLotsOfSoundsApiKeyAndRefresh(key: String) {
        saveLotsOfSoundsApiKey(key)
        refreshKeysState()
    }
    fun saveOpenVfxApiKeyAndRefresh(key: String) {
        saveOpenVfxApiKey(key)
        refreshKeysState()
    }
    fun savePixabayApiKeyAndRefresh(key: String) {
        savePixabayApiKey(key)
        refreshKeysState()
    }
    fun savePexelsApiKeyAndRefresh(key: String) {
        savePexelsApiKey(key)
        refreshKeysState()
    }
    fun saveFreesoundApiKeyAndRefresh(key: String) {
        saveFreesoundApiKey(key)
        refreshKeysState()
    }
}
