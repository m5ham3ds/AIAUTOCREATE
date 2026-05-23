package com.aiautocreate.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.aiautocreate.domain.usecase.model.RefreshModelsStylesUseCase
import com.aiautocreate.util.NetworkUtils
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.delay
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * عامل خلفية لتحديث قوائم الأنماط والنماذج من HuggingFace و GitHub.
 * يتم تشغيله عادة عند الضغط على زر "تحديث القوائم" في واجهة إعدادات النماذج.
 * 
 * التحسينات:
 * - إعادة محاولة ذكية مع استراتيجية backoff.
 * - التحقق من وجود اتصال بالإنترنت قبل البدء.
 * - تسجيل مفصل (Timber) لسهولة التتبع.
 * - إمكانية إضافة نتائج (Result.success مع outputData) لتمرير عدد النماذج المحدثة.
 * - التعامل مع الاستثناءات وإرجاع Result.retry أو failure حسب الحالة.
 * - دعم إلغاء (cancellation) بشكل جيد.
 */
@HiltWorker
class BackgroundStyleRefresher @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val refreshUseCase: RefreshModelsStylesUseCase
) : CoroutineWorker(context, params) {

    companion object {
        // مفتاح لتمرير عدد النماذج المحدثة في outputData
        const val KEY_UPDATED_MODELS_COUNT = "updated_models_count"
        
        /**
         * إنشاء طلب عمل مع قيود الشبكة واستراتيجية إعادة المحاولة
         * باستخدام TimeUnit للتوافق مع جميع إصدارات أندرويد
         */
        fun createWorkRequest(): OneTimeWorkRequest {
            return OneTimeWorkRequestBuilder<BackgroundStyleRefresher>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    10, // 10 ثوانٍ
                    TimeUnit.SECONDS
                )
                .addTag("refresh_models")
                .build()
        }
    }

    override suspend fun doWork(): Result {
        // التحقق من الإلغاء مبكراً
        if (isStopped) {
            Timber.w("BackgroundStyleRefresher: تم إلغاء العمل قبل البدء")
            return Result.failure()
        }

        // ✅ التحقق من وجود اتصال بالإنترنت (إضافي فوق قيد الشبكة)
        if (!NetworkUtils.isOnline(applicationContext)) {
            Timber.e("BackgroundStyleRefresher: لا يوجد اتصال بالإنترنت، سيتم إعادة المحاولة لاحقاً")
            return Result.retry()
        }

        Timber.d("BackgroundStyleRefresher: بدء تحديث الأنماط والنماذج...")

        return try {
            // ✅ تنفيذ عملية التحديث الفعلية
            val updatedCount = refreshUseCase.refreshAll()
            
            // ✅ التحقق من الإلغاء بعد العملية
            if (isStopped) {
                Timber.w("BackgroundStyleRefresher: تم إلغاء العمل بعد التحديث")
                return Result.failure()
            }

            Timber.d("BackgroundStyleRefresher: اكتمل التحديث بنجاح. عدد النماذج/الأنماط المحدثة: $updatedCount")
            
            // ✅ تمرير النتيجة (عدد النماذج المحدثة) للاستخدام في التطبيق
            val outputData = workDataOf(KEY_UPDATED_MODELS_COUNT to updatedCount)
            Result.success(outputData)
        } catch (e: Exception) {
            Timber.e(e, "BackgroundStyleRefresher: فشل تحديث الأنماط والنماذج")
            
            // ✅ إعادة المحاولة فقط للأخطاء الشبكية المؤقتة
            if (e.message?.contains("network", ignoreCase = true) == true ||
                e.message?.contains("timeout", ignoreCase = true) == true ||
                e.message?.contains("unreachable", ignoreCase = true) == true
            ) {
                Result.retry()
            } else {
                // ❌ أخطاء دائمة (مثلاً خطأ في التوكين) لا تستحق إعادة المحاولة المتكررة
                Result.failure()
            }
        }
    }
}
