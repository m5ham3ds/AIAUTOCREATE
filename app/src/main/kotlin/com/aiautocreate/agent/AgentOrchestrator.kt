package com.aiautocreate.agent

import com.aiautocreate.data.datasource.remote.api.GeminiApi
import com.aiautocreate.data.datasource.remote.api.HuggingFaceApi
import com.aiautocreate.data.repository.AppSettingsRepository
import com.aiautocreate.domain.model.ModelConfig
import com.aiautocreate.domain.repository.IModelsRepository
import com.aiautocreate.domain.repository.ISettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AgentOrchestrator @Inject constructor(
    private val geminiApi: GeminiApi,
    private val huggingFaceApi: HuggingFaceApi,
    private val modelsRepository: IModelsRepository,
    private val appSettingsRepo: AppSettingsRepository,
    private val secureSettingsRepo: ISettingsRepository,
    private val contextProvider: ProjectContextProvider,
    private val geminiKeyManager: GeminiKeyManager,
    private val tokenManager: HuggingFaceTokenManager
) {
    private val _events = MutableSharedFlow<AgentEvent>()
    val events = _events.asSharedFlow()

    private var maxInterventionDepth: Int = 3

    init {
        CoroutineScope(Dispatchers.IO).launch {
            maxInterventionDepth = appSettingsRepo.getStringOnce("agent_depth", "3").toIntOrNull() ?: 3
            geminiKeyManager.refreshKeys()
            tokenManager.refreshTokens()
        }
    }

    suspend fun updateMaxDepth(depth: Int) {
        maxInterventionDepth = depth
        appSettingsRepo.setString("agent_depth", depth.toString())
    }

    suspend fun getCurrentMaxDepth(): Int = maxInterventionDepth

    suspend fun suggestAlternativeModel(
        category: String,
        failedModelId: String,
        errorMessage: String,
        currentDepth: Int = 1
    ): ModelConfig? {
        if (currentDepth > maxInterventionDepth) {
            emitEvent(AgentEvent.InterventionSkipped(category, "تم تجاوز عمق التدخل المسموح ($maxInterventionDepth)", currentDepth))
            return null
        }
        val allModels = modelsRepository.getAllModelConfigs().first()
        val candidates = allModels.filter { it.isEnabled && it.category.equals(category, ignoreCase = true) && it.modelId != failedModelId }
        if (candidates.isEmpty()) {
            emitEvent(AgentEvent.NoAlternatives(category, failedModelId))
            return null
        }
        val prompt = buildGeminiPrompt(category, failedModelId, errorMessage, candidates)
        val suggestedModelId = askGeminiForSuggestion(prompt)
        val selected = if (suggestedModelId != null) candidates.find { it.modelId == suggestedModelId } else candidates.firstOrNull()
        if (selected != null) {
            emitEvent(AgentEvent.AlternativeSuggested(category, failedModelId, selected.modelId, selected.modelName, errorMessage, currentDepth))
        }
        return selected
    }

    suspend fun suggestTransition(prevScene: String, nextScene: String, candidates: List<Asset>): Asset? {
        val prompt = """
            أنت خبير مونتاج فيديو محترف.
            المشهد السابق: ${prevScene.take(200)}
            المشهد الحالي: ${nextScene.take(200)}
            أنواع الانتقالات المتاحة:
            ${candidates.joinToString("\n") { "- ${it.name} (${it.command})" }}
            أي انتقال هو الأنسب للانتقال من المشهد السابق إلى الحالي؟
            أجب فقط بـ "id" للانتقال كما هو مكتوب في القائمة، ولا تكتب أي شيء آخر.
        """.trimIndent()
        val suggestedId = askGeminiForSuggestion(prompt)
        return candidates.find { it.id == suggestedId }
    }

    suspend fun suggestMusic(overallTheme: String, sceneDescriptions: List<String>, candidates: List<Asset>): Asset? {
        val scenesSummary = sceneDescriptions.take(3).joinToString("\n") { "- $it" }
        val prompt = """
            أنت خبير اختيار موسيقى خلفية للأفلام.
            موضوع الفيديو العام: ${overallTheme.take(150)}
            وصف أول 3 مشاهد:
            $scenesSummary
            قائمة الموسيقى المتاحة:
            ${candidates.joinToString("\n") { "- ${it.name}" }}
            أي قطعة موسيقية هي الأنسب كخلفية لهذا الفيديو؟
            أجب فقط بـ "id" للموسيقى كما هو مكتوب في القائمة، ولا تكتب أي شيء آخر.
        """.trimIndent()
        val suggestedId = askGeminiForSuggestion(prompt)
        return candidates.find { it.id == suggestedId }
    }

    suspend fun suggestSoundEffects(sceneDescription: String, candidates: List<Asset>): List<Asset> {
        val prompt = """
            أنت خبير مؤثرات صوتية للأفلام.
            وصف المشهد: ${sceneDescription.take(200)}
            قائمة المؤثرات الصوتية المتاحة:
            ${candidates.joinToString("\n") { "- ${it.name}" }}
            حدد أي المؤثرات الصوتية (قد تكون صفر أو واحد أو أكثر) تناسب هذا المشهد.
            أجب بقائمة مفصولة بفواصل تحتوي على "id" لكل مؤثر مناسب، مثال: "sfx1,sfx3"
            إذا لم يكن أي مناسب، أجب بـ "none".
        """.trimIndent()
        val answer = askGeminiForSuggestion(prompt) ?: "none"
        if (answer == "none") return emptyList()
        val selectedIds = answer.split(",").map { it.trim() }.filter { it.isNotBlank() }
        return candidates.filter { it.id in selectedIds }
    }

    suspend fun logIntervention(intervention: AgentInterventionLog) {
        emitEvent(AgentEvent.InterventionLogged(intervention))
    }

    // ==================== دوال الرد الرئيسية ====================
    suspend fun getContextualAnswer(question: String, requestedModelId: String? = null): String {
        val context = contextProvider.getQuickContext()
        val defaultModelId = appSettingsRepo.getDefaultAgentModelId()
        var currentModelId = requestedModelId ?: getCurrentModelId(defaultModelId)
        val fallbackOrder = appSettingsRepo.getFallbackAgentModelsOrder()
        val allModels = modelsRepository.getAllModelConfigs().first()

        // بناء قائمة النماذج التي سنحاولها (الحالي ثم الاحتياطي)
        val modelsToTry = mutableListOf(currentModelId)
        if (currentModelId != defaultModelId && defaultModelId.isNotBlank()) {
            modelsToTry.add(defaultModelId)
        }
        modelsToTry.addAll(fallbackOrder.filter { it != currentModelId && it != defaultModelId && it.isNotBlank() })

        var lastError = ""
        for (modelId in modelsToTry.distinct()) {
            val model = allModels.find { it.modelId == modelId && it.isEnabled }
            if (model == null) {
                lastError = "النموذج $modelId غير موجود أو معطل"
                continue
            }
            val result = executeModelRequest(modelId, context, question)
            if (result != null) return result
            lastError = "فشل النموذج $modelId"
        }
        return "جميع النماذج فشلت. آخر خطأ: $lastError"
    }

    private suspend fun getCurrentModelId(defaultModelId: String): String {
        val tempId = appSettingsRepo.getStringOnce("temp_agent_model_id", "")
        return if (tempId.isNotBlank()) tempId else defaultModelId
    }

    private suspend fun executeModelRequest(modelId: String, context: String, question: String): String? {
        return when {
            modelId.startsWith("gemini-") -> executeGeminiRequest(context, question)
            else -> executeHuggingFaceRequest(modelId, context, question)
        }
    }

    private suspend fun executeGeminiRequest(context: String, question: String): String? {
        var attempts = 0
        val maxAttempts = geminiKeyManager.getAllKeys().size.coerceAtLeast(1)
        while (attempts < maxAttempts) {
            val apiKey = geminiKeyManager.getCurrentKey()
            if (apiKey.isNullOrBlank()) return null
            val fullPrompt = """
                أنت مساعد ذكي لتطبيق AI AutoCreate لتحرير الفيديو.
                إليك ملخص سريع عن التطبيق:
                $context
                سؤال المستخدم: $question
                أجب بإيجاز وبشكل مفيد. إذا كان السؤال عن خطأ معين، حلله واقترح حلاً.
            """.trimIndent()
            val request = com.aiautocreate.data.datasource.remote.dto.request.GeminiRequestDto(
                contents = listOf(
                    com.aiautocreate.data.datasource.remote.dto.request.Content(
                        parts = listOf(
                            com.aiautocreate.data.datasource.remote.dto.request.Part(text = fullPrompt)
                        )
                    )
                )
            )
            try {
                val response = withTimeoutOrNull(30000L) { geminiApi.generateContent(apiKey, request) }
                if (response?.isSuccessful == true) {
                    geminiKeyManager.markSuccess()
                    return response.body()?.candidates?.firstOrNull()?.content?.parts?.joinToString(" ") { it.text ?: "" }
                        ?: "عذراً، لم أستطع معالجة الطلب."
                } else {
                    val code = response?.code()
                    if (code == 429) {
                        geminiKeyManager.markRateLimitAndGetNext()
                        attempts++
                        continue
                    } else return null
                }
            } catch (e: Exception) { return null }
        }
        return null
    }

    private suspend fun executeHuggingFaceRequest(modelId: String, context: String, question: String): String? {
        var currentToken = tokenManager.getTokenForModel(modelId)
        var attempts = 0
        val maxAttempts = tokenManager.getAllTokens().size.coerceAtLeast(1)
        while (attempts < maxAttempts && currentToken != null) {
            val fullPrompt = """
                أنت مساعد ذكي. السياق: $context
                سؤال: $question
                أجب بإيجاز.
            """.trimIndent()
            val request = mapOf(
                "inputs" to fullPrompt,
                "parameters" to mapOf(
                    "max_new_tokens" to 500,
                    "temperature" to 0.7,
                    "do_sample" to true
                )
            )
            try {
                val response = withTimeoutOrNull(30000L) {
                    huggingFaceApi.generateText(modelId, request, "Bearer $currentToken")
                }
                if (response?.isSuccessful == true) {
                    tokenManager.markSuccess(modelId, currentToken)
                    val body = response.body()?.string()
                    return body ?: "لا يوجد رد."
                } else {
                    val code = response?.code()
                    if (code == 429) {
                        tokenManager.markRateLimit(modelId, currentToken)
                        currentToken = tokenManager.getNextTokenForModel(modelId, currentToken)
                        attempts++
                        continue
                    } else return null
                }
            } catch (e: Exception) {
                return null
            }
        }
        return null
    }

    // ==================== دوال التحليلات المتخصصة ====================
    suspend fun quickScan(): String {
        val context = contextProvider.getQuickContext()
        val defaultModelId = appSettingsRepo.getDefaultAgentModelId()
        val result = executeModelRequest(defaultModelId, context, "قم بتحليل سريع لحالة التطبيق: $context. قدم ملخصاً مختصراً (سطرين كحد أقصى).")
        return result ?: "فشل التحليل السريع."
    }

    suspend fun fullAnalysis(): String {
        val context = contextProvider.getFullContext()
        val defaultModelId = appSettingsRepo.getDefaultAgentModelId()
        val prompt = """
            أنت خبير في تحليل تطبيقات AI. قم بتحليل شامل للتطبيق بناءً على السياق التالي:
            $context
            أجب في النقاط التالية:
            1. حالة المشاريع والمشاكل المتوقعة.
            2. كفاءة النماذج المختارة واقتراح تحسينات.
            3. الأخطاء الأخيرة وتحليل أسبابها مع حلول مقترحة.
            4. توصيات عامة لتحسين الأداء.
        """.trimIndent()
        val result = executeModelRequest(defaultModelId, context, prompt)
        return result ?: "فشل التحليل الشامل."
    }

    suspend fun criticalErrorsCheck(): String {
        val context = contextProvider.getErrorContext()
        val defaultModelId = appSettingsRepo.getDefaultAgentModelId()
        val prompt = """
            أنت خبير أمن ومراقبة جودة. قم بفحص الأخطاء التالية:
            $context
            حدد الأخطاء الخطيرة فقط (التي تؤثر على عمل التطبيق)، واقترح حلاً عاجلاً لكل منها.
            إذا لم تكن هناك أخطاء خطيرة، اذكر ذلك.
        """.trimIndent()
        val result = executeModelRequest(defaultModelId, context, prompt)
        return result ?: "فشل فحص الأخطاء الخطيرة."
    }

    suspend fun refreshStats(): AgentStats = contextProvider.getStats()

    // ==================== دوال مساعدة ====================
    private suspend fun askGeminiForSuggestion(prompt: String): String? {
        var attempts = 0
        val maxAttempts = geminiKeyManager.getAllKeys().size.coerceAtLeast(1)
        while (attempts < maxAttempts) {
            val apiKey = geminiKeyManager.getCurrentKey()
            if (apiKey.isNullOrBlank()) return null
            val request = com.aiautocreate.data.datasource.remote.dto.request.GeminiRequestDto(
                contents = listOf(
                    com.aiautocreate.data.datasource.remote.dto.request.Content(
                        parts = listOf(
                            com.aiautocreate.data.datasource.remote.dto.request.Part(text = prompt)
                        )
                    )
                )
            )
            try {
                val response = withTimeoutOrNull(5000L) { geminiApi.generateContent(apiKey, request) }
                if (response?.isSuccessful == true) {
                    geminiKeyManager.markSuccess()
                    val result = response.body()?.candidates?.firstOrNull()?.content?.parts?.joinToString(" ") { it.text ?: "" }
                    return result?.trim()?.takeIf { it.isNotBlank() }
                } else {
                    val code = response?.code()
                    if (code == 429) {
                        geminiKeyManager.markRateLimitAndGetNext()
                        attempts++
                        continue
                    } else return null
                }
            } catch (e: Exception) {
                geminiKeyManager.markRateLimitAndGetNext()
                attempts++
            }
        }
        return null
    }

    private fun buildGeminiPrompt(category: String, failedModelId: String, errorMessage: String, candidates: List<ModelConfig>): String {
        val candidatesList = candidates.joinToString("\n") { "- ${it.modelId} (${it.modelName}): ${it.description.take(100)}" }
        return """
            أنت مساعد خبير في نماذج الذكاء الاصطناعي.
            الفئة: $category
            النموذج الفاشل: $failedModelId
            رسالة الخطأ: $errorMessage
            النماذج البديلة المتاحة:
            $candidatesList
            بناءً على رسالة الخطأ، أي من هذه النماذج البديلة هو الأنسب لتحل محل النموذج الفاشل؟
            أجب فقط بمعرف النموذج (modelId) كما هو مكتوب في القائمة، ولا تكتب أي شيء آخر.
        """.trimIndent()
    }

    private suspend fun emitEvent(event: AgentEvent) {
        _events.emit(event)
    }
}

sealed class AgentEvent {
    data class AlternativeSuggested(val category: String, val originalModelId: String, val suggestedModelId: String, val suggestedModelName: String, val reason: String, val depth: Int) : AgentEvent()
    data class InterventionSkipped(val category: String, val reason: String, val depth: Int) : AgentEvent()
    data class NoAlternatives(val category: String, val failedModelId: String) : AgentEvent()
    data class InterventionLogged(val intervention: AgentInterventionLog) : AgentEvent()
}

data class AgentInterventionLog(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val category: String,
    val failedModelId: String,
    val suggestedModelId: String,
    val success: Boolean,
    val errorMessage: String? = null,
    val depth: Int
)

data class Asset(
    val id: String,
    val name: String,
    val command: String? = null,
    val fileUrl: String? = null,
    val localPath: String? = null
)
