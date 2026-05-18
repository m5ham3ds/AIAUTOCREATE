package com.aiautocreate.data.datasource.remote.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GeminiResponseDto(
    @SerialName("candidates")
    val candidates: List<Candidate>? = null,
    @SerialName("promptFeedback")
    val promptFeedback: PromptFeedback? = null
)

@Serializable
data class Candidate(
    @SerialName("content")
    val content: Content? = null,
    @SerialName("finishReason")
    val finishReason: String? = null,
    @SerialName("safetyRatings")
    val safetyRatings: List<SafetyRating>? = null
)

@Serializable
data class Content(
    @SerialName("parts")
    val parts: List<Part>? = null,
    @SerialName("role")
    val role: String? = null
)

@Serializable
data class Part(
    @SerialName("text")
    val text: String? = null
)

@Serializable
data class SafetyRating(
    @SerialName("category")
    val category: String? = null,
    @SerialName("probability")
    val probability: String? = null
)

@Serializable
data class PromptFeedback(
    @SerialName("safetyRatings")
    val safetyRatings: List<SafetyRating>? = null
)