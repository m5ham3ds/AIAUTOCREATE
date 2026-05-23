package com.aiautocreate.presentation.ui.screens.models

import com.aiautocreate.data.datasource.remote.dto.response.HfModelInfo
import com.aiautocreate.domain.model.ModelConfig

data class ModelsManagerState(
    val models: List<ModelConfig> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val successMessage: String? = null,   // ✅ رسالة نجاح
    val infoMessage: String? = null,      // ✅ رسالة معلومات (مثل بدء البحث)
    val searchQuery: String = "",
    val isSearching: Boolean = false,
    val searchedModel: HfModelInfo? = null,
    val searchError: String? = null,
    val editableModel: EditableModel? = null,
    val isAdding: Boolean = false,
    val categorySearchResults: List<HfModelInfo> = emptyList(),
    val isSearchingByCategory: Boolean = false,
    val selectedCategoryForSearch: String = "text-to-image"
)

data class EditableModel(
    val original: HfModelInfo,
    val customName: String,
    val customDescription: String,
    val isEnabled: Boolean = true,
    val categories: List<String> = listOf("analysis"),   // ✅ تغيير category إلى categories (قائمة)
    val settingsUrl: String = "",
    val readmeUrl: String = "",
    val supportedStyles: String = "",
    val supportsVoiceCloning: Boolean = false
)
