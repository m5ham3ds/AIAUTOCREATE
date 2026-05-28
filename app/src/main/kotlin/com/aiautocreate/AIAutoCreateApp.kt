package com.aiautocreate

import android.app.Application
import androidx.work.*
import com.aiautocreate.data.datasource.local.secure.ApiKeyMigration
import com.aiautocreate.domain.usecase.model.DefaultModelsInitializer
import com.aiautocreate.worker.BackgroundStyleRefresher
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * ✅ FIXED: I/O operations (DataStore migrations) now run on Dispatchers.IO
 * instead of the main thread. Uses application-scoped CoroutineScope with
 * SupervisorJob for proper lifecycle management.
 */
@HiltAndroidApp
class AIAutoCreateApp : Application() {

    @Inject
    lateinit var apiKeyMigration: ApiKeyMigration

    @Inject
    lateinit var defaultModelsInitializer: DefaultModelsInitializer

    /**
     * ✅ FIXED: Properly managed CoroutineScope for application-level coroutines.
     * SupervisorJob ensures child coroutine failures don't cancel siblings.
     */
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        instance = this

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        // ✅ FIXED: Run DataStore I/O operations on IO dispatcher
        // Previously ran on main thread which could cause ANR on first launch
        applicationScope.launch {
            // ترحيل مفاتيح API إلى التخزين الآمن
            apiKeyMigration.migrateIfNeeded()

            // تهيئة النماذج الافتراضية (تضاف مرة واحدة فقط عند أول تشغيل)
            defaultModelsInitializer.initializeIfNeeded()
        }

        // ✅ جدولة تحديث الأنماط من README في الخلفية
        scheduleStyleRefresher()
    }

    private fun scheduleStyleRefresher() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val refreshWork = PeriodicWorkRequestBuilder<BackgroundStyleRefresher>(
            repeatInterval = 1, TimeUnit.DAYS
        ).setConstraints(constraints)
            .setInitialDelay(2, TimeUnit.HOURS) // تأخير 2 ساعة بعد التشغيل الأول
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "refresh_model_styles",
            ExistingPeriodicWorkPolicy.KEEP, // لا تحل محل العمل الموجود إن وجد
            refreshWork
        )
        Timber.d("تم جدولة تحديث الأنماط بشكل دوري (كل 24 ساعة)")
    }

    companion object {
        /**
         * ⚠️ DEPRECATED: Use proper Dependency Injection instead.
         * This is kept for backward compatibility but new code should
         * inject @ApplicationContext via Hilt.
         */
        @Deprecated("Use @ApplicationContext injection via Hilt instead")
        lateinit var instance: AIAutoCreateApp
            private set
    }
}
