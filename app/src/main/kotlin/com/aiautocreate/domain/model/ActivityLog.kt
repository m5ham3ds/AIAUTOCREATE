package com.aiautocreate.domain.model

data class ActivityLog(
    val id: Long = 0,
    val type: String,
    val title: String,
    val description: String = "",
    val projectId: Long? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val isSuccess: Boolean = true
)