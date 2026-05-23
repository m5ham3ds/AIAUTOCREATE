package com.aiautocreate.presentation.ui.screens.models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aiautocreate.AIAutoCreateApp   // ✅ استيراد التطبيق للتحقق من الإنترنت
import com.aiautocreate.agent.HuggingFaceTokenManager
import com.aiautocreate.data.datasource.remote.api.HuggingFaceApi
import com.aiautocreate.data.datasource.remote.dto.response.HfModelInfo
import com.aiautocreate.domain.model.ModelConfig
import com.aiautocreate.domain.repository.ISettingsRepository
import com.aiautocreate.domain.usecase.model.CheckApiModelsUseCase
import com.aiautocreate.domain.usecase.model.ManageModelsUseCase
import com.aiautocreate.util.NetworkUtils           // ✅ استيراد أداة الشبكة
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ModelsManagerViewModel @Inject constructor(
    private val manageModelsUseCase: ManageModelsUseCase,
    private val checkApiModelsUseCase: CheckApiModelsUseCase,
    private val huggingFaceApi: HuggingFaceApi,
    private val secureSettingsRepo: ISettingsRepository,
    private val tokenManager: HuggingFaceTokenManager
) : ViewModel() {

    private val _state = MutableStateFlow(ModelsManagerState())
    val state: StateFlow<ModelsManagerState> = _state.asStateFlow()

    init {
        loadModels()
        viewModelScope.launch { tokenManager.refreshTokens() }
    }

    // ===================== دوال إدارة الرسائل =====================
    private fun setSuccessMessage(message: String) {
        _state.update { it.copy(successMessage = message) }
    }
    private fun setErrorMessage(message: String) {
        _state.update { it.copy(errorMessage = message) }
    }
    private fun setInfoMessage(message: String) {
        _state.update { it.copy(infoMessage = message) }
    }
    fun clearMessages() {
        _state.update { it.copy(successMessage = null, errorMessage = null, infoMessage = null) }
    }

    fun loadModels() {
        viewModelScope.launch {
            val apiStatusFlow = secureSettingsRepo.observeHasApiKeys()
                .map { checkApiModelsUseCase.checkAll() }
                .catch { emit(emptyMap()) }

            combine(
                manageModelsUseCase.getAllModels(),
                apiStatusFlow
            ) { models, apiStatus ->
                models.map { model ->
                    val hasKey = apiStatus[model.provider] ?: false
                    model.copy(isEnabled = hasKey && model.isEnabled)
                }
            }.catch { e ->
                _state.update { it.copy(isLoading = false, errorMessage = e.message) }
            }.collect { updatedModels ->
                _state.update { it.copy(models = updatedModels, isLoading = false) }
            }
        }
    }

    fun toggleModel(id: Long, enabled: Boolean) {
        viewModelScope.launch {
            manageModelsUseCase.toggleModel(id, enabled)
        }
    }

    fun deleteModel(model: ModelConfig) {
        viewModelScope.launch {
            manageModelsUseCase.removeModel(model)
            loadModels()
            setSuccessMessage("🗑️ تم حذف النموذج \"${model.modelName}\" بنجاح")
        }
    }

    fun updateModel(model: ModelConfig) {
        viewModelScope.launch {
            manageModelsUseCase.updateModel(model)
            loadModels()
            setSuccessMessage("✏️ تم تحديث النموذج \"${model.modelName}\" بنجاح")
        }
    }

    fun onSearchQueryChanged(query: String) {
        _state.update { it.copy(searchQuery = query, searchError = null) }
    }

    fun searchModelOnHuggingFace() {
        val query = _state.value.searchQuery.trim()
        if (query.isEmpty()) {
            _state.update { it.copy(searchError = "الرجاء إدخال معرف النموذج") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isSearching = true, searchError = null, searchedModel = null) }
            val modelId = query
            var currentToken = tokenManager.getTokenForModel(modelId)
            var attempts = 0
            val maxAttempts = tokenManager.getAllTokens().size.coerceAtLeast(1)
            while (attempts < maxAttempts && currentToken != null) {
                try {
                    val authHeader = "Bearer $currentToken"
                    val response = huggingFaceApi.getModelInfo(query, authHeader)
                    if (response.isSuccessful && response.body() != null) {
                        tokenManager.markSuccess(modelId, currentToken)
                        val modelInfo = response.body()!!
                        val editable = EditableModel(
                            original = modelInfo,
                            customName = modelInfo.cardData?.title?.takeIf { it.isNotBlank() }
                                ?: modelInfo.id.split("/").last(),
                            customDescription = modelInfo.cardData?.description ?: "لا يوجد وصف متاح",
                            isEnabled = true,
                            categories = listOf(guessCategoryFromPipelineTag(modelInfo.pipelineTag)),
                            settingsUrl = "",
                            readmeUrl = "https://huggingface.co/${modelInfo.id}/raw/main/README.md",
                            supportedStyles = "",
                            supportsVoiceCloning = false
                        )
                        _state.update {
                            it.copy(
                                isSearching = false,
                                searchedModel = modelInfo,
                                editableModel = editable,
                                searchError = null
                            )
                        }
                        return@launch
                    } else {
                        val code = response.code()
                        if (code == 429) {
                            tokenManager.markRateLimit(modelId, currentToken)
                            currentToken = tokenManager.getNextTokenForModel(modelId, currentToken)
                            attempts++
                            continue
                        } else {
                            val errorMsg = when (code) {
                                401 -> "خطأ في المصادقة: التوكن غير صالح"
                                403 -> "غير مصرح: التوكن لا يملك صلاحية لهذا النموذج"
                                404 -> "النموذج غير موجود في HuggingFace"
                                else -> "فشل البحث: $code"
                            }
                            _state.update { it.copy(isSearching = false, searchError = errorMsg) }
                            return@launch
                        }
                    }
                } catch (e: HttpException) {
                    if (e.code() == 429) {
                        if (currentToken != null) {
                            tokenManager.markRateLimit(modelId, currentToken)
                            currentToken = tokenManager.getNextTokenForModel(modelId, currentToken)
                        }
                        attempts++
                    } else {
                        _state.update { it.copy(isSearching = false, searchError = "خطأ في الخادم (${e.code()})") }
                        return@launch
                    }
                } catch (e: Exception) {
                    _state.update { it.copy(isSearching = false, searchError = "فشل الاتصال: ${e.message}") }
                    return@launch
                }
            }
            _state.update { it.copy(isSearching = false, searchError = "جميع التوكنات فشلت مع هذا النموذج") }
        }
    }

    fun updateEditableModel(customName: String, customDescription: String) {
        _state.update { state ->
            val current = state.editableModel ?: return@update state
            state.copy(editableModel = current.copy(customName = customName, customDescription = customDescription))
        }
    }

    // ✅ دالة لتحديث قائمة الفئات
    fun updateEditableCategories(categories: List<String>) {
        _state.update { state ->
            val current = state.editableModel ?: return@update state
            state.copy(editableModel = current.copy(categories = categories))
        }
    }

    fun updateEditableSettingsUrl(url: String) {
        _state.update { state ->
            val current = state.editableModel ?: return@update state
            state.copy(editableModel = current.copy(settingsUrl = url))
        }
    }

    fun updateEditableReadmeUrl(url: String) {
        _state.update { state ->
            val current = state.editableModel ?: return@update state
            state.copy(editableModel = current.copy(readmeUrl = url))
        }
    }

    fun updateEditableSupportedStyles(styles: String) {
        _state.update { state ->
            val current = state.editableModel ?: return@update state
            state.copy(editableModel = current.copy(supportedStyles = styles))
        }
    }

    fun updateEditableVoiceCloning(enabled: Boolean) {
        _state.update { state ->
            val current = state.editableModel ?: return@update state
            state.copy(editableModel = current.copy(supportsVoiceCloning = enabled))
        }
    }

    fun addModelFromSearch() {
        val editable = _state.value.editableModel ?: return
        val original = editable.original

        val modelConfig = ModelConfig(
            id = 0,
            modelId = original.id,
            modelName = editable.customName,
            provider = "huggingface",
            isEnabled = editable.isEnabled,
            description = editable.customDescription,
            pipelineTag = original.pipelineTag ?: "",
            tags = original.tags ?: emptyList(),
            modelUrl = "https://huggingface.co/${original.id}",
            category = editable.categories.joinToString(","),   // ✅ تخزين الفئات كـ CSV
            settingsUrl = editable.settingsUrl,
            readmeUrl = editable.readmeUrl,
            supportedStyles = if (editable.supportedStyles.isNotBlank()) {
                editable.supportedStyles.split(",").map { it.trim() }
            } else emptyList(),
            supportsVoiceCloning = editable.supportsVoiceCloning,
            apiEndpoint = null,
            parametersJson = null,
            createdAt = System.currentTimeMillis()
        )

        viewModelScope.launch {
            _state.update { it.copy(isAdding = true) }
            manageModelsUseCase.addModel(modelConfig)
            _state.update { it.copy(isAdding = false, searchedModel = null, editableModel = null, searchQuery = "") }
            loadModels()
            setSuccessMessage("✅ تم إضافة النموذج \"${modelConfig.modelName}\" بنجاح")
        }
    }

    fun setSelectedCategory(category: String) {
        _state.update { it.copy(selectedCategoryForSearch = category) }
    }

    fun searchModelsByCategory() {
        viewModelScope.launch {
            // ✅ التحقق من الاتصال بالإنترنت
            if (!NetworkUtils.isOnline(AIAutoCreateApp.instance)) {
                setErrorMessage("📡 لا يوجد اتصال بالإنترنت. يرجى التحقق من اتصالك وإعادة المحاولة.")
                return@launch
            }
            _state.update { it.copy(isSearchingByCategory = true, categorySearchResults = emptyList()) }
            val generalToken = tokenManager.getAllTokens().firstOrNull()
            if (generalToken == null) {
                _state.update { it.copy(isSearchingByCategory = false, categorySearchResults = emptyList()) }
                setErrorMessage("لا توجد توكنات HuggingFace صالحة. يرجى إدخال توكن واحد على الأقل في الإعدادات.")
                return@launch
            }
            try {
                setInfoMessage("🔍 جاري البحث عن نماذج...")
                val authHeader = "Bearer $generalToken"
                val response = huggingFaceApi.searchModelsByCategory(
                    pipelineTag = _state.value.selectedCategoryForSearch,
                    limit = 30,
                    authorization = authHeader
                )
                if (response.isSuccessful && response.body() != null) {
                    _state.update { it.copy(categorySearchResults = response.body()!!, isSearchingByCategory = false) }
                    setSuccessMessage("✅ تم العثور على ${response.body()!!.size} نموذج")
                } else {
                    _state.update { it.copy(categorySearchResults = emptyList(), isSearchingByCategory = false) }
                    setErrorMessage("❌ فشل البحث: ${response.code()}")
                }
            } catch (e: Exception) {
                _state.update { it.copy(categorySearchResults = emptyList(), isSearchingByCategory = false) }
                setErrorMessage("❌ فشل البحث: ${e.message}")
            }
        }
    }

    fun addModelFromSearchResult(modelInfo: HfModelInfo) {
        val editable = EditableModel(
            original = modelInfo,
            customName = modelInfo.cardData?.title?.takeIf { it.isNotBlank() }
                ?: modelInfo.id.split("/").last(),
            customDescription = modelInfo.cardData?.description ?: "لا يوجد وصف متاح",
            isEnabled = true,
            categories = listOf(guessCategoryFromPipelineTag(modelInfo.pipelineTag)),
            settingsUrl = "",
            readmeUrl = "https://huggingface.co/${modelInfo.id}/raw/main/README.md",
            supportedStyles = "",
            supportsVoiceCloning = false
        )
        _state.update { state ->
            state.copy(
                searchedModel = modelInfo,
                editableModel = editable,
                searchError = null
            )
        }
    }

    private fun guessCategoryFromPipelineTag(tag: String?): String {
        return when (tag?.lowercase()) {
            "text-generation", "text2text-generation", "translation", "summarization",
            "question-answering", "conversational", "text-to-text", "fill-mask",
            "zero-shot-classification", "sentence-similarity", "feature-extraction" -> "text"
            "text-to-image", "image-to-image", "image-enhancement", "image-segmentation",
            "image-classification", "object-detection" -> "image"
            "image-to-video", "video-generation", "text-to-video", "video-to-video",
            "video-classification" -> "video"
            "text-to-speech", "text-to-audio", "audio-to-audio" -> "tts"
            "automatic-speech-recognition", "audio-classification", "voice-activity-detection" -> "analysis"
            "text-to-music", "music-generation", "audio-generation" -> "music"
            "video-transition", "transition" -> "transition"
            "code-generation", "command-generation", "text-to-bash", "shell-command" -> "ffmpeg"
            "translation" -> "subtitle"
            else -> "analysis"
        }
    }
}
