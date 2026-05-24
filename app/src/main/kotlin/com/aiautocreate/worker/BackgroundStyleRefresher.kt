package com.aiautocreate.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.aiautocreate.domain.usecase.model.RefreshModelsStylesUseCase
import com.aiautocreate.util.NetworkUtils
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber
import java.util.concurrent.TimeUnit

@HiltWorker
class BackgroundStyleRefresher @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val refreshUseCase: RefreshModelsStylesUseCase
) : CoroutineWorker(context, params) {

    companion object {
        const val KEY_UPDATED_MODELS_COUNT = "updated_models_count"

        fun createWorkRequest(): OneTimeWorkRequest {
            return OneTimeWorkRequestBuilder<BackgroundStyleRefresher>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    10,
                    TimeUnit.SECONDS
                )
                .addTag("refresh_models")
                .build()
        }
    }

    override suspend fun doWork(): Result {
        if (isStopped) {
            Timber.w("تم إلغاء العمل قبل البدء")
            return Result.failure()
        }

        if (!NetworkUtils.isOnline(applicationContext)) {
            Timber.e("لا يوجد اتصال بالإنترنت")
            return Result.retry()
        }

        Timber.d("بدء تحديث الأنماط والنماذج...")

        return try {
            val updatedCount = refreshUseCase.refreshAll()
            if (isStopped) return Result.failure()

            Timber.d("اكتمل التحديث. عدد النماذج المحدثة: $updatedCount")
            val outputData = workDataOf(KEY_UPDATED_MODELS_COUNT to updatedCount)
            Result.success(outputData)
        } catch (e: Exception) {
            Timber.e(e, "فشل التحديث")
            if (e.message?.contains("network") == true ||
                e.message?.contains("timeout") == true
            ) Result.retry() else Result.failure()
        }
    }
}
