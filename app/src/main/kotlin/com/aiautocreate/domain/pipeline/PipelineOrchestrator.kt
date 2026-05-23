package com.aiautocreate.domain.pipeline

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import com.aiautocreate.agent.AgentInterventionHandler
import com.aiautocreate.agent.AgentOrchestrator
import com.aiautocreate.agent.Asset
import com.aiautocreate.data.asset.*
import com.aiautocreate.data.datasource.remote.api.GeminiApi
import com.aiautocreate.data.datasource.remote.api.HuggingFaceApi
import com.aiautocreate.data.datasource.remote.dto.request.*
import com.aiautocreate.data.repository.AppSettingsRepository
import com.aiautocreate.domain.model.MontagePlan
import com.aiautocreate.domain.service.AssetProvider
import com.aiautocreate.domain.service.FFmpegCommandBuilder
import com.aiautocreate.util.FFmpegRunner
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import okhttp3.OkHttpClient
import okhttp3.Request
import timber.log.Timber
import java.io.*
import javax.inject.Inject
import javax.inject.Singleton

data class PipelineConfig(
    val prompt: String,
    val imageStyle: String,
    val coverStyle: String,
    val voiceChoice: String,
    val videoStyle: String,
    val montageStyle: String,
    val minutes: String,
    val seconds: String,
    val aspect: String,
    val quality: String,
    val sdModel: String,
    val img2VidModel: String,
    val ttsModel: String,
    val masterModelId: String = "",
    val audioFxModelId: String = "",
    val visualFxModelId: String = "",
    val transitionsModelId: String = "",
    val subtitlesModelId: String = "",
    val musicModelId: String = "",
    val reviewerModelId: String = "",
    val orchestratorModelId: String = "",
    val audioFx: String? = null,
    val visualFx: String? = null,
    val transFx: String? = null,
    val smartSel: String? = null,
    val subsModel: String? = null,
    val musicBg: String? = null,
    val masterAgg: String? = null,
    val reviewer: String? = null,
    val orch: String? = null,
    val selectedFps: String = "30"
)

sealed class PipelineEvent {
    data class Progress(val stage: String, val message: String, val percent: Int) : PipelineEvent()
    data class Log(val message: String) : PipelineEvent()
    data class Error(val stage: String, val message: String) : PipelineEvent()
    data class FinalResult(val outputFile: String) : PipelineEvent()
}

data class SmartCountResult(
    val audioFxCount: Int,
    val visualFxCount: Int,
    val transitionsCount: Int
)

data class OrchestrationPlan(
    val enableMaster: Boolean,
    val enableAudioFx: Boolean,
    val enableVisualFx: Boolean,
    val enableTransitions: Boolean,
    val enableSmartCount: Boolean,
    val enableSubtitles: Boolean,
    val enableMusic: Boolean,
    val enableReviewer: Boolean,
    val enableExternalVideo: Boolean,
    val enableExternalImage: Boolean
)

