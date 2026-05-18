package com.aiautocreate.domain.service

import com.aiautocreate.data.repository.AppSettingsRepository
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * يدير عمليات استنساخ الصوت:
 * - رفع عينة صوتية وربطها بنموذج TTS محدد
 * - التحقق من وجود العينة
 * - تطبيع الصوت (يمكن استدعاء Worker للخلفية)
 */
@Singleton
class VoiceCloneManager @Inject constructor(
    private val settingsRepo: AppSettingsRepository
) {

    /**
     * يحفظ مسار عينة الصوت لنموذج TTS معين.
     * @param ttsModelId معرف النموذج (مثال: "coqui/XTTS-v2")
     * @param samplePath المسار المطلق لملف الصوت (WAV, MP3)
     */
    suspend fun saveVoiceSample(ttsModelId: String, samplePath: String) {
        settingsRepo.setString("tts_voice_sample_${ttsModelId}", samplePath)
    }

    /**
     * يرجع مسار عينة الصوت لنموذج TTS معين، أو null إذا لم تكن موجودة.
     */
    suspend fun getVoiceSample(ttsModelId: String): String? {
        return settingsRepo.getStringOnce("tts_voice_sample_${ttsModelId}", "").takeIf { it.isNotBlank() }
    }

    /**
     * حذف عينة الصوت لنموذج TTS معين.
     */
    suspend fun deleteVoiceSample(ttsModelId: String) {
        settingsRepo.setString("tts_voice_sample_${ttsModelId}", "")
    }

    /**
     * تفعيل/تعطيل استنساخ الصوت لنموذج TTS معين.
     */
    suspend fun setVoiceCloneEnabled(ttsModelId: String, enabled: Boolean) {
        settingsRepo.setBoolean("tts_use_clone_${ttsModelId}", enabled)
    }

    /**
     * التحقق مما إذا كان استنساخ الصوت مفعلاً للنموذج.
     */
    suspend fun isVoiceCloneEnabled(ttsModelId: String): Boolean {
        return settingsRepo.getBoolOnce("tts_use_clone_${ttsModelId}", false)
    }

    /**
     * تطبيع عينة الصوت (يمكن استدعاء Worker لمعالجة الخلفية).
     * هنا نقوم فقط بنسخ الملف إلى موقع معياري أو تطبيق بسيط.
     * @param originalPath المسار الأصلي للعينة
     * @return المسار الجديد بعد المعالجة (أو null إذا فشل)
     */
    suspend fun normalizeVoiceSample(originalPath: String): String? {
        return try {
            val inputFile = File(originalPath)
            if (!inputFile.exists()) return null

            val outputDir = File(inputFile.parentFile, "normalized")
            if (!outputDir.exists()) outputDir.mkdirs()
            val outputFile = File(outputDir, "${inputFile.nameWithoutExtension}_normalized.wav")

            // هنا يمكن استخدام FFmpeg لتحويل الصوت إلى WAV بمعدل 16kHz (مثلاً)
            // لكن حالياً نقوم فقط بنسخ الملف (للتوسع لاحقاً)
            inputFile.copyTo(outputFile, overwrite = true)
            outputFile.absolutePath
        } catch (e: Exception) {
            null
        }
    }
}