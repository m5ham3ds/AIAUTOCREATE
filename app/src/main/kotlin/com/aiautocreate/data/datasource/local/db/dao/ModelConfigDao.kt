package com.aiautocreate.data.datasource.local.db.dao

import androidx.room.*
import com.aiautocreate.data.datasource.local.db.entities.ModelConfigEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ModelConfigDao {

    @Query("SELECT * FROM model_configs ORDER BY provider, model_name")
    fun getAllModelConfigs(): Flow<List<ModelConfigEntity>>

    @Query("SELECT * FROM model_configs WHERE is_enabled = 1")
    fun getEnabledModels(): Flow<List<ModelConfigEntity>>

    @Query("SELECT * FROM model_configs WHERE id = :id")
    suspend fun getModelConfigById(id: Long): ModelConfigEntity?

    @Query("SELECT * FROM model_configs WHERE provider = :provider")
    fun getModelsByProvider(provider: String): Flow<List<ModelConfigEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertModelConfig(config: ModelConfigEntity): Long

    @Update
    suspend fun updateModelConfig(config: ModelConfigEntity)

    @Delete
    suspend fun deleteModelConfig(config: ModelConfigEntity)

    @Query("UPDATE model_configs SET is_enabled = :enabled WHERE id = :id")
    suspend fun setModelEnabled(id: Long, enabled: Boolean)
}
