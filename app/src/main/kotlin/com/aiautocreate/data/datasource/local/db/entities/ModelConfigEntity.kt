package com.aiautocreate.data.datasource.local.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "model_configs")
data class ModelConfigEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "model_id")
    val modelId: String,                    // المعرف الفريد في HuggingFace

    @ColumnInfo(name = "model_name")
    val modelName: String,                  // اسم العرض

    @ColumnInfo(name = "provider")
    val provider: String,                   // "google", "huggingface"

    @ColumnInfo(name = "is_enabled")
    val isEnabled: Boolean = true,

    @ColumnInfo(name = "description")
    val description: String = "",           // وصف النموذج

    @ColumnInfo(name = "pipeline_tag")
    val pipelineTag: String = "",           // تصنيف النموذج (text-generation...)

    @ColumnInfo(name = "tags")
    val tags: String = "",                  // وسوم النموذج (مخزنة كنص CSV)

    @ColumnInfo(name = "model_url")
    val modelUrl: String = "",              // رابط النموذج على HuggingFace

    // ✅ الحقول الجديدة
    @ColumnInfo(name = "category")
    val category: String = "",              // فئة النموذج

    @ColumnInfo(name = "settings_url")
    val settingsUrl: String = "",           // رابط الإعدادات

    @ColumnInfo(name = "readme_url")
    val readmeUrl: String = "",             // رابط README

    @ColumnInfo(name = "supported_styles")
    val supportedStyles: String = "",       // الأنماط المدعومة (نص CSV)

    @ColumnInfo(name = "supports_voice_cloning")
    val supportsVoiceCloning: Boolean = false,

    @ColumnInfo(name = "api_endpoint")
    val apiEndpoint: String? = null,

    @ColumnInfo(name = "parameters_json")
    val parametersJson: String? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)