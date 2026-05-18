package com.aiautocreate.domain.usecase.model

import com.aiautocreate.data.repository.AppSettingsRepository
import com.aiautocreate.domain.repository.IModelsRepository
import com.aiautocreate.util.ModelStyleParser
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers

class RefreshModelsStylesUseCase @Inject constructor(
    private val modelsRepository: IModelsRepository,
    private val settingsRepo: AppSettingsRepository,
    private val okHttpClient: OkHttpClient
) {

    /**
     * يقوم بتحديث حقل `supportedStyles` لكل نموذج لديه `readmeUrl` غير فارغ.
     * يعيد عدد النماذج التي تم تحديثها.
     */
    suspend fun refreshAll(): Int = withContext(Dispatchers.IO) {
        val allModels = modelsRepository.getAllModelConfigs().first()
        val modelsWithReadme = allModels.filter { it.readmeUrl.isNotBlank() }
        var updatedCount = 0

        for (model in modelsWithReadme) {
            try {
                val readmeText = downloadReadme(model.readmeUrl)
                if (readmeText.isNotBlank()) {
                    val styles = ModelStyleParser.extractStyles(readmeText)
                    if (styles.isNotEmpty()) {
                        val updatedModel = model.copy(supportedStyles = styles)
                        modelsRepository.updateModelConfig(updatedModel)
                        updatedCount++
                    }
                }
            } catch (e: Exception) {
                // سجل الخطأ وتابع
                e.printStackTrace()
            }
        }
        updatedCount
    }

    private suspend fun downloadReadme(url: String): String = withContext(Dispatchers.IO) {
        val request = Request.Builder().url(url).build()
        okHttpClient.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
                response.body?.string() ?: ""
            } else ""
        }
    }
}