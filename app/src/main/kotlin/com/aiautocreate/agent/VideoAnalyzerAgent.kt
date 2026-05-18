package com.aiautocreate.agent

import com.aiautocreate.data.datasource.remote.api.GeminiApi
import com.aiautocreate.data.datasource.remote.dto.request.Content
import com.aiautocreate.data.datasource.remote.dto.request.GeminiRequestDto
import com.aiautocreate.data.datasource.remote.dto.request.Part
import javax.inject.Inject

/**
 * وكيل يحلل محتوى الفيديو باستخدام Gemini لاستخراج وصف هيكلي.
 */
class VideoAnalyzerAgent @Inject constructor(
    private val geminiApi: GeminiApi
) : AgentBase() {

    override val agentName = "VideoAnalyzer"

    override suspend fun execute(input: Any): AgentResult {
        val prompt = input as? String ?: return AgentResult(false, errorMessage = "مدخلات غير صالحة")
        return try {
            val request = GeminiRequestDto(
                contents = listOf(Content(parts = listOf(Part(text = prompt))))
            )
            val response = geminiApi.generateContentWithKey(request)
            if (response.isSuccessful) {
                val text = response.body()?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                AgentResult(true, data = text)
            } else {
                AgentResult(false, errorMessage = "فشل تحليل الفيديو: ${response.code()}")
            }
        } catch (e: Exception) {
            AgentResult(false, errorMessage = e.message)
        }
    }
}