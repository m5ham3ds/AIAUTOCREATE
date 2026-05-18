package com.aiautocreate.data.datasource.remote.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HfImageRequestDto(
    @SerialName("inputs")
    val inputs: String,                  // النص الوصفي
    @SerialName("parameters")
    val parameters: HfImageParameters? = null
)

@Serializable
data class HfImageParameters(
    @SerialName("width")
    val width: Int = 512,
    @SerialName("height")
    val height: Int = 512,
    @SerialName("num_inference_steps")
    val numInferenceSteps: Int = 25,
    @SerialName("guidance_scale")
    val guidanceScale: Float = 7.5f
)