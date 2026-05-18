package com.aiautocreate.data.datasource.remote.api

import com.aiautocreate.data.datasource.remote.dto.request.GeminiRequestDto
import com.aiautocreate.data.datasource.remote.dto.response.GeminiResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * واجهة REST للتفاعل مع Gemini API (Google Generative Language).
 */
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

/**
 * دالة مساعدة (امتداد) لتوليد نص من برومبت واحد.
 * @param prompt النص المرسلة إلى Gemini.
 * @return النص الناتج من النموذج، أو null في حالة الخطأ.
 */
suspend fun GeminiApi.generateText(prompt: String): String? {
    val request = GeminiRequestDto(
        contents = listOf(
            mapOf(
                "parts" to listOf(
                    mapOf("text" to prompt)
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