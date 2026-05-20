package com.aiautocreate.data.datasource.remote.api

import com.aiautocreate.data.datasource.remote.dto.request.Content
import com.aiautocreate.data.datasource.remote.dto.request.GeminiRequestDto
import com.aiautocreate.data.datasource.remote.dto.request.Part
import com.aiautocreate.data.datasource.remote.dto.response.GeminiResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface GeminiApi {

    @POST("v1beta/models/gemini-2.0-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequestDto
    ): Response<GeminiResponseDto>

    @POST("v1beta/models/gemini-2.0-pro:generateContent")
    suspend fun generateContentPro(
        @Query("key") apiKey: String,
        @Body request: GeminiRequestDto
    ): Response<GeminiResponseDto>

    @POST("v1beta/models/gemini-2.0-flash:generateContent")
    suspend fun generateContentWithKey(
        @Body request: GeminiRequestDto
    ): Response<GeminiResponseDto>
}

// ========== دوال مساعدة ==========

/**
 * دالة مساعدة لتوليد نص من برومبت واحد باستخدام مفتاح API صريح.
 * هذه هي الدالة الموصى باستخدامها.
 * @param apiKey مفتاح Gemini API.
 * @param prompt النص المرسل إلى النموذج.
 * @return النص الناتج أو null في حالة الفشل.
 */
suspend fun GeminiApi.generateText(apiKey: String, prompt: String): String? {
    val request = GeminiRequestDto(
        contents = listOf(
            Content(
                parts = listOf(
                    Part(text = prompt)
                )
            )
        )
    )
    val response = generateContent(apiKey, request)
    return if (response.isSuccessful && response.body() != null) {
        response.body()?.candidates?.firstOrNull()
            ?.content?.parts?.joinToString(" ") { it.text ?: "" }
    } else {
        null
    }
}

/**
 * دالة مساعدة قديمة (للتوافق مع الكود القديم).
 * تعتمد على generateContentWithKey التي قد لا تعمل إذا لم يتم تعيين المفتاح عبر Interceptor.
 * يُفضل استخدام الدالة التي تستقبل المفتاح apiKey صراحة.
 */
@Deprecated("Use generateText(apiKey, prompt) instead", ReplaceWith("generateText(apiKey, prompt)"))
suspend fun GeminiApi.generateText(prompt: String): String? {
    val request = GeminiRequestDto(
        contents = listOf(
            Content(
                parts = listOf(
                    Part(text = prompt)
                )
            )
        )
    )
    val response = generateContentWithKey(request)
    return if (response.isSuccessful && response.body() != null) {
        response.body()?.candidates?.firstOrNull()
            ?.content?.parts?.joinToString(" ") { it.text ?: "" }
    } else {
        null
    }
}
