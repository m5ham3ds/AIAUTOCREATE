package com.aiautocreate.data.datasource.remote.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class HfTtsResponseDto(
    val audio: ByteArray? = null,
    val error: String? = null
)