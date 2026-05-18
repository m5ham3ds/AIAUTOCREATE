package com.aiautocreate.data.datasource.remote.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HfTtsRequestDto(
    @SerialName("inputs")
    val inputs: String,                 // النص المراد تحويله إلى كلام
    @SerialName("parameters")
    val parameters: HfTtsParameters? = null
)

@Serializable
data class HfTtsParameters(
    @SerialName("language")
    val language: String = "ar",
    @SerialName("speaker")
    val speaker: String? = null,
    @SerialName("speed")
    val speed: Float = 1.0f,
    // ✅ حقل استنساخ الصوت: expects base64-encoded audio (e.g., speaker_wav)
    @SerialName("speaker_wav")
    val speakerWav: String? = null,
    // بديل: speaker_embedding (أرقام عائمة)
    @SerialName("speaker_embedding")
    val speakerEmbedding: List<Float>? = null
)