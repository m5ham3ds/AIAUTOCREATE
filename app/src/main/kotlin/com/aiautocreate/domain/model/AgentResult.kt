package com.aiautocreate.domain.model

/**
 * نتيجة تشغيل عامل ذكي (Agent).
 */
data class AgentResult(
    val success: Boolean,
    val output: String? = null,
    val errorMessage: String? = null,
    val durationMs: Long = 0,
    val data: Any? = null   // ✅ تمت الإضافة
)
