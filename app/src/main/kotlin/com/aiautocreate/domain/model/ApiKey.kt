package com.aiautocreate.domain.model

/**
 * نموذج يمثل مفتاح API مخزّن بأمان.
 */
data class ApiKey(
    val provider: String,
    val keyValue: String? = null,
    val isStored: Boolean = false
)