@Singleton
class PipelineOrchestrator @Inject constructor(
    private val geminiApi: GeminiApi,
    private val huggingFaceApi: HuggingFaceApi,
    private val settingsRepo: AppSettingsRepository,
    private val commandBuilder: FFmpegCommandBuilder,
    private val agentOrchestrator: AgentOrchestrator,
    private val interventionHandler: AgentInterventionHandler,
    private val okHttpClient: OkHttpClient,
    private val pexelsProvider: PexelsAssetProvider,
    private val pixabayProvider: PixabayAssetProvider,
    private val lotsOfSoundsProvider: LotsOfSoundsAssetProvider,
    private val freesoundProvider: FreesoundAssetProvider,
    private val openVfxProvider: OpenVFXAssetProvider,
    private val localAssetProvider: LocalAssetProvider,
    @com.aiautocreate.di.Dispatcher(com.aiautocreate.di.DispatcherType.IO)
    private val ioDispatcher: CoroutineDispatcher
) {
    private val assetProviders: Set<AssetProvider> = setOf(
        localAssetProvider,
        pexelsProvider,
        pixabayProvider,
        lotsOfSoundsProvider,
        freesoundProvider,
        openVfxProvider
    )

    private val _events = MutableSharedFlow<PipelineEvent>()
    val events: SharedFlow<PipelineEvent> = _events

    @Volatile
    private var cancelled = false
    @Volatile
    private var hfQuotaExceeded = false
    private var hfQuotaModel = ""
    private var hfQuotaStage = ""

    companion object {
        private const val DEFAULT_NEGATIVE = "lowres, blurry, bad anatomy, deformed, watermark, text, jpeg artifacts, worst quality, low quality, noisy"
        private const val VOICE_CLONE_OPTION = "استنساخ العينة (من الإعدادات)"
    }

    // ----------------------------------------------
    // 1. دالة التشغيل الرئيسية (معدلة)
    // ----------------------------------------------
    suspend fun execute(config: PipelineConfig) {
        cancelled = false
        hfQuotaExceeded = false
        hfQuotaModel = ""
        hfQuotaStage = ""

        val projectId = System.currentTimeMillis()
        val projectTempDir = ensureDir(settingsRepo.getProjectTempDir(projectId))
        val scriptsDir = ensureDir("$projectTempDir/SCRIPT")
        val imagesDir = ensureDir("$projectTempDir/IMAGES")
        val audiosDir = ensureDir("$projectTempDir/AUDIOS")
        val videosDir = ensureDir("$projectTempDir/VIDEOS")

        // ✅ حساب الأبعاد الهدف والمدة من إعدادات FFmpeg
        val targetSize = computeTargetSize(config.aspect, config.quality)
        val totalDurationSec = (config.minutes.toIntOrNull() ?: 1) * 60 + (config.seconds.toIntOrNull() ?: 30)

        try {
            emitLog("بدء معالجة الطلب: ${config.prompt}")
            emitLog("📐 الأبعاد المطلوبة: ${targetSize.first}x${targetSize.second}")
            emitLog("⏱️ المدة المطلوبة: ${totalDurationSec / 60} دقيقة و ${totalDurationSec % 60} ثانية")
            emitProgress("orchestrator", "تنسيق الخطة...", 1)

            // ✅ 1. المنسق الرئيسي (Orchestrator)
            val orchestrationPlan = orchestratePlan(config)

            emitProgress("script", "توليد السيناريو...", 5)
            // ✅ 2. توليد السيناريو مع المدة والأبعاد
            val scriptText = generateScript(config, totalDurationSec, targetSize)
            emitProgress("script", "تم إنشاء السيناريو", 15)

            if (checkCancel()) return
            saveScriptAndExtract(scriptText, scriptsDir)

            // ✅ 3. العدد الذكي (Smart Count)
            val audioFile = File(audiosDir, "SCRIPTS_SSML_AU.wav")
            val actualAudioDurationMs = if (audioFile.exists()) getAudioDuration(audioFile) else 0L
            val smartCount = if (orchestrationPlan.enableSmartCount) {
                smartCountAnalysis(config, scriptText, actualAudioDurationMs)
            } else SmartCountResult(3, 2, 2)

            // ✅ 4. المهام الفرعية
            if (!hfQuotaExceeded) processImages(config, imagesDir, scriptsDir, smartCount.visualFxCount, targetSize)
            if (!hfQuotaExceeded) processTts(config, audiosDir, scriptsDir)
            if (!hfQuotaExceeded) processVideo(config, videosDir, imagesDir, scriptsDir, smartCount.transitionsCount)

            emitProgress("video", "تجميع الفيديو النهائي...", 80)
            val outputFile = assembleWithMontagePlan(
                config, scriptsDir, imagesDir, audiosDir, videosDir,
                projectTempDir, projectId, smartCount, orchestrationPlan, targetSize
            )
            emitProgress("video", "اكتمل الفيديو", 100)

            // ✅ 5. المراجع (Reviewer)
            if (orchestrationPlan.enableReviewer) {
                val review = reviewFinalVideo(outputFile, config)
                if (review != null) emitLog("📝 مراجعة الوكيل: $review")
            }

            emitFinalResult(outputFile)
            cleanupTempFiles(projectTempDir)
        } catch (e: Exception) {
            if (!cancelled) {
                Timber.e(e, "Pipeline failed")
                emitError("pipeline", e.localizedMessage ?: "خطأ غير معروف")
                saveErrorLog(projectTempDir, e)
            }
        }
    }

    fun cancel() { cancelled = true }
    private suspend fun checkCancel(): Boolean = cancelled

    // ==================== دوال جديدة ====================

    // ✅ توليد السيناريو مع المدة والأبعاد
    private suspend fun generateScript(config: PipelineConfig, totalDurationSec: Int, targetSize: Pair<Int, Int>): String {
        try {
            val prompt = buildGeminiPrompt(config, totalDurationSec, targetSize)
            val request = GeminiRequestDto(contents = listOf(Content(parts = listOf(Part(text = prompt)))))
            val response = geminiApi.generateContentWithKey(request)
            if (response.isSuccessful) {
                val text = response.body()?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
                emitLog("السيناريو المستلم: ${text.take(100)}...")
                return text
            } else {
                emitLog("فشل Gemini مع الكود ${response.code()}، استخدام prompt الأصلي")
                return config.prompt
            }
        } catch (e: Exception) {
            emitLog("استثناء في Gemini: ${e.message}، استخدام prompt الأصلي")
            return config.prompt
        }
    }

    private fun buildGeminiPrompt(config: PipelineConfig, totalDurationSec: Int, targetSize: Pair<Int, Int>): String {
        val durationDesc = "${totalDurationSec / 60} دقيقة و ${totalDurationSec % 60} ثانية"
        return """
المطلوب: ${config.prompt}
مدة الفيديو: $durationDesc
الجودة: ${config.quality}
الأبعاد: ${targetSize.first}x${targetSize.second} (نسبة العرض ${config.aspect})
لغة كتابة القصة تعتمد على اللغة المكتوب بها نص الطلب.

الآن بعد كتابة القصة كاملة، يجب أن تُخرج نسخة أخرى بصيغة SSML قياسية للتحويل إلى صوت.
- ضع النص بين الرموز 🎵 في البداية والنهاية.
- قسّم القصة إلى مشاهد.
- كل مشهد يتضمن:
   * نص المشهد.
   * برومبت صورة المشهد محصوراً بين 😶...😶 وبالإنجليزية، ويجب أن تتطابق أبعاد الصورة مع ${targetSize.first}x${targetSize.second}.
   * برومبت حركة المشهد محصوراً بين 🥱...🥱 وبالإنجليزية.
- برومبتات المشاهد تسمى بالتسلسل: MSHHD1، MSHHD2 ...
- برومبتات تحريك المشاهد تسمى: HAREKA1، HAREKA2 ...
- ملف النص الصافي يسمى SCRIPTS_SSH.
- ملف الصوت يسمى SCRIPTS_SSML.
        """.trimIndent()
    }

    // ✅ استخراج مدة الصوت الفعلية
    private fun getAudioDuration(audioFile: File): Long {
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(audioFile.absolutePath)
            val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            retriever.release()
            durationStr?.toLongOrNull() ?: 0L
        } catch (e: Exception) {
            Timber.e(e, "فشل قراءة مدة الصوت")
            0L
        }
    }

    // ✅ تحليل العدد الذكي
    private suspend fun smartCountAnalysis(config: PipelineConfig, scriptText: String, audioDurationMs: Long): SmartCountResult {
        emitLog("🔢 بدء تحليل العدد الذكي...")
        val modelId = if (config.masterModelId.isNotBlank()) config.masterModelId else config.smartSel ?: "google/flan-t5-base"
        val token = settingsRepo.getHuggingFaceToken() ?: ""
        if (token.isBlank()) {
            emitLog("⚠️ لا يوجد توكن HuggingFace، استخدام القيم الافتراضية للعدد الذكي")
            return SmartCountResult(3, 2, 2)
        }
        val prompt = """
            أنت خبير في تحليل النصوص وتحديد عدد المؤثرات المناسبة.
            النص: ${scriptText.take(800)}
            مدة الصوت: ${audioDurationMs / 1000} ثانية.
            حدد الأعداد التالية (أعداد صحيحة فقط):
            - عدد المؤثرات الصوتية (audioFxCount): مناسب لعدد المشاهد.
            - عدد المؤثرات البصرية (visualFxCount): مناسب لعدد المشاهد.
            - عدد الانتقالات (transitionsCount): مناسب لعدد المشاهد.
            أجب فقط بالأرقام مفصولة بفواصل، مثال: 4,3,3
        """.trimIndent()
        val response = try {
            huggingFaceApi.generateText(modelId, mapOf("inputs" to prompt), "Bearer $token")
        } catch (e: Exception) { null }
        val text = response?.body()?.string() ?: "3,2,2"
        val parts = text.split(",").map { it.trim().toIntOrNull() ?: 2 }
        val result = SmartCountResult(
            audioFxCount = parts.getOrElse(0) { 3 },
            visualFxCount = parts.getOrElse(1) { 2 },
            transitionsCount = parts.getOrElse(2) { 2 }
        )
        emitLog("✅ العدد الذكي: صوتي=${result.audioFxCount}, بصري=${result.visualFxCount}, انتقالات=${result.transitionsCount}")
        return result
    }

    // ✅ المنسق الرئيسي
    private suspend fun orchestratePlan(config: PipelineConfig): OrchestrationPlan {
        emitLog("🎛️ تنسيق خطة المهام...")
        val modelId = if (config.masterModelId.isNotBlank()) config.masterModelId else config.orch ?: "google/flan-t5-base"
        val token = settingsRepo.getHuggingFaceToken() ?: ""
        if (token.isBlank()) {
            emitLog("⚠️ لا يوجد توكن HuggingFace، استخدام جميع المهام")
            return OrchestrationPlan(
                enableMaster = true, enableAudioFx = true, enableVisualFx = true,
                enableTransitions = true, enableSmartCount = true, enableSubtitles = true,
                enableMusic = true, enableReviewer = true, enableExternalVideo = true,
                enableExternalImage = true
            )
        }
        val prompt = """
            أنت منسق مهام. بناءً على النماذج المتاحة والإعدادات:
            النموذج الرئيسي: ${config.masterModelId}
            نموذج المؤثرات الصوتية: ${config.audioFxModelId}
            نموذج المؤثرات البصرية: ${config.visualFxModelId}
            نموذج الانتقالات: ${config.transitionsModelId}
            نموذج الترجمة: ${config.subtitlesModelId}
            نموذج الموسيقى: ${config.musicModelId}
            نموذج المراجع: ${config.reviewerModelId}
            هل يجب تفعيل كل مهمة؟ أجب بـ true/false لكل مهمة بالترتيب:
            master, audioFx, visualFx, transitions, smartCount, subtitles, music, reviewer, externalVideo, externalImage
        """.trimIndent()
        val response = try {
            huggingFaceApi.generateText(modelId, mapOf("inputs" to prompt), "Bearer $token")
        } catch (e: Exception) { null }
        val text = response?.body()?.string() ?: "true,true,true,true,true,true,true,true,true,true"
        val flags = text.split(",").map { it.trim().toBoolean() }
        return OrchestrationPlan(
            enableMaster = flags.getOrElse(0) { true },
            enableAudioFx = flags.getOrElse(1) { true },
            enableVisualFx = flags.getOrElse(2) { true },
            enableTransitions = flags.getOrElse(3) { true },
            enableSmartCount = flags.getOrElse(4) { true },
            enableSubtitles = flags.getOrElse(5) { true },
            enableMusic = flags.getOrElse(6) { true },
            enableReviewer = flags.getOrElse(7) { true },
            enableExternalVideo = flags.getOrElse(8) { true },
            enableExternalImage = flags.getOrElse(9) { true }
        )
    }

    // ✅ مراجعة الفيديو النهائي
    private suspend fun reviewFinalVideo(outputPath: String, config: PipelineConfig): String? {
        emitLog("📋 بدء مراجعة الفيديو النهائي...")
        val modelId = if (config.masterModelId.isNotBlank()) config.masterModelId else config.reviewer ?: "google/flan-t5-base"
        val token = settingsRepo.getHuggingFaceToken() ?: ""
        if (token.isBlank()) return null
        val prompt = """
            أنت مراجع خبير. قم بمراجعة الفيديو النهائي بناءً على الطلب الأصلي:
            الطلب: ${config.prompt}
            أجب بتقييم (ممتاز/جيد/ضعيف) واقتراح تحسينات في سطر واحد.
        """.trimIndent()
        val response = try {
            huggingFaceApi.generateText(modelId, mapOf("inputs" to prompt), "Bearer $token")
        } catch (e: Exception) { null }
        return response?.body()?.string()
    }

    // ----------------------------------------------
    // 2. توليد السيناريو (المساعدة)
    // ----------------------------------------------
    private suspend fun saveScriptAndExtract(scriptText: String, scriptsDir: File) {
        try {
            if (!scriptsDir.exists()) scriptsDir.mkdirs()
            File(scriptsDir, "script_full.txt").writeText(scriptText)
            extractTaggedFiles(scriptText, scriptsDir, "MSHHD", "😶", "😶")
            extractTaggedFiles(scriptText, scriptsDir, "HAREKA", "🥱", "🥱")
            extractTaggedFiles(scriptText, scriptsDir, "SCRIPTS_SSML", "🎵", "🎵")
            emitLog("تم حفظ السكريبت واستخراج الملفات")
        } catch (e: Exception) {
            emitLog("فشل حفظ السكريبت: ${e.message}")
        }
    }

    private fun extractTaggedFiles(text: String, dir: File, prefix: String, startTag: String, endTag: String) {
        var searchFrom = 0
        var counter = 1
        while (true) {
            val marker = if (prefix == "SCRIPTS_SSML") prefix else "$prefix$counter"
            val markerIdx = text.indexOf(marker, searchFrom)
            if (markerIdx < 0) break
            val startIdx = text.indexOf(startTag, markerIdx)
            val endIdx = text.indexOf(endTag, startIdx + startTag.length)
            if (startIdx >= 0 && endIdx > startIdx) {
                val content = text.substring(startIdx + startTag.length, endIdx).trim()
                val fileName = if (prefix == "SCRIPTS_SSML") "SCRIPTS_SSML.txt" else "${prefix}${counter}.txt"
                File(dir, fileName).writeText(content)
            }
            searchFrom = markerIdx + 1
            counter++
        }
    }

    // ----------------------------------------------
    // 3. توليد الصور (معدل لاستخدام الأبعاد المطلوبة)
    // ----------------------------------------------
    private suspend fun processImages(config: PipelineConfig, imagesDir: File, scriptsDir: File, visualFxCount: Int, targetSize: Pair<Int, Int>) {
        if (hfQuotaExceeded) return
        val hfToken = settingsRepo.getStringOnce("hf_token", "")
        val sdModel = if (config.masterModelId.isNotBlank()) config.masterModelId else config.sdModel.ifEmpty { "stabilityai/stable-diffusion-xl-base-1.0" }

        val genSize = targetSize  // ✅ استخدام الأبعاد المطلوبة مباشرة
        val preset = imagePresetForModel(sdModel)

        val mshhdFiles = scriptsDir.listFiles()
            ?.filter { it.name.startsWith("MSHHD") && it.name.endsWith(".txt") }
            ?.sortedBy { it.name } ?: emptyList()
        if (mshhdFiles.isEmpty()) {
            emitLog("لا توجد ملفات MSHHD في ${scriptsDir.absolutePath}")
            return
        }

        for ((index, file) in mshhdFiles.withIndex()) {
            if (checkCancel() || hfQuotaExceeded) return
            val idx = file.name.removePrefix("MSHHD").removeSuffix(".txt").toIntOrNull() ?: (index + 1)
            val promptRaw = file.readText().trim()
            val finalPrompt = patchPromptForModel(sdModel, promptRaw)
            emitProgress("image", "توليد الصورة $idx من ${mshhdFiles.size}...", 20 + (index * 25 / mshhdFiles.size))

            val imgBytes = tryGenerateImageWithAgent(hfToken, sdModel, finalPrompt, genSize, preset, "image")
            if (imgBytes != null) {
                val outFile = File(imagesDir, "MSHHD${idx}_MG.png")
                outFile.writeBytes(imgBytes)  // لا حاجة لإعادة التحجيم
                emitLog("تم إنشاء ${outFile.name} بحجم ${genSize.first}x${genSize.second}")
            } else {
                emitLog("فشل توليد الصورة لـ MSHHD$idx")
            }
            delay(500)
        }
    }

    private suspend fun tryGenerateImageWithAgent(
        hfToken: String, modelId: String,
        prompt: String, genSize: Pair<Int, Int>, preset: ImagePreset,
        category: String
    ): ByteArray? {
        var currentModelId = modelId
        var currentDepth = 1
        val maxDepth = agentOrchestrator.getCurrentMaxDepth()

        while (currentDepth <= maxDepth) {
            val result = tryGenerateImage(hfToken, currentModelId, prompt, genSize, preset)
            if (result != null) return result

            if (currentDepth >= maxDepth) break

            val alternative = agentOrchestrator.suggestAlternativeModel(
                category = category,
                failedModelId = currentModelId,
                errorMessage = "فشل توليد الصورة بعد المحاولة $currentDepth",
                currentDepth = currentDepth
            )
            if (alternative == null) break

            interventionHandler.temporarilySwitchModel(category, alternative.modelId)
            currentModelId = alternative.modelId
            currentDepth++
            emitLog("الوكيل: تم تغيير النموذج إلى $currentModelId (المحاولة $currentDepth)")
        }
        return null
    }

    private suspend fun tryGenerateImage(
        hfToken: String, modelId: String,
        prompt: String, genSize: Pair<Int, Int>, preset: ImagePreset
    ): ByteArray? {
        val (genW, genH) = genSize
        try {
            val result = callHuggingFaceImage(modelId, prompt, genW, genH, preset, hfToken)
            if (result != null) return result
        } catch (e: Exception) {
            if (handleQuotaError(e, "image", modelId)) return null
            emitLog("فشل HF image: ${e.message}")
        }

        val smallW = maxOf(512, (genW / 2 / 8) * 8)
        val smallH = maxOf(512, (genH / 2 / 8) * 8)
        val smallPreset = ImagePreset(maxOf(1, preset.steps - 4), (preset.guidance - 0.5).coerceAtLeast(0.1))
        return try {
            callHuggingFaceImage(modelId, prompt, smallW, smallH, smallPreset, hfToken)
        } catch (e: Exception) {
            if (handleQuotaError(e, "image", modelId)) null else null
        }
    }

    private suspend fun callHuggingFaceImage(
        modelId: String, prompt: String, w: Int, h: Int, preset: ImagePreset, hfToken: String
    ): ByteArray? {
        val response = huggingFaceApi.generateImage(modelId, HfImageRequestDto(inputs = prompt))
        return if (response.isSuccessful) response.body()?.byteStream()?.readBytes() else null
    }

    // ----------------------------------------------
    // 4. توليد الصوت (TTS)
    // ----------------------------------------------
    private suspend fun processTts(config: PipelineConfig, audiosDir: File, scriptsDir: File) {
        if (hfQuotaExceeded) return
        val hfToken = settingsRepo.getStringOnce("hf_token", "")
        val ttsModel = if (config.masterModelId.isNotBlank()) config.masterModelId else config.ttsModel.ifEmpty { "coqui/XTTS-v2" }
        val voiceSamplePath = settingsRepo.getStringOnce("voice_sample_path", "")
        val ssmlFile = File(scriptsDir, "SCRIPTS_SSML.txt")
        if (!ssmlFile.exists()) { emitLog("لا يوجد ملف SSML"); return }
        val plainText = stripSsml(ssmlFile.readText())
        emitProgress("tts", "توليد التعليق الصوتي...", 50)

        val audioBytes = tryGenerateSpeechWithAgent(hfToken, ttsModel, plainText, config.voiceChoice, voiceSamplePath, "tts")
        if (audioBytes != null) {
            val outFile = File(audiosDir, "SCRIPTS_SSML_AU.wav")
            outFile.writeBytes(audioBytes)
            emitLog("تم توليد التعليق الصوتي: ${outFile.name}")
        } else {
            emitLog("فشل توليد الصوت")
        }
    }

    private suspend fun tryGenerateSpeechWithAgent(
        hfToken: String, modelId: String, text: String, voiceChoice: String, voiceSamplePath: String,
        category: String
    ): ByteArray? {
        var currentModelId = modelId
        var currentDepth = 1
        val maxDepth = agentOrchestrator.getCurrentMaxDepth()

        while (currentDepth <= maxDepth) {
            val result = tryGenerateSpeech(hfToken, currentModelId, text, voiceChoice, voiceSamplePath)
            if (result != null) return result

            if (currentDepth >= maxDepth) break

            val alternative = agentOrchestrator.suggestAlternativeModel(
                category = category,
                failedModelId = currentModelId,
                errorMessage = "فشل توليد الصوت بعد المحاولة $currentDepth",
                currentDepth = currentDepth
            )
            if (alternative == null) break

            interventionHandler.temporarilySwitchModel(category, alternative.modelId)
            currentModelId = alternative.modelId
            currentDepth++
            emitLog("الوكيل: تم تغيير نموذج الصوت إلى $currentModelId (المحاولة $currentDepth)")
        }
        return null
    }

    private suspend fun tryGenerateSpeech(
        hfToken: String, modelId: String, text: String, voiceChoice: String, voiceSamplePath: String
    ): ByteArray? {
        val wantClone = voiceChoice == VOICE_CLONE_OPTION && voiceSamplePath.isNotBlank()

        try {
            val request = HfTtsRequestDto(inputs = text)
            val response = huggingFaceApi.generateSpeech(modelId, request)
            if (response.isSuccessful) return response.body()?.byteStream()?.readBytes()
        } catch (e: Exception) {
            if (handleQuotaError(e, "tts", modelId)) return null
            emitLog("فشل TTS عادي: ${e.message}")
        }

        if (wantClone) {
            try {
                val sampleBase64 = settingsRepo.getVoiceSampleBase64(modelId)
                if (sampleBase64 != null) {
                    val parameters = HfTtsParameters(
                        language = "ar",
                        speakerWav = sampleBase64,
                        speed = 1.0f
                    )
                    val request = HfTtsRequestDto(inputs = text, parameters = parameters)
                    val response = huggingFaceApi.generateClonedSpeech(modelId, request)
                    if (response.isSuccessful) {
                        emitLog("تم توليد الصوت باستنساخ العينة للنموذج $modelId")
                        return response.body()?.byteStream()?.readBytes()
                    } else {
                        emitLog("فشل استنساخ الصوت: ${response.code()} - ${response.message()}")
                    }
                } else {
                    emitLog("لا توجد عينة صوت مخزنة للنموذج $modelId")
                }
            } catch (e: Exception) {
                if (handleQuotaError(e, "tts", modelId)) return null
                emitLog("استثناء في استنساخ الصوت: ${e.message}")
            }
        }
        return null
    }

    // ----------------------------------------------
    // 5. تحويل الصور إلى فيديوهات قصيرة (img2vid) مع عدد الانتقالات
    // ----------------------------------------------
    private suspend fun processVideo(config: PipelineConfig, videosDir: File, imagesDir: File, scriptsDir: File, transitionsCount: Int) {
        if (hfQuotaExceeded) return
        val hfToken = settingsRepo.getStringOnce("hf_token", "")
        val img2VidModel = if (config.masterModelId.isNotBlank()) config.masterModelId else config.img2VidModel.ifEmpty { "stabilityai/stable-video-diffusion-img2vid" }

        val harekaFiles = scriptsDir.listFiles()
            ?.filter { it.name.startsWith("HAREKA") && it.name.endsWith(".txt") }
            ?.sortedBy { it.name } ?: emptyList()
        if (harekaFiles.isEmpty()) { emitLog("لا توجد ملفات HAREKA"); return }

        val totalDurationMs = ((config.minutes.toIntOrNull() ?: 1) * 60 + (config.seconds.toIntOrNull() ?: 30)) * 1000
        val perClipMs = maxOf(2000, totalDurationMs / maxOf(1, harekaFiles.size))

        for ((index, file) in harekaFiles.withIndex()) {
            if (checkCancel() || hfQuotaExceeded) return
            val idx = file.name.removePrefix("HAREKA").removeSuffix(".txt").toIntOrNull() ?: (index + 1)
            val motionPrompt = file.readText()
            val baseImgFile = File(imagesDir, "MSHHD${idx}_MG.png")
            emitProgress("video", "توليد حركة $idx من ${harekaFiles.size}...", 70 + (index * 20 / harekaFiles.size))

            if (baseImgFile.exists()) {
                val outFile = File(videosDir, "HAREKA${idx}_VO.mp4")
                val videoBytes = tryGenerateVideoWithAgent(hfToken, img2VidModel, baseImgFile, motionPrompt, "video")
                if (videoBytes != null) {
                    outFile.writeBytes(videoBytes)
                    emitLog("تم إنشاء ${outFile.name}")
                } else {
                    createPlaceholderVideo(outFile, perClipMs)
                    emitLog("فيديو تجريبي لـ HAREKA$idx")
                }
            } else {
                emitLog("الصورة الأساسية مفقودة لـ HAREKA$idx")
                createPlaceholderVideo(File(videosDir, "HAREKA${idx}_VO.mp4"), perClipMs)
            }
            delay(500)
        }
    }

    private suspend fun tryGenerateVideoWithAgent(
        hfToken: String, modelId: String, imageFile: File, motionPrompt: String, category: String
    ): ByteArray? {
        var currentModelId = modelId
        var currentDepth = 1
        val maxDepth = agentOrchestrator.getCurrentMaxDepth()

        while (currentDepth <= maxDepth) {
            val result = tryGenerateVideo(hfToken, currentModelId, imageFile, motionPrompt)
            if (result != null) return result

            if (currentDepth >= maxDepth) break

            val alternative = agentOrchestrator.suggestAlternativeModel(
                category = category,
                failedModelId = currentModelId,
                errorMessage = "فشل توليد الفيديو بعد المحاولة $currentDepth",
                currentDepth = currentDepth
            )
            if (alternative == null) break

            interventionHandler.temporarilySwitchModel(category, alternative.modelId)
            currentModelId = alternative.modelId
            currentDepth++
            emitLog("الوكيل: تم تغيير نموذج الفيديو إلى $currentModelId (المحاولة $currentDepth)")
        }
        return null
    }

    private suspend fun tryGenerateVideo(
        hfToken: String, modelId: String, imageFile: File, motionPrompt: String
    ): ByteArray? {
        try {
            val request = HfImageRequestDto(inputs = "data:image/png;base64,${readFileB64(imageFile.absolutePath)}")
            val response = huggingFaceApi.generateImage(modelId, request)
            return if (response.isSuccessful) response.body()?.byteStream()?.readBytes() else null
        } catch (e: Exception) {
            if (handleQuotaError(e, "img2vid", modelId)) return null
            return null
        }
    }

    // ----------------------------------------------
    // 6. تجميع الفيديو النهائي (معدل لاستخدام الأبعاد والعدد الذكي والخارجي)
    // ----------------------------------------------
    private suspend fun assembleWithMontagePlan(
        config: PipelineConfig,
        scriptsDir: File,
        imagesDir: File,
        audiosDir: File,
        videosDir: File,
        projectTempDir: File,
        projectId: Long,
        smartCount: SmartCountResult,
        plan: OrchestrationPlan,
        targetSize: Pair<Int, Int>
    ): String {
        val finalDir = ensureDir("$projectTempDir/FINAL")
        val outputPath = File(finalDir, "final_${System.currentTimeMillis()}.mp4").absolutePath
        try {
            val montagePlan = buildMontagePlan(config, scriptsDir, imagesDir, audiosDir, videosDir, outputPath, smartCount, plan, targetSize)
            val command = commandBuilder.buildCommand(montagePlan)
            emitLog("أمر FFmpeg النهائي: ${command.take(200)}...")
            val result = FFmpegRunner.execute(command)
            if (result.isSuccess) {
                emitLog("تم تجميع الفيديو النهائي: $outputPath")
                return outputPath
            } else {
                throw Exception("FFmpeg فشل: ${result.exceptionOrNull()?.message}")
            }
        } catch (e: Exception) {
            emitError("ffmpeg", "فشل تجميع الفيديو: ${e.message}")
            createPlaceholderVideo(File(outputPath), 5000)
            return outputPath
        }
    }

    private suspend fun buildMontagePlan(
        config: PipelineConfig,
        scriptsDir: File,
        imagesDir: File,
        audiosDir: File,
        videosDir: File,
        outputPath: String,
        smartCount: SmartCountResult,
        plan: OrchestrationPlan,
        targetSize: Pair<Int, Int>
    ): MontagePlan {
        val style = config.montageStyle.ifEmpty { "قصص وروايات" }
        val profilePrefix = "profile_${style}_"

        val useAudioFx = plan.enableAudioFx && settingsRepo.getBoolFlag(profilePrefix + "audio_on", false)
        val useVisualFx = plan.enableVisualFx && settingsRepo.getBoolFlag(profilePrefix + "visual_on", false)
        val useTransitions = plan.enableTransitions && settingsRepo.getBoolFlag(profilePrefix + "trans_on", false)
        val useExternalVideo = plan.enableExternalVideo && settingsRepo.getBoolFlag(profilePrefix + "external_video_on", false)
        val useExternalImage = plan.enableExternalImage && settingsRepo.getBoolFlag(profilePrefix + "external_image_on", false)
        val useMusic = plan.enableMusic && settingsRepo.getBoolFlag(profilePrefix + "music_on", false)

        val imageFiles = scriptsDir.listFiles()
            ?.filter { it.name.startsWith("MSHHD") && it.name.endsWith(".txt") }
            ?.sortedBy { it.name } ?: emptyList()

        val sceneDescriptions = imageFiles.mapNotNull { file -> file.readText().take(200) }
        val totalDurationMs = ((config.minutes.toIntOrNull() ?: 1) * 60 + (config.seconds.toIntOrNull() ?: 30)) * 1000L
        val perSceneDurationMs = totalDurationMs / maxOf(1, imageFiles.size)

        val inputs = imageFiles.mapIndexed { index, _ ->
            val imgFile = File(imagesDir, "MSHHD${index + 1}_MG.png")
            MontagePlan.MontageInput(
                path = if (imgFile.exists()) imgFile.absolutePath else "",
                type = "image",
                durationMs = perSceneDurationMs
            )
        }.filter { it.path.isNotEmpty() }.toMutableList()

        // 1. الانتقالات (مع احترام العدد الذكي)
        val transitions = if (useTransitions && inputs.size > 1) {
            mutableListOf<MontagePlan.MontageTransition>().apply {
                val maxTrans = minOf(smartCount.transitionsCount, inputs.size - 1)
                for (i in 0 until maxTrans) {
                    val prevScene = sceneDescriptions.getOrElse(i) { "" }
                    val nextScene = sceneDescriptions.getOrElse(i + 1) { "" }
                    val candidates = fetchAssets(assetProviders, "transition", "", 10)
                    val finalCandidates = if (candidates.isNotEmpty()) candidates else listOf(
                        Asset(id = "fade", name = "تلاشي", command = "fade"),
                        Asset(id = "slide", name = "انزلاق", command = "slide"),
                        Asset(id = "wipe", name = "مسح", command = "wipe"),
                        Asset(id = "zoom", name = "تكبير", command = "zoom")
                    )
                    val suggestedTransition = agentOrchestrator.suggestTransition(prevScene, nextScene, finalCandidates)
                    val transType = suggestedTransition?.command ?: "fade"
                    add(MontagePlan.MontageTransition(fromIndex = i, toIndex = i + 1, type = transType, durationMs = 500L))
                    emitLog("الوكيل: تم اختيار انتقال '$transType' بين المشهد ${i+1} و ${i+2}")
                }
            }
        } else emptyList()

        // 2. التراكبات (مؤثرات بصرية)
        val overlays = mutableListOf<MontagePlan.MontageOverlay>()
        if (useVisualFx) {
            for (i in 0 until minOf(smartCount.visualFxCount, sceneDescriptions.size)) {
                overlays.add(
                    MontagePlan.MontageOverlay(
                        type = "text",
                        content = "تأثير بصري للمشهد ${i+1}",
                        startMs = i * perSceneDurationMs,
                        durationMs = perSceneDurationMs,
                        position = "center",
                        fontSize = 20,
                        fontColor = "white"
                    )
                )
            }
        }

        // 3. المسارات الصوتية
        val audioTracks = mutableListOf<MontagePlan.MontageAudio>()
        val mainAudioFile = File(audiosDir, "SCRIPTS_SSML_AU.wav")
        if (mainAudioFile.exists()) {
            audioTracks.add(
                MontagePlan.MontageAudio(
                    path = mainAudioFile.absolutePath,
                    startMs = 0L,
                    durationMs = totalDurationMs,
                    volume = 1.0,
                    fadeInMs = 500,
                    fadeOutMs = 1000
                )
            )
        }

        if (useAudioFx) {
            val overallTheme = "موضوع الفيديو: ${config.prompt}"
            val musicCandidates = fetchAssets(assetProviders, "music", overallTheme, 5)
            if (musicCandidates.isNotEmpty()) {
                val suggestedMusic = agentOrchestrator.suggestMusic(overallTheme, sceneDescriptions, musicCandidates)
                if (suggestedMusic != null) {
                    val localPath = suggestedMusic.localPath ?: downloadAssetIfNeeded(suggestedMusic)
                    if (localPath != null) {
                        audioTracks.add(
                            MontagePlan.MontageAudio(
                                path = localPath,
                                startMs = 0L,
                                durationMs = totalDurationMs,
                                volume = 0.5,
                                fadeInMs = 2000,
                                fadeOutMs = 3000
                            )
                        )
                        emitLog("الوكيل: تم اختيار الموسيقى الخلفية '${suggestedMusic.name}'")
                    }
                }
            }

            for (i in 0 until minOf(smartCount.audioFxCount, sceneDescriptions.size)) {
                val sceneDesc = sceneDescriptions[i]
                val sfxCandidates = fetchAssets(assetProviders, "sfx", sceneDesc, 3)
                val suggestedSfx = if (sfxCandidates.isNotEmpty()) {
                    agentOrchestrator.suggestSoundEffects(sceneDesc, sfxCandidates)
                } else emptyList()
                suggestedSfx.forEach { sfx ->
                    val localPath = sfx.localPath ?: downloadAssetIfNeeded(sfx)
                    if (localPath != null) {
                        audioTracks.add(
                            MontagePlan.MontageAudio(
                                path = localPath,
                                startMs = (i * perSceneDurationMs).coerceAtMost(totalDurationMs - 5000),
                                durationMs = perSceneDurationMs,
                                volume = 0.7,
                                fadeInMs = 300,
                                fadeOutMs = 500
                            )
                        )
                        emitLog("الوكيل: تم إضافة مؤثر صوتي '${sfx.name}' للمشهد ${i+1}")
                    }
                }
            }
        }

        // 4. عناصر خارجية (فيديوهات، صور)
        if (useExternalVideo) {
            val videoQuery = config.prompt.take(50)
            val videoAssets = fetchAssets(assetProviders, "video", videoQuery, 2)
            if (videoAssets.isNotEmpty()) {
                val firstVideo = videoAssets.first()
                val localPath = firstVideo.localPath ?: downloadAssetIfNeeded(firstVideo)
                if (localPath != null) {
                    inputs.add(
                        MontagePlan.MontageInput(
                            path = localPath,
                            type = "video",
                            durationMs = perSceneDurationMs
                        )
                    )
                    emitLog("تم إضافة فيديو خارجي '${firstVideo.name}'")
                }
            }
        }

        if (useExternalImage) {
            val imageQuery = config.prompt.take(50)
            val imageAssets = fetchAssets(assetProviders, "image", imageQuery, 2)
            if (imageAssets.isNotEmpty()) {
                val firstImage = imageAssets.first()
                val localPath = firstImage.localPath ?: downloadAssetIfNeeded(firstImage)
                if (localPath != null) {
                    overlays.add(
                        MontagePlan.MontageOverlay(
                            type = "image",
                            content = localPath,
                            startMs = 0,
                            durationMs = totalDurationMs,
                            position = "center",
                            fontSize = 0,
                            fontColor = ""
                        )
                    )
                    emitLog("تم إضافة صورة خارجية '${firstImage.name}' كتراكب")
                }
            }
        }

        val subtitleStyle = if (plan.enableSubtitles) getSubtitleStyle(scriptsDir, config) else null

        return MontagePlan(
            inputFiles = inputs,
            transitions = transitions,
            overlays = overlays,
            audioTracks = audioTracks,
            outputSettings = MontagePlan.OutputSettings(
                width = targetSize.first,
                height = targetSize.second,
                fps = config.selectedFps.toIntOrNull() ?: 30,
                aspectRatio = config.aspect,
                quality = config.quality,
                outputPath = outputPath
            ),
            subtitle = subtitleStyle
        )
    }

    // ----------------------------------------------
    // 7. دوال مساعدة للأصول والذاكرة المؤقتة
    // ----------------------------------------------
    private suspend fun fetchAssets(
        providers: Set<AssetProvider>,
        type: String,
        query: String,
        limit: Int
    ): List<Asset> {
        for (provider in providers) {
            val assets = try {
                when (type) {
                    "video" -> provider.searchVideos(query, limit)
                    "image" -> provider.searchImages(query, limit)
                    "music" -> provider.searchMusic(query, limit)
                    "sfx" -> provider.searchSoundEffects(query, limit)
                    "transition" -> provider.getTransitions(limit)
                    else -> emptyList()
                }
            } catch (e: Exception) {
                emitLog("خطأ في مزود الأصول ${provider::class.simpleName}: ${e.message}")
                emptyList()
            }
            if (assets.isNotEmpty()) {
                emitLog("تم جلب ${assets.size} عنصر من نوع $type من ${provider::class.simpleName}")
                return assets
            }
        }
        return emptyList()
    }

    private suspend fun downloadAssetIfNeeded(asset: Asset): String? {
        val url = asset.fileUrl ?: return null
        val fileName = asset.id + "_" + url.substringAfterLast("/")
        val cacheDir = File(settingsRepo.getCacheAssetsDir())
        if (!cacheDir.exists()) cacheDir.mkdirs()
        val cachedFile = File(cacheDir, fileName)

        if (cachedFile.exists()) {
            emitLog("استخدام العنصر من الكاش: ${cachedFile.absolutePath}")
            return cachedFile.absolutePath
        }

        return try {
            val request = Request.Builder().url(url).build()
            val response = okHttpClient.newCall(request).execute()
            if (response.isSuccessful && response.body != null) {
                cachedFile.outputStream().use { output -> response.body!!.byteStream().copyTo(output) }
                emitLog("تم تحميل العنصر: ${cachedFile.absolutePath}")
                cachedFile.absolutePath
            } else {
                emitLog("فشل تحميل العنصر: ${asset.name} (${response.code})")
                null
            }
        } catch (e: Exception) {
            emitLog("استثناء أثناء تحميل العنصر: ${e.message}")
            null
        }
    }

    private suspend fun getSubtitleStyle(scriptsDir: File, config: PipelineConfig): MontagePlan.SubtitleStyle? {
        val profilePrefix = "profile_${config.montageStyle}_"
        val subtitleEnabled = settingsRepo.getBoolFlag(profilePrefix + "sub_on", false)
        if (!subtitleEnabled) return null

        val scriptFile = File(scriptsDir, "script_full.txt")
        val subtitleText = if (scriptFile.exists()) scriptFile.readText().take(200) else config.prompt.take(200)

        val fontSize = settingsRepo.getStringOnce("sub_font_size", "32").toIntOrNull() ?: 32
        val textColor = settingsRepo.getStringOnce("sub_text_color", "#FFFFFF")
        val bgColor = settingsRepo.getStringOnce("sub_bg_color", "#000000")
        val bgOpacity = settingsRepo.getStringOnce("sub_bg_opacity", "40").toIntOrNull() ?: 40
        val alignment = settingsRepo.getStringOnce("sub_alignment", "center")
        val shadow = settingsRepo.getStringOnce("sub_shadow", "قوي")
        val font = settingsRepo.getStringOnce("sub_font", "default")

        return MontagePlan.SubtitleStyle(
            text = subtitleText,
            fontSize = fontSize,
            fontColor = textColor,
            backgroundColor = bgColor,
            backgroundOpacity = bgOpacity,
            position = alignment,
            shadow = shadow,
            fontFamily = font
        )
    }

    // ----------------------------------------------
    // 8. تنظيف الملفات المؤقتة
    // ----------------------------------------------
    private suspend fun cleanupTempFiles(projectTempDir: File) {
        try {
            listOf("SCRIPT", "IMAGES", "AUDIOS", "VIDEOS").forEach { sub ->
                val subDir = File(projectTempDir, sub)
                if (subDir.exists()) subDir.deleteRecursively()
            }
            emitLog("تم حذف الملفات المؤقتة للمشروع ${projectTempDir.name}")
        } catch (e: Exception) {
            emitLog("فشل حذف الملفات المؤقتة: ${e.message}")
        }
    }

    private suspend fun saveErrorLog(projectTempDir: File, error: Exception) {
        try {
            val errorDir = File(settingsRepo.getErrorsDir())
            if (!errorDir.exists()) errorDir.mkdirs()
            val logFile = File(errorDir, "pipeline_error_${System.currentTimeMillis()}.log")
            logFile.writeText("${error.javaClass.simpleName}: ${error.message}\n${error.stackTraceToString()}")
        } catch (_: Exception) { }
    }

    // ----------------------------------------------
    // 9. دوال مساعدة عامة
    // ----------------------------------------------
    private fun ensureDir(path: String): File { val dir = File(path); if (!dir.exists()) dir.mkdirs(); return dir }
    private suspend fun handleQuotaError(e: Exception, stage: String, modelId: String): Boolean {
        if (e.message?.contains("402") == true) {
            hfQuotaExceeded = true; hfQuotaModel = modelId; hfQuotaStage = stage
            emitError(stage, "⛔ نفدت حصة HuggingFace للموديل: $modelId")
            return true
        }
        return false
    }

    private data class ImagePreset(val steps: Int, val guidance: Double)
    private data class MotionParams(val fps: Int, val numFrames: Int, val motionBucketId: Int, val noiseAugStrength: Float)

    private fun computeTargetSize(aspect: String, quality: String): Pair<Int, Int> {
        return when (aspect) {
            "16:9" -> when (quality.lowercase()) {
                "480p" -> 848 to 480; "720p" -> 1280 to 720; "2k" -> 2560 to 1440; "4k" -> 3840 to 2160; else -> 1920 to 1080
            }
            "9:16" -> when (quality.lowercase()) {
                "480p" -> 480 to 848; "720p" -> 720 to 1280; "2k" -> 1440 to 2560; "4k" -> 2160 to 3840; else -> 1080 to 1920
            }
            "1:1" -> when (quality.lowercase()) {
                "480p" -> 480 to 480; "720p" -> 720 to 720; "2k" -> 1440 to 1440; "4k" -> 2160 to 2160; else -> 1080 to 1080
            }
            else -> 1920 to 1080
        }
    }

    private fun computeGenSize(modelId: String, aspect: String): Pair<Int, Int> {
        val longSide = when {
            modelId.contains("sdxl", ignoreCase = true) || modelId.contains("portraitplus", ignoreCase = true) -> 1024
            modelId.contains("realistic_vision", ignoreCase = true) -> 896
            else -> 768
        }
        return longSide to longSide
    }

    private fun imagePresetForModel(modelId: String): ImagePreset {
        return when {
            modelId.contains("sdxl", ignoreCase = true) -> ImagePreset(28, 8.0)
            modelId.contains("dreamlike", ignoreCase = true) -> ImagePreset(30, 7.0)
            modelId.contains("realistic_vision", ignoreCase = true) -> ImagePreset(30, 7.5)
            else -> ImagePreset(28, 7.5)
        }
    }

    private fun parseMotionParams(txt: String, clipMs: Int): MotionParams {
        val fps = when { txt.contains("slow", ignoreCase = true) -> 10; txt.contains("fast", ignoreCase = true) -> 14; else -> 12 }
        val frames = maxOf(8, ((fps * (clipMs / 1000.0))).toInt())
        return MotionParams(fps, frames, 120, 0.08f)
    }

    private fun patchPromptForModel(modelId: String, prompt: String): String = if (modelId.contains("openjourney", ignoreCase = true)) "mdjrny-v4 style, $prompt" else prompt
    private fun stripSsml(ssml: String): String = ssml.replace(Regex("<[^>]+>"), " ").trim()
    private fun readFileB64(path: String): String = try { android.util.Base64.encodeToString(File(path).readBytes(), android.util.Base64.NO_WRAP) } catch (_: Exception) { "" }
    private fun createPlaceholderVideo(file: File, durationMs: Int) {
        try {
            file.parentFile?.mkdirs()
            FileOutputStream(file).use { fos ->
                val header = "AI-AutoCreate Placeholder MP4\n".toByteArray()
                fos.write(header)
                val bytes = maxOf(1024, minOf(256 * 1024, if (durationMs <= 0) 32 * 1024 else durationMs / 10))
                val buf = ByteArray(1024) { (it and 0xFF).toByte() }
                var written = 0
                while (written < bytes) { val toWrite = minOf(1024, bytes - written); fos.write(buf, 0, toWrite); written += toWrite }
            }
        } catch (_: Exception) { }
    }

    private suspend fun emitProgress(stage: String, msg: String, percent: Int) {
        _events.emit(PipelineEvent.Progress(stage, msg, percent))
        _events.emit(PipelineEvent.Log("[$stage] $msg"))
    }

    private suspend fun emitLog(msg: String) {
        _events.emit(PipelineEvent.Log(msg))
    }

    private suspend fun emitError(stage: String, msg: String) {
        _events.emit(PipelineEvent.Error(stage, msg))
    }

    private suspend fun emitFinalResult(path: String) {
        _events.emit(PipelineEvent.FinalResult(path))
    }
}

private suspend fun AppSettingsRepository.getBoolFlag(key: String, default: Boolean): Boolean {
    return try { getStringOnce(key, if (default) "true" else "false").toBoolean() } catch (_: Exception) { default }
}
