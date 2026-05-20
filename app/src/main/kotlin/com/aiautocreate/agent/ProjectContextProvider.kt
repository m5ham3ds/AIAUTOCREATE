package com.aiautocreate.agent

import com.aiautocreate.data.repository.ActivityLogRepository
import com.aiautocreate.data.repository.AppSettingsRepository
import com.aiautocreate.domain.repository.IModelsRepository
import com.aiautocreate.domain.repository.IProjectRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProjectContextProvider @Inject constructor(
    private val projectRepo: IProjectRepository,
    private val modelsRepo: IModelsRepository,
    private val activityLogRepo: ActivityLogRepository,
    private val settingsRepo: AppSettingsRepository
) {

    /**
     * جلب السياق الكامل للتطبيق (للوكيل)
     */
    suspend fun getFullContext(): String {
        val projects = projectRepo.getAllProjects().first()
        val enabledModels = modelsRepo.getEnabledModels().first()
        val recentLogs = activityLogRepo.getAllLogs().first().take(10)
        val selectedModels = mapOf(
            "image" to settingsRepo.getSelectedModelForCategory("image"),
            "video" to settingsRepo.getSelectedModelForCategory("video"),
            "tts" to settingsRepo.getSelectedModelForCategory("tts"),
            "master" to settingsRepo.getSelectedModelForCategory("master"),
            "reviewer" to settingsRepo.getSelectedModelForCategory("reviewer"),
            "orchestrator" to settingsRepo.getSelectedModelForCategory("orchestrator")
        ).filterValues { it.isNotBlank() }

        return buildString {
            appendLine("=== معلومات المشروع ===")
            appendLine("إجمالي المشاريع: ${projects.size}")
            if (projects.isNotEmpty()) {
                appendLine("أحدث المشاريع:")
                projects.take(5).forEach { proj ->
                    appendLine("- ${proj.title} (الحالة: ${proj.status}, آخر تحديث: ${proj.updatedAt})")
                }
            }

            appendLine("\n=== النماذج المفعلة ===")
            appendLine("عدد النماذج النشطة: ${enabledModels.size}")
            appendLine("النماذج المختارة حالياً:")
            selectedModels.forEach { (cat, modelId) ->
                val model = enabledModels.find { it.modelId == modelId }
                if (model != null) {
                    appendLine("- $cat → ${model.modelName} (${model.modelId})")
                } else {
                    appendLine("- $cat → $modelId (⚠️ غير موجود في القائمة النشطة)")
                }
            }

            appendLine("\n=== آخر النشاطات والأخطاء ===")
            val errors = recentLogs.filter { !it.isSuccess }
            val successes = recentLogs.filter { it.isSuccess }
            appendLine("✅ نجاح: ${successes.size} | ❌ فشل/خطأ: ${errors.size}")
            if (errors.isNotEmpty()) {
                appendLine("أحدث الأخطاء:")
                errors.take(5).forEach { log ->
                    appendLine("✗ [${log.type}] ${log.title}: ${log.description.take(100)}")
                }
            } else {
                appendLine("لا توجد أخطاء حديثة. كل شيء يعمل بشكل جيد.")
            }
        }
    }

    /**
     * إحصائيات موجزة لعرضها في تبويب "الإحصائيات"
     */
    suspend fun getStats(): AgentStats {
        val projects = projectRepo.getAllProjects().first()
        val models = modelsRepo.getAllModelConfigs().first()
        val logs = activityLogRepo.getAllLogs().first()
        val errors = logs.filter { !it.isSuccess }
        val lastRun = logs.maxByOrNull { it.timestamp }

        return AgentStats(
            projectCount = projects.size,
            activeModelCount = models.count { it.isEnabled },
            totalModelCount = models.size,
            totalLogs = logs.size,
            errorCount = errors.size,
            successCount = logs.size - errors.size,
            lastActivityTimestamp = lastRun?.timestamp ?: 0,
            lastActivityTitle = lastRun?.title ?: "لا يوجد نشاط"
        )
    }

    /**
     * جلب العمليات الجارية (سيتم توسيعها لاحقاً عند ربط PipelineOrchestrator)
     */
    suspend fun getRunningOperations(): List<String> {
        // TODO: ربط PipelineOrchestrator للحصول على العمليات الجارية
        return emptyList()
    }
}

data class AgentStats(
    val projectCount: Int,
    val activeModelCount: Int,
    val totalModelCount: Int,
    val totalLogs: Int,
    val errorCount: Int,
    val successCount: Int,
    val lastActivityTimestamp: Long,
    val lastActivityTitle: String
)
