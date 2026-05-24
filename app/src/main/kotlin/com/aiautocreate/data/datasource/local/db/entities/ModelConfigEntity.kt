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
    val modelId: String,

    @ColumnInfo(name = "model_name")
    val modelName: String,

    @ColumnInfo(name = "provider")
    val provider: String,

    @ColumnInfo(name = "is_enabled")
    val isEnabled: Boolean = true,

    @ColumnInfo(name = "description")
    val description: String = "",

    @ColumnInfo(name = "pipeline_tag")
    val pipelineTag: String = "",

    @ColumnInfo(name = "tags")
    val tags: String = "",                  // مخزنة كنص CSV

    @ColumnInfo(name = "model_url")
    val modelUrl: String = "",

    @ColumnInfo(name = "category")
    val category: String = "",

    @ColumnInfo(name = "settings_url")
    val settingsUrl: String = "",

    @ColumnInfo(name = "readme_url")
    val readmeUrl: String = "",

    @ColumnInfo(name = "supported_styles")
    val supportedStyles: String = "",       // CSV

    @ColumnInfo(name = "supports_voice_cloning")
    val supportsVoiceCloning: Boolean = false,

    // ✅ حقل جديد: رابط GitHub README
    @ColumnInfo(name = "github_readme_url")
    val githubReadmeUrl: String? = null,

    @ColumnInfo(name = "api_endpoint")
    val apiEndpoint: String? = null,

    @ColumnInfo(name = "parameters_json")
    val parametersJson: String? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
