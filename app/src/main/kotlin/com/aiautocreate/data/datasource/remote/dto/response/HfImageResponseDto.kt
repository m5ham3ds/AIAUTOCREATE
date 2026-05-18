package com.aiautocreate.data.datasource.remote.dto.response

import kotlinx.serialization.Serializable

/**
 * HuggingFace يرجع مباشرة صورة ثنائية (byte array) أو JSON.
 * نستخدم نوعاً مماثلاً لـ ResponseBody أو ByteArray.
 * هنا نعرّف غلافاً بسيطاً للتعامل مع الاستجابة النصية إن وجدت.
 */
@Serializable
data class HfImageResponseDto(
    val generatedImage: ByteArray? = null,
    val error: String? = null
)