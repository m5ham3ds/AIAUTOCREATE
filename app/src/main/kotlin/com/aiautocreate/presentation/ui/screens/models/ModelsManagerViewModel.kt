package com.aiautocreate.presentation.ui.screens.models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aiautocreate.data.datasource.remote.api.HuggingFaceApi
import com.aiautocreate.data.datasource.remote.dto.response.HfModelInfo
import com.aiautocreate.domain.model.ModelConfig
import com.aiautocreate.domain.repository.ISettingsRepository
import com.aiautocreate.domain.usecase.model.CheckApiModelsUseCase
import com.aiautocreate.domain.usecase.model.ManageModelsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject

@HiltViewModel
class ModelsManagerViewModel @Inject constructor(
    private val manageModelsUseCase: ManageModelsUseCase,
    private val checkApiModelsUseCase: CheckApiModelsUseCase,
    private val huggingFaceApi: HuggingFaceApi,
    private val secureSettingsRepo: ISettingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ModelsManagerState())
    val state: StateFlow<ModelsManagerState> = _state.asStateFlow()

    init {
        loadModels()
    }

    // ✅ جعل الدالة عامة لإعادة التحميل من الشاشة
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
        }
    }

    fun updateModel(model: ModelConfig) {
        viewModelScope.launch {
            manageModelsUseCase.updateModel(model)
            loadModels()
        }
    }

    private suspend fun getHuggingFaceToken(): String {
        return secureSettingsRepo.getHuggingFaceToken() ?: ""
    }

    fun onSearchQueryChanged(query: String) {
        _state.update { it.copy(searchQuery = query, searchError = null) }
    }

    fun searchModelOnHuggingFace() {
        val query = _state.value.searchQuery.trim()
        if (query.isEmpty()) {
            _state.update { it.copy(searchError = "الرجاء إدخال معرف النموذج (مثل: mistralai/Mistral-7B-Instruct)") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isSearching = true, searchError = null, searchedModel = null) }
            try {
                val token = getHuggingFaceToken()
                val authHeader = if (token.isNotBlank()) "Bearer $token" else ""
                val response = huggingFaceApi.getModelInfo(query, authHeader)
                if (response.isSuccessful && response.body() != null) {
                    val modelInfo = response.body()!!
                    val editable = EditableModel(
                        original = modelInfo,
                        customName = modelInfo.cardData?.title?.takeIf { it.isNotBlank() }
                            ?: modelInfo.id.split("/").last(),
                        customDescription = modelInfo.cardData?.description ?: "لا يوجد وصف متاح",
                        isEnabled = true,
                        category = guessCategoryFromPipelineTag(modelInfo.pipelineTag),
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
                } else {
                    val errorMsg = when (response.code()) {
                        401 -> "خطأ في المصادقة: تأكد من إدخال HuggingFace Token في الإعدادات"
                        404 -> "النموذج غير موجود في HuggingFace"
                        else -> "فشل البحث: ${response.code()}"
                    }
                    _state.update { it.copy(isSearching = false, searchError = errorMsg) }
                }
            } catch (e: HttpException) {
                _state.update { it.copy(isSearching = false, searchError = "خطأ في الخادم (${e.code()})") }
            } catch (e: Exception) {
                _state.update { it.copy(isSearching = false, searchError = "فشل الاتصال: ${e.message}") }
            }
        }
    }

    fun updateEditableModel(customName: String, customDescription: String) {
        _state.update { state ->
            val current = state.editableModel ?: return@update state
            state.copy(editableModel = current.copy(customName = customName, customDescription = customDescription))
        }
    }

    fun updateEditableCategory(category: String) {
        _state.update { state ->
            val current = state.editableModel ?: return@update state
            state.copy(editableModel = current.copy(category = category))
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
            category = editable.category,
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
        }
    }

    fun setSelectedCategory(category: String) {
        _state.update { it.copy(selectedCategoryForSearch = category) }
    }

    fun searchModelsByCategory() {
        viewModelScope.launch {
            _state.update { it.copy(isSearchingByCategory = true, categorySearchResults = emptyList()) }
            try {
                val token = getHuggingFaceToken()
                val authHeader = if (token.isNotBlank()) "Bearer $token" else ""
                val response = huggingFaceApi.searchModelsByCategory(
                    pipelineTag = _state.value.selectedCategoryForSearch,
                    limit = 30,
                    authorization = authHeader
                )
                if (response.isSuccessful && response.body() != null) {
                    _state.update { it.copy(categorySearchResults = response.body()!!, isSearchingByCategory = false) }
                } else {
                    _state.update { it.copy(categorySearchResults = emptyList(), isSearchingByCategory = false) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(categorySearchResults = emptyList(), isSearchingByCategory = false) }
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
            category = guessCategoryFromPipelineTag(modelInfo.pipelineTag),
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
