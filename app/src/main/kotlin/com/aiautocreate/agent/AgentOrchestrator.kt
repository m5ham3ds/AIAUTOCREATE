package com.aiautocreate.agent

import com.aiautocreate.data.datasource.remote.api.GeminiApi
import com.aiautocreate.data.repository.AppSettingsRepository
import com.aiautocreate.domain.model.ModelConfig
import com.aiautocreate.domain.repository.IModelsRepository
import com.aiautocreate.domain.repository.ISettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AgentOrchestrator @Inject constructor(
    private val geminiApi: GeminiApi,
    private val modelsRepository: IModelsRepository,
    private val appSettingsRepo: AppSettingsRepository,
    private val secureSettingsRepo: ISettingsRepository,
    private val contextProvider: ProjectContextProvider,
    private val geminiKeyManager: GeminiKeyManager   // ✅ إضافة مدير المفاتيح
) {
    private val _events = MutableSharedFlow<AgentEvent>()
    val events = _events.asSharedFlow()

    private var maxInterventionDepth: Int = 3

    init {
        CoroutineScope(Dispatchers.IO).launch {
            maxInterventionDepth = appSettingsRepo.getStringOnce("agent_depth", "3").toIntOrNull() ?: 3
            geminiKeyManager.refreshKeys()   // تحميل المفاتيح عند البدء
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

    suspend fun getContextualAnswer(question: String): String {
        val context = contextProvider.getFullContext()
        var attempts = 0
        val maxAttempts = geminiKeyManager.getAllKeys().size.coerceAtLeast(1)
        while (attempts < maxAttempts) {
            val apiKey = geminiKeyManager.getCurrentKey()
            if (apiKey.isNullOrBlank()) {
                return "عذراً، لا توجد مفاتيح Gemini متاحة. يرجى إدخال مفتاح واحد على الأقل في الإعدادات."
            }
            val fullPrompt = """
                أنت مساعد ذكي لتطبيق AI AutoCreate لتحرير الفيديو وإنشاء المحتوى.
                إليك السياق الحالي للتطبيق:
                
                $context
                
                سؤال المستخدم: $question
                
                أجب بإيجاز وبشكل مفيد بناءً على السياق أعلاه. إذا كان السؤال عن خطأ معين، حاول تحليله واقتراح حل.
                إذا كان السؤال عن إجراءات أو توصيات، قدم نصائح عملية.
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
                val response = withTimeoutOrNull(10000L) { geminiApi.generateContent(apiKey, request) }
                if (response?.isSuccessful == true) {
                    geminiKeyManager.markSuccess()
                    return response.body()?.candidates?.firstOrNull()?.content?.parts?.joinToString(" ") { it.text ?: "" }
                        ?: "عذراً، لم أستطع معالجة الطلب."
                } else {
                    val code = response?.code()
                    if (code == 429) {
                        Timber.w("المفتاح الحالي تجاوز الحد (429)، التبديل إلى التالي")
                        geminiKeyManager.markFailureAndGetNext()
                        attempts++
                        continue
                    } else {
                        return "حدث خطأ في الاتصال: ${response?.code() ?: "timeout"}"
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "استثناء أثناء getContextualAnswer")
                geminiKeyManager.markFailureAndGetNext()
                attempts++
            }
        }
        return "جميع مفاتيح Gemini تجاوزت الحد المسموح أو غير صالحة. يرجى المحاولة لاحقاً أو إضافة مفاتيح جديدة."
    }

    suspend fun refreshStats(): AgentStats {
        return contextProvider.getStats()
    }

    private suspend fun askGeminiForSuggestion(prompt: String): String? {
        var attempts = 0
        val maxAttempts = geminiKeyManager.getAllKeys().size.coerceAtLeast(1)
        while (attempts < maxAttempts) {
            val apiKey = geminiKeyManager.getCurrentKey()
            if (apiKey.isNullOrBlank()) {
                Timber.e("لا يوجد مفتاح Gemini صالح")
                return null
            }
            try {
                val request = com.aiautocreate.data.datasource.remote.dto.request.GeminiRequestDto(
                    contents = listOf(
                        com.aiautocreate.data.datasource.remote.dto.request.Content(
                            parts = listOf(
                                com.aiautocreate.data.datasource.remote.dto.request.Part(text = prompt)
                            )
                        )
                    )
                )
                val response = withTimeoutOrNull(5000L) { geminiApi.generateContent(apiKey, request) }
                if (response?.isSuccessful == true) {
                    geminiKeyManager.markSuccess()
                    val result = response.body()?.candidates?.firstOrNull()?.content?.parts?.joinToString(" ") { it.text ?: "" }
                    return result?.trim()?.takeIf { it.isNotBlank() }
                } else {
                    val code = response?.code()
                    if (code == 429) {
                        Timber.w("المفتاح الحالي تجاوز الحد (429)، التبديل إلى التالي")
                        geminiKeyManager.markFailureAndGetNext()
                        attempts++
                        continue
                    } else {
                        Timber.e("خطأ Gemini: $code")
                        return null
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "استثناء أثناء طلب Gemini")
                geminiKeyManager.markFailureAndGetNext()
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
