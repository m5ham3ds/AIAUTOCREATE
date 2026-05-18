package com.aiautocreate.data.datasource.remote.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GeminiRequestDto(
    @SerialName("contents")
    val contents: List<Content>,
    @SerialName("generationConfig")
    val generationConfig: GenerationConfig? = null
)

@Serializable
data class Content(
    @SerialName("parts")
    val parts: List<Part>,
    @SerialName("role")
    val role: String? = "user"
)

@Serializable
data class Part(
    @SerialName("text")
    val text: String
)

@Serializable
data class GenerationConfig(
    @SerialName("temperature")
    val temperature: Float = 0.7f,
    @SerialName("topK")
    val topK: Int = 40,
    @SerialName("topP")
    val topP: Float = 0.95f,
    @SerialName("maxOutputTokens")
    val maxOutputTokens: Int = 2048
)