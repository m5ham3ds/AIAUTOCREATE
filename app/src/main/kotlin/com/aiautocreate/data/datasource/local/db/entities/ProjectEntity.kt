package com.aiautocreate.data.datasource.local.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * يمثل مشروع إنشاء فيديو بالذكاء الاصطناعي.
 */
@Serializable
@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "description")
    val description: String = "",

    @ColumnInfo(name = "script_text")
    val scriptText: String = "",

    @ColumnInfo(name = "status")
    val status: String = "draft", // draft, generating, completed, failed

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "output_video_path")
    val outputVideoPath: String? = null,

    @ColumnInfo(name = "thumbnail_path")
    val thumbnailPath: String? = null
)