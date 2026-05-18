package com.aiautocreate.domain.usecase.model

import com.aiautocreate.domain.repository.ISettingsRepository
import javax.inject.Inject

/**
 * حالة استخدام للتحقق من وجود مفاتيح API لمزودي النماذج (Gemini, HuggingFace).
 */
class CheckApiModelsUseCase @Inject constructor(
    private val settingsRepository: ISettingsRepository
) {
    /**
     * يتحقق من وجود أي مفتاح API مخزن.
     */
    suspend fun hasAnyApiKey(): Boolean {
        return settingsRepository.hasAnyApiKey()
    }

    /**
     * يتحقق من وجود مفتاح Gemini.
     */
    suspend fun hasGeminiKey(): Boolean {
        return settingsRepository.getApiKey("gemini").isStored
    }

    /**
     * يتحقق من وجود مفتاح HuggingFace.
     */
    suspend fun hasHuggingFaceKey(): Boolean {
        return settingsRepository.getApiKey("huggingface").isStored
    }

    /**
     * يجري فحصاً كاملاً ويعيد خريطة بنتائج المفاتيح.
     */
    suspend fun checkAll(): Map<String, Boolean> {
        return mapOf(
            "gemini" to hasGeminiKey(),
            "huggingface" to hasHuggingFaceKey()
        )
    }
}