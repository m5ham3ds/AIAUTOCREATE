package com.aiautocreate.domain.model

/**
 * نموذج المشروع في طبقة Domain.
 */
data class Project(
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val scriptText: String = "",
    val status: String = "draft",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val outputVideoPath: String? = null,
    val thumbnailPath: String? = null
)