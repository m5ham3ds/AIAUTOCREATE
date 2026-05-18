package com.aiautocreate.data.datasource.local.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * يمثل إعداداً مسبقاً (Preset) لتنسيق الترجمة.
 */
@Serializable
@Entity(tableName = "subtitle_presets")
data class SubtitlePresetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "name")
    val name: String,                     // "افتراضي", "تصميم مخصص"

    @ColumnInfo(name = "font_name")
    val fontName: String = "default",

    @ColumnInfo(name = "font_size")
    val fontSize: Float = 16f,

    @ColumnInfo(name = "text_color_hex")
    val textColorHex: String = "#FFFFFF",

    @ColumnInfo(name = "background_color_hex")
    val backgroundColorHex: String = "#00000000",

    @ColumnInfo(name = "position")
    val position: String = "bottom",      // top, center, bottom

    @ColumnInfo(name = "is_default")
    val isDefault: Boolean = false,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)