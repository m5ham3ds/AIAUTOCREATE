package com.aiautocreate

import android.app.Application
import androidx.work.*
import com.aiautocreate.data.datasource.local.secure.ApiKeyMigration
import com.aiautocreate.domain.usecase.model.DefaultModelsInitializer
import com.aiautocreate.worker.BackgroundStyleRefresher
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class AIAutoCreateApp : Application() {

    @Inject
    lateinit var apiKeyMigration: ApiKeyMigration

    @Inject
    lateinit var defaultModelsInitializer: DefaultModelsInitializer

    override fun onCreate() {
        super.onCreate()
        instance = this

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        // ترحيل مفاتيح API إلى التخزين الآمن
        apiKeyMigration.migrateIfNeeded()

        // تهيئة النماذج الافتراضية (تضاف مرة واحدة فقط عند أول تشغيل)
        defaultModelsInitializer.initializeIfNeeded()

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
        lateinit var instance: AIAutoCreateApp
            private set
    }
}