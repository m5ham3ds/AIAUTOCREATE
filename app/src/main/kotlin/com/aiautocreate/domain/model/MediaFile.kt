package com.aiautocreate.domain.model

/**
 * يمثل ملف وسائط (صورة، صوت، فيديو) مرتبط بمشروع.
 */
data class MediaFile(
    val id: Long = 0,
    val projectId: Long,
    val fileType: String,             // "image", "audio", "video", "subtitle"
    val filePath: String,
    val originalName: String = "",
    val mimeType: String? = null,
    val sizeBytes: Long = 0,
    val durationMs: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)