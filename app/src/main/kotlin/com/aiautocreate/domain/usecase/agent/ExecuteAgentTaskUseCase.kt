package com.aiautocreate.domain.usecase.agent

import com.aiautocreate.agent.AgentInterventionHandler
import com.aiautocreate.agent.AgentOrchestrator
import com.aiautocreate.data.datasource.remote.api.HuggingFaceApi
import com.aiautocreate.data.datasource.remote.dto.request.HfImageRequestDto
import com.aiautocreate.data.datasource.remote.dto.request.HfTtsRequestDto
import com.aiautocreate.domain.model.AgentResult
import javax.inject.Inject

class ExecuteAgentTaskUseCase @Inject constructor(
    private val huggingFaceApi: HuggingFaceApi,
    private val interventionHandler: AgentInterventionHandler,
    private val agentOrchestrator: AgentOrchestrator
) {

    suspend fun execute(taskId: String, input: String, style: String = "", category: String = ""): AgentResult {
        val effectiveModelId = interventionHandler.getEffectiveModel(category)
        if (effectiveModelId.isBlank()) {
            return AgentResult(false, errorMessage = "لا يوجد نموذج محدد للفئة $category")
        }

        var currentDepth = 1
        var currentModelId = effectiveModelId
        var lastError: String? = null

        while (currentDepth <= agentOrchestrator.getCurrentMaxDepth()) {
            val result = when (taskId) {
                "generate_image" -> generateImageWithModel(currentModelId, input)
                "generate_audio" -> generateAudioWithModel(currentModelId, input)
                else -> AgentResult(false, errorMessage = "المهمة $taskId غير مدعومة")
            }

            if (result.success) return result

            lastError = result.errorMessage ?: "فشل غير معروف"
            if (currentDepth >= agentOrchestrator.getCurrentMaxDepth()) break

            val alternative = agentOrchestrator.suggestAlternativeModel(
                category = category,
                failedModelId = currentModelId,
                errorMessage = lastError,
                currentDepth = currentDepth
            )

            if (alternative == null) break

            interventionHandler.temporarilySwitchModel(category, alternative.modelId)
            currentModelId = alternative.modelId
            currentDepth++
        }

        return AgentResult(false, errorMessage = "فشل التنفيذ بعد $currentDepth محاولة: $lastError")
    }

    private suspend fun generateImageWithModel(modelId: String, prompt: String): AgentResult {
        return try {
            val request = HfImageRequestDto(inputs = prompt)
            val response = huggingFaceApi.generateImage(modelId, request)
            if (response.isSuccessful && response.body() != null) {
                val bytes = response.body()!!.byteStream().readBytes()
                AgentResult(success = true, output = "تم توليد الصورة بنجاح", data = bytes)
            } else {
                AgentResult(false, errorMessage = "فشل توليد الصورة: ${response.code()}")
            }
        } catch (e: Exception) {
            AgentResult(false, errorMessage = e.message)
        }
    }

    private suspend fun generateAudioWithModel(modelId: String, text: String): AgentResult {
        return try {
            val request = HfTtsRequestDto(inputs = text)
            val response = huggingFaceApi.generateSpeech(modelId, request)
            if (response.isSuccessful && response.body() != null) {
                val bytes = response.body()!!.byteStream().readBytes()
                AgentResult(success = true, output = "تم توليد الصوت بنجاح", data = bytes)
            } else {
                AgentResult(false, errorMessage = "فشل توليد الصوت: ${response.code()}")
            }
        } catch (e: Exception) {
            AgentResult(false, errorMessage = e.message)
        }
    }
}
