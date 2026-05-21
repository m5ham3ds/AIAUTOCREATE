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
    private var cachedTokens: List<String> = emptyList()
    // لكل نموذج -> آخر توكن نجح معه
    private val lastSuccessfulTokenForModel = mutableMapOf<String, String>()
    // لكل نموذج -> قائمة التوكنات التي فشلت معه مؤقتاً
    private val failedTokensForModel = mutableMapOf<String, MutableSet<String>>()

    suspend fun refreshTokens() {
        mutex.withLock {
            val tokensCsv = appSettingsRepo.getHuggingFaceTokensCsv()
            cachedTokens = if (tokensCsv.isNotBlank()) {
                tokensCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            } else {
                val singleToken = appSettingsRepo.getStringOnce("hf_token", "")
                if (singleToken.isNotBlank()) listOf(singleToken) else emptyList()
            }
            Timber.d("تم تحديث قائمة توكنات HuggingFace، العدد: ${cachedTokens.size}")
        }
    }

    /**
     * الحصول على التوكن المناسب لنموذج معين.
     * يفضل آخر توكن نجح مع هذا النموذج، ثم أول توكن لم يفشل معه.
     */
    suspend fun getTokenForModel(modelId: String): String? {
        mutex.withLock {
            if (cachedTokens.isEmpty()) refreshTokens()
            if (cachedTokens.isEmpty()) return null

            // 1. نفضل آخر توكن نجح مع هذا النموذج
            val lastSuccess = lastSuccessfulTokenForModel[modelId]
            if (lastSuccess != null && cachedTokens.contains(lastSuccess)) {
                return lastSuccess
            }

            // 2. نبحث عن أول توكن لم يفشل مع هذا النموذج
            val failed = failedTokensForModel[modelId] ?: emptySet()
            val candidate = cachedTokens.firstOrNull { !failed.contains(it) }
            if (candidate != null) return candidate

            // 3. جميع التوكنات فشلت، نعيد أقدمها (سيؤدي إلى محاولة جديدة)
            return cachedTokens.firstOrNull()
        }
    }

    /**
     * تسجيل نجاح توكن مع نموذج معين.
     */
    suspend fun markSuccess(modelId: String, token: String) {
        mutex.withLock {
            lastSuccessfulTokenForModel[modelId] = token
            failedTokensForModel[modelId]?.remove(token)
            Timber.d("✅ نجاح التوكن $token مع النموذج $modelId")
        }
    }

    /**
     * تسجيل فشل توكن مع نموذج معين بسبب تجاوز الحد (429).
     */
    suspend fun markRateLimit(modelId: String, token: String) {
        mutex.withLock {
            val failedSet = failedTokensForModel.getOrPut(modelId) { mutableSetOf() }
            failedSet.add(token)
            Timber.w("⚠️ التوكن $token تجاوز الحد مع النموذج $modelId (429)")
        }
    }

    /**
     * الحصول على التوكن التالي لنفس النموذج بعد فشل التوكن الحالي.
     */
    suspend fun getNextTokenForModel(modelId: String, currentFailedToken: String): String? {
        mutex.withLock {
            // سجل الفشل أولاً
            val failedSet = failedTokensForModel.getOrPut(modelId) { mutableSetOf() }
            failedSet.add(currentFailedToken)

            // ابحث عن توكن آخر لم يفشل بعد
            val available = cachedTokens.filter { !failedSet.contains(it) }
            val next = available.firstOrNull()
            if (next != null) return next

            // إذا لم يبقَ أي توكن، نعيد أول توكن (سيحاول من جديد)
            return cachedTokens.firstOrNull()
        }
    }

    suspend fun getAllTokens(): List<String> = mutex.withLock { cachedTokens.toList() }
}
