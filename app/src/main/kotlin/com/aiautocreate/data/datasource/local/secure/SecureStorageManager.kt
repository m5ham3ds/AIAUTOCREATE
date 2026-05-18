package com.aiautocreate.data.datasource.local.secure

import androidx.security.crypto.EncryptedSharedPreferences
import com.aiautocreate.data.datasource.local.secure.SecureStorageManager.Keys
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * مدير تخزين آمن للبيانات الحساسة (مفاتيح API، رموز، إلخ).
 * يستخدم [EncryptedSharedPreferences] مع MasterKey من Android KeyStore.
 *
 * @param encryptedPrefs مثيل [EncryptedSharedPreferences] مهيأ مسبقاً.
 */
class SecureStorageManager(
    private val encryptedPrefs: EncryptedSharedPreferences
) {

    /** المفاتيح المخزنة داخل التخزين المشفر */
    private object Keys {
        const val GEMINI_API_KEY = "gemini_api_key"
        const val HUGGINGFACE_API_KEY = "huggingface_api_key"
        const val REFRESH_TOKEN = "refresh_token"
        const val ACCESS_TOKEN = "access_token"
        const val HAS_BIOMETRIC_ENABLED = "has_biometric_enabled"
    }

    // ========== مفاتيح API ==========

    /**
     * يحفظ مفتاح Gemini API بشكل آمن.
     */
    fun saveGeminiApiKey(apiKey: String) {
        encryptedPrefs.edit()
            .putString(Keys.GEMINI_API_KEY, apiKey)
            .apply()
    }

    /**
     * يسترجع مفتاح Gemini API، أو null إذا لم يُخزّن بعد.
     */
    fun getGeminiApiKey(): String? {
        return encryptedPrefs.getString(Keys.GEMINI_API_KEY, null)
    }

    /**
     * يحفظ مفتاح HuggingFace API بشكل آمن.
     */
    fun saveHuggingFaceApiKey(apiKey: String) {
        encryptedPrefs.edit()
            .putString(Keys.HUGGINGFACE_API_KEY, apiKey)
            .apply()
    }

    /**
     * يسترجع مفتاح HuggingFace API، أو null إذا لم يُخزّن بعد.
     */
    fun getHuggingFaceApiKey(): String? {
        return encryptedPrefs.getString(Keys.HUGGINGFACE_API_KEY, null)
    }

    // ========== إدارة الرموز (Tokens) – اختياري للمستقبل ==========

    /**
     * يحفظ رمز الوصول (Access Token) بشكل آمن.
     */
    fun saveAccessToken(token: String) {
        encryptedPrefs.edit()
            .putString(Keys.ACCESS_TOKEN, token)
            .apply()
    }

    /**
     * يسترجع رمز الوصول.
     */
    fun getAccessToken(): String? {
        return encryptedPrefs.getString(Keys.ACCESS_TOKEN, null)
    }

    /**
     * يحفظ رمز التحديث (Refresh Token) بشكل آمن.
     */
    fun saveRefreshToken(token: String) {
        encryptedPrefs.edit()
            .putString(Keys.REFRESH_TOKEN, token)
            .apply()
    }

    /**
     * يسترجع رمز التحديث.
     */
    fun getRefreshToken(): String? {
        return encryptedPrefs.getString(Keys.REFRESH_TOKEN, null)
    }

    // ========== إعدادات الأمان ==========

    /**
     * تفعيل/تعطيل المصادقة البيومترية وحفظ الإعداد.
     */
    fun setBiometricEnabled(enabled: Boolean) {
        encryptedPrefs.edit()
            .putBoolean(Keys.HAS_BIOMETRIC_ENABLED, enabled)
            .apply()
    }

    /**
     * هل المصادقة البيومترية مفعلة؟
     */
    fun isBiometricEnabled(): Boolean {
        return encryptedPrefs.getBoolean(Keys.HAS_BIOMETRIC_ENABLED, false)
    }

    // ========== دوال عامة ==========

    /**
     * يحذف مفتاحاً معيناً.
     */
    fun removeKey(key: String) {
        encryptedPrefs.edit()
            .remove(key)
            .apply()
    }

    /**
     * يمسح **جميع** البيانات الحساسة المخزنة (سلة محو كامل).
     * يُستخدم عند تسجيل الخروج الكامل أو حذف الحساب.
     */
    fun clearAll() {
        encryptedPrefs.edit()
            .clear()
            .apply()
    }

    /**
     * هل يوجد مفتاح API واحد على الأقل مخزن؟
     */
    fun hasAnyApiKey(): Boolean {
        return !getGeminiApiKey().isNullOrBlank() || !getHuggingFaceApiKey().isNullOrBlank()
    }

    // ========== حالة تفاعلية (اختياري) ==========

    private val _hasKeysState = MutableStateFlow(hasAnyApiKey())
    val hasKeysState: Flow<Boolean> = _hasKeysState.asStateFlow()

    /** ينعش حالة `hasKeysState` بعد أي تغيير */
    private fun refreshKeysState() {
        _hasKeysState.value = hasAnyApiKey()
    }

    /** دوال الحفظ المعدّلة لتنعش الحالة تلقائياً */
    fun saveGeminiApiKeyAndRefresh(apiKey: String) {
        saveGeminiApiKey(apiKey)
        refreshKeysState()
    }

    fun saveHuggingFaceApiKeyAndRefresh(apiKey: String) {
        saveHuggingFaceApiKey(apiKey)
        refreshKeysState()
    }
}