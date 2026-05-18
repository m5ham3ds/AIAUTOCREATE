package com.aiautocreate.agent

import com.aiautocreate.data.repository.AppSettingsRepository
import com.aiautocreate.domain.repository.IModelsRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AgentInterventionHandler @Inject constructor(
    private val settingsRepo: AppSettingsRepository,
    private val modelsRepository: IModelsRepository
) {

    suspend fun temporarilySwitchModel(category: String, newModelId: String): Boolean {
        val modelExists = modelsRepository.getAllModelConfigs().first()
            .any { it.modelId == newModelId && it.isEnabled }
        if (!modelExists) return false
        
        settingsRepo.setString("temp_selected_${category}_model", newModelId)
        return true
    }

    suspend fun retrySameModel(category: String): Boolean {
        settingsRepo.setString("temp_selected_${category}_model", "")
        return true
    }

    suspend fun applyPermanently(category: String, newModelId: String) {
        settingsRepo.setString("selected_model_$category", newModelId)
        settingsRepo.setString("temp_selected_${category}_model", "")
    }

    suspend fun getEffectiveModel(category: String): String {
        val tempModel = settingsRepo.getStringOnce("temp_selected_${category}_model", "")
        if (tempModel.isNotBlank()) return tempModel
        return settingsRepo.getStringOnce("selected_model_$category", "")
    }

    suspend fun clearTempModel(category: String) {
        settingsRepo.setString("temp_selected_${category}_model", "")
    }
}