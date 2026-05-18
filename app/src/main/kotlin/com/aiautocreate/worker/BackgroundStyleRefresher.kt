package com.aiautocreate.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.aiautocreate.domain.usecase.model.RefreshModelsStylesUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class BackgroundStyleRefresher @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val refreshUseCase: RefreshModelsStylesUseCase
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val count = refreshUseCase.refreshAll()
            if (count > 0) Result.success()
            else Result.success() // لا يوجد خطأ
        } catch (e: Exception) {
            Result.retry()
        }
    }
}