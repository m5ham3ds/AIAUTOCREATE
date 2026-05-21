package com.aiautocreate.agent

import com.aiautocreate.data.repository.AppSettingsRepository
import com.aiautocreate.domain.repository.ISettingsRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiKeyManager @Inject constructor(
    private val appSettingsRepo: AppSettingsRepository,
    private val secureSettingsRepo: ISettingsRepository
) {
    private val mutex = Mutex()
    private var currentKeyIndex = 0
    private var lastSuccessfulKey: String? = null

    private var cachedKeys: List<String> = emptyList()

    suspend fun refreshKeys() {
        mutex.withLock {
            val keysCsv = appSettingsRepo.getStringOnce("gemini_keys_csv", "")
            cachedKeys = if (keysCsv.isNotBlank()) {
                keysCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            } else {
                val singleKey = secureSettingsRepo.getGeminiKey()
                if (!singleKey.isNullOrBlank()) listOf(singleKey) else emptyList()
            }
            if (lastSuccessfulKey != null && cachedKeys.contains(lastSuccessfulKey)) {
                currentKeyIndex = cachedKeys.indexOf(lastSuccessfulKey)
            } else {
                currentKeyIndex = 0
                lastSuccessfulKey = cachedKeys.getOrNull(0)
            }
            Timber.d("تم تحديث قائمة مفاتيح Gemini، العدد: ${cachedKeys.size}")
        }
    }

    suspend fun getCurrentKey(): String? {
        mutex.withLock {
            if (cachedKeys.isEmpty()) refreshKeys()
            return cachedKeys.getOrNull(currentKeyIndex)
        }
    }

    suspend fun markFailureAndGetNext(): String? {
        mutex.withLock {
            if (cachedKeys.isEmpty()) return null
            currentKeyIndex = (currentKeyIndex + 1) % cachedKeys.size
            val nextKey = cachedKeys.getOrNull(currentKeyIndex)
            Timber.w("المفتاح الحالي فشل، التبديل إلى المفتاح التالي (الفهرس $currentKeyIndex)")
            return nextKey
        }
    }

    suspend fun markSuccess() {
        mutex.withLock {
            lastSuccessfulKey = cachedKeys.getOrNull(currentKeyIndex)
            Timber.d("تم تسجيل نجاح المفتاح الحالي")
        }
    }

    suspend fun getAllKeys(): List<String> = mutex.withLock { cachedKeys.toList() }
}
