package com.aiautocreate.agent

import com.aiautocreate.data.repository.AppSettingsRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HuggingFaceTokenManager @Inject constructor(
    private val appSettingsRepo: AppSettingsRepository
) {
    private val mutex = Mutex()
    private var currentTokenIndex = 0
    private var lastSuccessfulToken: String? = null
    private var cachedTokens: List<String> = emptyList()

    suspend fun refreshTokens() {
        mutex.withLock {
            val tokensCsv = appSettingsRepo.getHuggingFaceTokensCsv()
            cachedTokens = if (tokensCsv.isNotBlank()) {
                tokensCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            } else {
                val singleToken = appSettingsRepo.getStringOnce("hf_token", "")
                if (singleToken.isNotBlank()) listOf(singleToken) else emptyList()
            }
            if (lastSuccessfulToken != null && cachedTokens.contains(lastSuccessfulToken)) {
                currentTokenIndex = cachedTokens.indexOf(lastSuccessfulToken)
            } else {
                currentTokenIndex = 0
                lastSuccessfulToken = cachedTokens.getOrNull(0)
            }
            Timber.d("تم تحديث قائمة توكنات HuggingFace، العدد: ${cachedTokens.size}")
        }
    }

    suspend fun getCurrentToken(): String? {
        mutex.withLock {
            if (cachedTokens.isEmpty()) refreshTokens()
            return cachedTokens.getOrNull(currentTokenIndex)
        }
    }

    suspend fun markFailureAndGetNext(): String? {
        mutex.withLock {
            if (cachedTokens.isEmpty()) return null
            currentTokenIndex = (currentTokenIndex + 1) % cachedTokens.size
            val nextToken = cachedTokens.getOrNull(currentTokenIndex)
            Timber.w("التوكن الحالي فشل، التبديل إلى التالي (الفهرس $currentTokenIndex)")
            return nextToken
        }
    }

    suspend fun markSuccess() {
        mutex.withLock {
            lastSuccessfulToken = cachedTokens.getOrNull(currentTokenIndex)
            Timber.d("تم تسجيل نجاح التوكن الحالي")
        }
    }

    suspend fun getAllTokens(): List<String> = mutex.withLock { cachedTokens.toList() }
}
