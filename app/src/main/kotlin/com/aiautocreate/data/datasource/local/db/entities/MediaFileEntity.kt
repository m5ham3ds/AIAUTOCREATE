package com.aiautocreate.data.datasource.local.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * يمثل ملف وسائط (صورة، صوت، فيديو) مرتبط بمشروع.
 */
@Serializable
@Entity(tableName = "media_files")
data class MediaFileEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "project_id")
    val projectId: Long,

    @ColumnInfo(name = "file_type")
    val fileType: String,               // "image", "audio", "video", "subtitle"

    @ColumnInfo(name = "file_path")
    val filePath: String,               // مسار الملف المحلي

    @ColumnInfo(name = "original_name")
    val originalName: String = "",

    @ColumnInfo(name = "mime_type")
    val mimeType: String? = null,

    @ColumnInfo(name = "size_bytes")
    val sizeBytes: Long = 0,

    @ColumnInfo(name = "duration_ms")
    val durationMs: Long? = null,       // للصوت والفيديو

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)