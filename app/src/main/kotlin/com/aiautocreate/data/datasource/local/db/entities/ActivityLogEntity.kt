package com.aiautocreate.data.datasource.local.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * يمثل سجل نشاط (توليد فيديو، خطأ، إلخ) لعرضه في شاشة سجل النشاطات.
 */
@Serializable
@Entity(tableName = "activity_logs")
data class ActivityLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "type")
    val type: String,                   // "video_generation", "error", "model_test", "sync"

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "description")
    val description: String = "",

    @ColumnInfo(name = "project_id")
    val projectId: Long? = null,        // اختياري: مرتبط بمشروع

    @ColumnInfo(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "is_success")
    val isSuccess: Boolean = true
)