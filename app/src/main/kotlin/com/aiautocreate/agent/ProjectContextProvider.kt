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

    // سياق كامل (للتحليل الشامل)
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
            appendLine("عدد المشاريع: ${projects.size}")
            if (projects.isNotEmpty()) {
                appendLine("أحدث المشاريع:")
                projects.take(3).forEach { proj ->
                    appendLine("- ${proj.title} (الحالة: ${proj.status})")
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
                    appendLine("- $cat → $modelId (⚠️ غير موجود)")
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
                appendLine("لا توجد أخطاء حديثة.")
            }
        }
    }

    // سياق سريع (للردود العادية والفحص السريع)
    suspend fun getQuickContext(): String {
        val projects = projectRepo.getAllProjects().first()
        val enabledModels = modelsRepo.getEnabledModels().first()
        val recentErrors = activityLogRepo.getAllLogs().first()
            .filter { !it.isSuccess }.take(2)
        return buildString {
            appendLine("المشاريع: ${projects.size}")
            appendLine("النماذج النشطة: ${enabledModels.size}")
            if (recentErrors.isNotEmpty()) {
                appendLine("آخر خطأين: ${recentErrors.joinToString { it.title }}")
            } else {
                appendLine("لا توجد أخطاء حديثة.")
            }
        }
    }

    // سياق الأخطاء فقط (لفحص الأخطاء الخطيرة)
    suspend fun getErrorContext(): String {
        val errors = activityLogRepo.getAllLogs().first()
            .filter { !it.isSuccess }.take(10)
        return if (errors.isEmpty()) "لا توجد أخطاء مسجلة."
        else buildString {
            appendLine("قائمة الأخطاء (آخر 10):")
            errors.forEach { error ->
                appendLine("- [${error.type}] ${error.title}: ${error.description.take(100)}")
            }
        }
    }

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
