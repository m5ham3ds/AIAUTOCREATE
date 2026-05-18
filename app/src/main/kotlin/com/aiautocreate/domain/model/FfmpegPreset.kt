package com.aiautocreate.domain.model

/**
 * إعداد مسبق لعمليات FFmpeg (ضغط، دمج، تحويل).
 */
data class FfmpegPreset(
    val id: Long = 0,
    val name: String,
    val command: String,
    val description: String = ""
)