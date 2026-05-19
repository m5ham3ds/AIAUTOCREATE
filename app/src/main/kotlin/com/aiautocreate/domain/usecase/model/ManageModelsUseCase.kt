package com.aiautocreate.domain.usecase.model

import com.aiautocreate.domain.model.ModelConfig
import com.aiautocreate.domain.repository.IModelsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * حالة استخدام لإدارة نماذج الذكاء الاصطناعي.
 */
class ManageModelsUseCase @Inject constructor(
    private val modelsRepository: IModelsRepository
) {
    fun getAllModels(): Flow<List<ModelConfig>> = modelsRepository.getAllModelConfigs()

    fun getEnabledModels(): Flow<List<ModelConfig>> = modelsRepository.getEnabledModels()

    suspend fun toggleModel(id: Long, enabled: Boolean) {
        modelsRepository.setModelEnabled(id, enabled)
    }

    suspend fun addModel(model: ModelConfig): Long {
        return modelsRepository.insertModelConfig(model)
    }

    // ✅ الدالة الجديدة: تحديث نموذج موجود
    suspend fun updateModel(model: ModelConfig) {
        modelsRepository.updateModelConfig(model)
    }

    suspend fun removeModel(model: ModelConfig) {
        modelsRepository.deleteModelConfig(model)
    }
}
