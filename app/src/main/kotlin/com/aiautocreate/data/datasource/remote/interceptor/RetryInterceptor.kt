package com.aiautocreate.data.datasource.remote.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * معترض إعادة المحاولة مع تأخير أسي (Exponential Backoff).
 * - يعيد المحاولة حتى 3 مرات عند فشل الشبكة أو أخطاء الخادم (5xx).
 * - لا يعيد المحاولة لأخطاء العميل (4xx) لأنها عادةً نهائية.
 */
@Singleton
class RetryInterceptor @Inject constructor() : Interceptor {

    companion object {
        private const val MAX_RETRIES = 3
        private const val INITIAL_DELAY_MS = 800L
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        var retryCount = 0
        var lastException: IOException? = null

        while (retryCount <= MAX_RETRIES) {
            try {
                val response = chain.proceed(chain.request())

                if (response.code in 500..599 && retryCount < MAX_RETRIES) {
                    Timber.w("إعادة المحاولة ${retryCount + 1}/$MAX_RETRIES بعد خطأ ${response.code}")
                    response.close()
                    retryCount++
                    exponentialBackoff(retryCount)
                } else {
                    return response // نجاح أو خطأ لا يستحق الإعادة
                }
            } catch (e: IOException) {
                lastException = e
                if (retryCount < MAX_RETRIES) {
                    Timber.w(e, "إعادة المحاولة ${retryCount + 1}/$MAX_RETRIES بعد IOException")
                    retryCount++
                    exponentialBackoff(retryCount)
                } else {
                    throw e // استنفذت المحاولات
                }
            }
        }
        // لن نصل هنا عادةً
        throw lastException ?: IOException("فشل بعد $MAX_RETRIES محاولات")
    }

    private fun exponentialBackoff(retryCount: Int) {
        val delay = INITIAL_DELAY_MS * (1L shl (retryCount - 1))
        Thread.sleep(delay)
    }
}