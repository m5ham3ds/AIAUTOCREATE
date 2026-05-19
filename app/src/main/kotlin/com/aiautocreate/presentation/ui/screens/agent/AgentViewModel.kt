package com.aiautocreate.presentation.ui.screens.agent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aiautocreate.AIAutoCreateApp
import com.aiautocreate.agent.AgentEvent
import com.aiautocreate.agent.AgentInterventionLog
import com.aiautocreate.agent.AgentOrchestrator
import com.aiautocreate.data.datasource.remote.api.GeminiApi
import com.aiautocreate.data.datasource.remote.dto.request.Content
import com.aiautocreate.data.datasource.remote.dto.request.GeminiRequestDto
import com.aiautocreate.data.datasource.remote.dto.request.Part
import com.aiautocreate.data.repository.AppSettingsRepository
import com.aiautocreate.util.NetworkUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AgentViewModel @Inject constructor(
    private val settingsRepo: AppSettingsRepository,
    private val geminiApi: GeminiApi,
    private val agentOrchestrator: AgentOrchestrator
) : ViewModel() {

    private val _state = MutableStateFlow(AgentState())
    val state: StateFlow<AgentState> = _state.asStateFlow()

    init {
        loadPermissions()
        loadWelcomeMessage()
        observeAgentEvents()
    }

    private fun observeAgentEvents() {
        viewModelScope.launch {
            agentOrchestrator.events.collect { event ->
                when (event) {
                    is AgentEvent.AlternativeSuggested -> {
                        val intervention = AgentIntervention(
                            id = UUID.randomUUID().toString(),
                            type = "model_switch",
                            title = "تغيير نموذج ${event.category}",
                            description = "تم اقتراح نموذج ${event.suggestedModelName} بدلاً من ${event.originalModelId} بسبب: ${event.reason}",
                            status = "success",
                            timestamp = System.currentTimeMillis()
                        )
                        _state.update { it.copy(interventions = listOf(intervention) + it.interventions) }
                    }
                    is AgentEvent.InterventionSkipped -> {
                        val intervention = AgentIntervention(
                            id = UUID.randomUUID().toString(),
                            type = "skip",
                            title = "تجاوز التدخل",
                            description = event.reason,
                            status = "warning",
                            timestamp = System.currentTimeMillis()
                        )
                        _state.update { it.copy(interventions = listOf(intervention) + it.interventions) }
                    }
                    is AgentEvent.NoAlternatives -> {
                        val intervention = AgentIntervention(
                            id = UUID.randomUUID().toString(),
                            type = "no_alternatives",
                            title = "لا يوجد بدائل",
                            description = "لا توجد نماذج بديلة متاحة لفئة ${event.category}",
                            status = "failed",
                            timestamp = System.currentTimeMillis()
                        )
                        _state.update { it.copy(interventions = listOf(intervention) + it.interventions) }
                    }
                    is AgentEvent.InterventionLogged -> {
                        val intervention = AgentIntervention(
                            id = event.intervention.id,
                            type = "intervention",
                            title = "تدخل الوكيل",
                            description = "تم تغيير النموذج من ${event.intervention.failedModelId} إلى ${event.intervention.suggestedModelId}",
                            status = if (event.intervention.success) "success" else "failed",
                            timestamp = event.intervention.timestamp
                        )
                        _state.update { it.copy(interventions = listOf(intervention) + it.interventions) }
                    }
                }
            }
        }
    }

    private fun loadPermissions() {
        viewModelScope.launch {
            val perms = AgentPermissions(
                autoFixErrors = settingsRepo.getStringOnce("agent_auto_fix", "false").toBoolean(),
                autoRetry = settingsRepo.getStringOnce("agent_auto_retry", "false").toBoolean(),
                optimizeResources = settingsRepo.getStringOnce("agent_optimize", "false").toBoolean(),
                accessProjects = settingsRepo.getStringOnce("agent_access", "false").toBoolean(),
                maxInterventionDepth = settingsRepo.getStringOnce("agent_depth", "3").toIntOrNull() ?: 3
            )
            _state.update { it.copy(permissions = perms, isLoading = false) }
        }
    }

    private fun loadWelcomeMessage() {
        val welcome = ChatMessage(
            id = UUID.randomUUID().toString(),
            text = "مرحباً، أنا الوكيل الذكي لـ AI AutoCreate. يمكنك سؤالي عن أي شيء يخص المشروع، الأخطاء، الأدوات، أو النماذج.",
            isUser = false
        )
        _state.update { it.copy(chatMessages = listOf(welcome)) }
    }

    fun onTabSelected(tab: Int) {
        _state.update { it.copy(selectedTab = tab) }
    }

    fun onInputChanged(text: String) {
        _state.update { it.copy(inputText = text, chatError = null) }
    }

    fun sendMessage() {
        val text = _state.value.inputText.trim()
        if (text.isEmpty()) return

        // ✅ إضافة فحص الاتصال بالإنترنت
        if (!NetworkUtils.isOnline(AIAutoCreateApp.instance)) {
            _state.update {
                it.copy(
                    isChatLoading = false,
                    chatError = "لا يوجد اتصال بالإنترنت. يرجى التحقق من اتصالك وإعادة المحاولة."
                )
            }
            return
        }

        val userMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            text = text,
            isUser = true
        )

        _state.update { it.copy(
            chatMessages = it.chatMessages + userMessage,
            inputText = "",
            isChatLoading = true,
            chatError = null
        ) }

        viewModelScope.launch {
            try {
                // ✅ التأكد من وجود مفتاح Gemini API
                val apiKey = settingsRepo.getStringOnce("gemini_key")
                if (apiKey.isBlank()) {
                    _state.update {
                        it.copy(
                            isChatLoading = false,
                            chatError = "مفتاح Gemini API غير موجود. يرجى إدخاله في إعدادات النماذج."
                        )
                    }
                    return@launch
                }

                val request = GeminiRequestDto(
                    contents = listOf(Content(parts = listOf(Part(text = text))))
                )
                // استخدام generateContent مع تمرير المفتاح صراحة لضمان العمل
                val response = geminiApi.generateContent(apiKey, request)
                val replyText = if (response.isSuccessful) {
                    response.body()?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                        ?: "عذراً، لم أستطع فهم الرد."
                } else {
                    "فشل الاتصال بالخادم (${response.code()})"
                }

                val agentMessage = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    text = replyText,
                    isUser = false
                )
                _state.update { it.copy(chatMessages = it.chatMessages + agentMessage, isChatLoading = false) }
            } catch (e: Exception) {
                _state.update { it.copy(isChatLoading = false, chatError = "فشل الاتصال: ${e.message}") }
            }
        }
    }

    fun togglePermission(permission: String) {
        _state.update { state ->
            val permissions = state.permissions
            val newPerms = when (permission) {
                "autoFixErrors" -> permissions.copy(autoFixErrors = !permissions.autoFixErrors)
                "autoRetry" -> permissions.copy(autoRetry = !permissions.autoRetry)
                "optimizeResources" -> permissions.copy(optimizeResources = !permissions.optimizeResources)
                "accessProjects" -> permissions.copy(accessProjects = !permissions.accessProjects)
                else -> permissions
            }
            state.copy(permissions = newPerms)
        }
        viewModelScope.launch {
            val newState = _state.value.permissions
            settingsRepo.setString("agent_auto_fix", newState.autoFixErrors.toString())
            settingsRepo.setString("agent_auto_retry", newState.autoRetry.toString())
            settingsRepo.setString("agent_optimize", newState.optimizeResources.toString())
            settingsRepo.setString("agent_access", newState.accessProjects.toString())
        }
    }

    fun onMaxDepthChanged(depth: Int) {
        _state.update { state ->
            state.copy(permissions = state.permissions.copy(maxInterventionDepth = depth))
        }
        viewModelScope.launch {
            settingsRepo.setString("agent_depth", depth.toString())
            agentOrchestrator.updateMaxDepth(depth)
        }
    }
}
