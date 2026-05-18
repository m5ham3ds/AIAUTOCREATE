package com.aiautocreate.domain.repository

import com.aiautocreate.domain.model.ModelConfig
import kotlinx.coroutines.flow.Flow

interface IModelsRepository {
    fun getAllModelConfigs(): Flow<List<ModelConfig>>
    fun getEnabledModels(): Flow<List<ModelConfig>>
    suspend fun getModelConfigById(id: Long): ModelConfig?
    fun getModelsByProvider(provider: String): Flow<List<ModelConfig>>
    suspend fun insertModelConfig(config: ModelConfig): Long
    suspend fun updateModelConfig(config: ModelConfig)
    suspend fun deleteModelConfig(config: ModelConfig)
    suspend fun setModelEnabled(id: Long, enabled: Boolean)
}