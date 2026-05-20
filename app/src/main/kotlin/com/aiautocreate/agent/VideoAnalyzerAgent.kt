package com.aiautocreate.agent

import com.aiautocreate.data.datasource.remote.api.GeminiApi
import com.aiautocreate.data.datasource.remote.dto.request.Content
import com.aiautocreate.data.datasource.remote.dto.request.GeminiRequestDto
import com.aiautocreate.data.datasource.remote.dto.request.Part
import com.aiautocreate.domain.model.AgentResult
import com.aiautocreate.domain.repository.ISettingsRepository
import javax.inject.Inject

/**
 * وكيل يحلل محتوى الفيديو باستخدام Gemini لاستخراج وصف هيكلي.
 */
class VideoAnalyzerAgent @Inject constructor(
    private val geminiApi: GeminiApi,
    private val settingsRepository: ISettingsRepository   // ✅ حقن واجهة الإعدادات الآمنة
) {

    suspend fun execute(input: Any): AgentResult {
        val prompt = input as? String ?: return AgentResult(
            success = false,
            errorMessage = "مدخلات غير صالحة"
        )
        return try {
            // ✅ الحصول على مفتاح Gemini من التخزين الآمن
            val apiKey = settingsRepository.getGeminiKey()
            if (apiKey.isNullOrBlank()) {
                return AgentResult(
                    success = false,
                    errorMessage = "مفتاح Gemini API غير موجود. يرجى إدخاله في إعدادات النماذج."
                )
            }

            val request = GeminiRequestDto(
                contents = listOf(Content(parts = listOf(Part(text = prompt))))
            )
            // ✅ استخدام الدالة التي تستقبل المفتاح صراحة
            val response = geminiApi.generateContent(apiKey, request)
            if (response.isSuccessful) {
                val text = response.body()?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                AgentResult(
                    success = true,
                    output = text,
                    data = text
                )
            } else {
                AgentResult(
                    success = false,
                    errorMessage = "فشل تحليل الفيديو: ${response.code()}"
                )
            }
        } catch (e: Exception) {
            AgentResult(
                success = false,
                errorMessage = e.message
            )
        }
    }
}
