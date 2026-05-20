package com.aiautocreate.domain.usecase.model

import com.aiautocreate.domain.repository.ISettingsRepository
import javax.inject.Inject

class CheckApiModelsUseCase @Inject constructor(
    private val settingsRepository: ISettingsRepository
) {
    suspend fun hasAnyApiKey(): Boolean = settingsRepository.hasAnyApiKey()

    suspend fun hasGeminiKey(): Boolean = settingsRepository.getGeminiKey() != null
    suspend fun hasHuggingFaceKey(): Boolean = settingsRepository.getHuggingFaceToken() != null
    suspend fun hasElevenLabsKey(): Boolean = settingsRepository.getElevenLabsKey() != null

    suspend fun checkAll(): Map<String, Boolean> {
        return mapOf(
            "gemini" to hasGeminiKey(),
            "huggingface" to hasHuggingFaceKey(),
            "elevenlabs" to hasElevenLabsKey()
        )
    }
}
