package com.aiautocreate.data.repository

import com.aiautocreate.data.datasource.local.db.dao.SubtitlePresetDao
import com.aiautocreate.data.datasource.local.db.entities.SubtitlePresetEntity
import com.aiautocreate.domain.model.SubtitleStyle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubtitlePresetRepository @Inject constructor(
    private val presetDao: SubtitlePresetDao
) {
    fun getAllPresets(): Flow<List<SubtitleStyle>> =
        presetDao.getAllPresets().map { list -> list.map { it.toDomain() } }

    suspend fun insertPreset(style: SubtitleStyle) {
        presetDao.insertPreset(style.toEntity())
    }

    private fun SubtitlePresetEntity.toDomain() = SubtitleStyle(
        id = id, name = name, fontName = fontName, fontSize = fontSize,
        textColorHex = textColorHex, backgroundColorHex = backgroundColorHex,
        position = position, isDefault = isDefault
    )

    private fun SubtitleStyle.toEntity() = SubtitlePresetEntity(
        id = id, name = name, fontName = fontName, fontSize = fontSize,
        textColorHex = textColorHex, backgroundColorHex = backgroundColorHex,
        position = position, isDefault = isDefault
    )
}