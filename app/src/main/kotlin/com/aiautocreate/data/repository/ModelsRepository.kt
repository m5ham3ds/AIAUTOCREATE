package com.aiautocreate.data.repository

import com.aiautocreate.data.datasource.local.db.dao.ModelConfigDao
import com.aiautocreate.data.datasource.local.db.entities.ModelConfigEntity
import com.aiautocreate.domain.model.ModelConfig
import com.aiautocreate.domain.repository.IModelsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ModelsRepository @Inject constructor(
    private val modelConfigDao: ModelConfigDao
) : IModelsRepository {

    override fun getAllModelConfigs(): Flow<List<ModelConfig>> =
        modelConfigDao.getAllModelConfigs().map { list -> list.map { it.toDomain() } }

    override fun getEnabledModels(): Flow<List<ModelConfig>> =
        modelConfigDao.getEnabledModels().map { list -> list.map { it.toDomain() } }

    override suspend fun getModelConfigById(id: Long): ModelConfig? =
        modelConfigDao.getModelConfigById(id)?.toDomain()

    override fun getModelsByProvider(provider: String): Flow<List<ModelConfig>> =
        modelConfigDao.getModelsByProvider(provider).map { list -> list.map { it.toDomain() } }

    override suspend fun insertModelConfig(config: ModelConfig): Long =
        modelConfigDao.insertModelConfig(config.toEntity())

    override suspend fun updateModelConfig(config: ModelConfig) =
        modelConfigDao.updateModelConfig(config.toEntity())

    override suspend fun deleteModelConfig(config: ModelConfig) =
        modelConfigDao.deleteModelConfig(config.toEntity())

    override suspend fun setModelEnabled(id: Long, enabled: Boolean) =
        modelConfigDao.setModelEnabled(id, enabled)
}

// تحويل من الكيان (Entity) إلى نموذج (Model)
private fun ModelConfigEntity.toDomain(): ModelConfig {
    val tagsList = if (tags.isBlank()) emptyList() else tags.split(",").map { it.trim() }
    val stylesList = if (supportedStyles.isBlank()) emptyList() else supportedStyles.split(",").map { it.trim() }
    return ModelConfig(
        id = id,
        modelId = modelId,
        modelName = modelName,
        provider = provider,
        isEnabled = isEnabled,
        description = description,
        pipelineTag = pipelineTag,
        tags = tagsList,
        modelUrl = modelUrl,
        category = category,
        settingsUrl = settingsUrl,
        readmeUrl = readmeUrl,
        supportedStyles = stylesList,
        supportsVoiceCloning = supportsVoiceCloning,
        apiEndpoint = apiEndpoint,
        parametersJson = parametersJson,
        createdAt = createdAt
    )
}

// تحويل من نموذج (Model) إلى كيان (Entity)
private fun ModelConfig.toEntity(): ModelConfigEntity {
    val tagsString = tags.joinToString(",")
    val stylesString = supportedStyles.joinToString(",")
    return ModelConfigEntity(
        id = id,
        modelId = modelId,
        modelName = modelName,
        provider = provider,
        isEnabled = isEnabled,
        description = description,
        pipelineTag = pipelineTag,
        tags = tagsString,
        modelUrl = modelUrl,
        category = category,
        settingsUrl = settingsUrl,
        readmeUrl = readmeUrl,
        supportedStyles = stylesString,
        supportsVoiceCloning = supportsVoiceCloning,
        apiEndpoint = apiEndpoint,
        parametersJson = parametersJson,
        createdAt = createdAt
    )
}