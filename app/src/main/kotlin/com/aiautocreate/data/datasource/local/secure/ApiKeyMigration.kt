package com.aiautocreate.data.datasource.local.secure

/**
 * ينقل مفاتيح API من SharedPreferences القديم (أو BuildConfig) إلى
 * التخزين المشفر (EncryptedSharedPreferences) تلقائياً عند أول تشغيل.
 */
class ApiKeyMigration(
    private val secureStorage: SecureStorageManager
) {
    fun migrateIfNeeded() {
        // مثال لترحيل مفتاح Gemini من BuildConfig (غير آمن) إلى التخزين الآمن
        // إذا لم يكن هناك مفتاح مخزن بالفعل
        if (secureStorage.getGeminiApiKey().isNullOrBlank()) {
            val oldKey = com.aiautocreate.BuildConfig.GEMINI_API_KEY
            if (oldKey != "PLACEHOLDER" && oldKey.isNotBlank()) {
                secureStorage.saveGeminiApiKey(oldKey)
            }
        }

        if (secureStorage.getHuggingFaceApiKey().isNullOrBlank()) {
            val oldKey = com.aiautocreate.BuildConfig.HF_API_KEY
            if (oldKey != "PLACEHOLDER" && oldKey.isNotBlank()) {
                secureStorage.saveHuggingFaceApiKey(oldKey)
            }
        }
    }
}