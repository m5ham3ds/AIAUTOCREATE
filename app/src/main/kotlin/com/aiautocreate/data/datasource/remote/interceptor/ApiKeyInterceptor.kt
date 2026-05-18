package com.aiautocreate.data.datasource.remote.interceptor

import com.aiautocreate.data.datasource.local.secure.SecureStorageManager
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * معترض يحقن مفتاح API المناسب في كل طلب حسب المضيف (Host).
 * - HuggingFace: يُضاف ترويسة `Authorization: Bearer <HF_API_KEY>`.
 * - Gemini: يُضاف معامل استعلام `?key=<GEMINI_API_KEY>`.
 */
@Singleton
class ApiKeyInterceptor @Inject constructor(
    private val secureStorage: SecureStorageManager
) : Interceptor {

    companion object {
        private const val HF_HOST = "api-inference.huggingface.co"
        private const val GEMINI_HOST = "generativelanguage.googleapis.com"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val host = originalRequest.url.host

        val newRequest = when {
            host.contains(HF_HOST) -> {
                val apiKey = secureStorage.getHuggingFaceApiKey()
                if (!apiKey.isNullOrBlank()) {
                    originalRequest.newBuilder()
                        .header("Authorization", "Bearer $apiKey")
                        .build()
                } else {
                    originalRequest
                }
            }
            host.contains(GEMINI_HOST) -> {
                val apiKey = secureStorage.getGeminiApiKey()
                if (!apiKey.isNullOrBlank()) {
                    val newUrl = originalRequest.url.newBuilder()
                        .addQueryParameter("key", apiKey)
                        .build()
                    originalRequest.newBuilder()
                        .url(newUrl)
                        .build()
                } else {
                    originalRequest
                }
            }
            else -> originalRequest
        }

        return chain.proceed(newRequest)
    }
}