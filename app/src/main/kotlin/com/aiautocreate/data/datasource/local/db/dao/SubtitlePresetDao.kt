package com.aiautocreate.data.datasource.local.db.dao

import androidx.room.*
import com.aiautocreate.data.datasource.local.db.entities.SubtitlePresetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SubtitlePresetDao {

    @Query("SELECT * FROM subtitle_presets ORDER BY created_at DESC")
    fun getAllPresets(): Flow<List<SubtitlePresetEntity>>

    @Query("SELECT * FROM subtitle_presets WHERE is_default = 1 LIMIT 1")
    suspend fun getDefaultPreset(): SubtitlePresetEntity?

    @Query("SELECT * FROM subtitle_presets WHERE id = :id")
    suspend fun getPresetById(id: Long): SubtitlePresetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreset(preset: SubtitlePresetEntity): Long

    @Update
    suspend fun updatePreset(preset: SubtitlePresetEntity)

    @Delete
    suspend fun deletePreset(preset: SubtitlePresetEntity)

    @Query("UPDATE subtitle_presets SET is_default = 0")
    suspend fun clearDefaultPresets()

    @Query("UPDATE subtitle_presets SET is_default = 1 WHERE id = :id")
    suspend fun setDefaultPreset(id: Long)
}