package com.aiautocreate.domain.service

import android.content.Context
import com.aiautocreate.domain.model.MontagePlan
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ✅ FIXED: Audio filters now properly chained with input/output labels.
 * ✅ FIXED: Subtitle filter ordering corrected - [v_out] is now properly defined before use.
 * ✅ FIXED: Arabic font paths now use assets/ directory instead of non-existent /system/fonts/.
 * ✅ FIXED: Added audio codec (-c:a aac -b:a 192k) for compatibility.
 */
@Singleton
class FFmpegCommandBuilder @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun buildCommand(plan: MontagePlan): String {
        val sb = StringBuilder()

        // 1. إدخال الملفات
        plan.inputFiles.forEach { input ->
            when (input.type) {
                "video" -> sb.append("-i "${input.path}" ")
                "image" -> sb.append("-loop 1 -t ${input.durationMs / 1000} -i "${input.path}" ")
            }
        }

        // 2. إدخال الصوت
        plan.audioTracks.forEach { audio ->
            sb.append("-i "${audio.path}" ")
        }

        // 3. بناء filter_complex
        val filterParts = mutableListOf<String>()

        // تحويل الصور إلى فيديو بنفس الأبعاد
        plan.inputFiles.forEachIndexed { index, input ->
            if (input.type == "image") {
                filterParts.add(
                    "[${index}:v]scale=${plan.outputSettings.width}:${plan.outputSettings.height}:force_original_aspect_ratio=decrease,pad=${plan.outputSettings.width}:${plan.outputSettings.height}:(ow-iw)/2:(oh-ih)/2,setsar=1[v$index]"
                )
            } else if (input.type == "video") {
                filterParts.add(
                    "[${index}:v]scale=${plan.outputSettings.width}:${plan.outputSettings.height}:force_original_aspect_ratio=decrease,pad=${plan.outputSettings.width}:${plan.outputSettings.height}:(ow-iw)/2:(oh-ih)/2,setsar=1,setdar=${plan.outputSettings.aspectRatio.replace(":", "/")}[v$index]"
                )
            }
        }

        // تطبيق الانتقالات بين المقاطع
        val videoLabelAfterTransitions: String
        if (plan.transitions.isNotEmpty()) {
            val transitionFilter = buildTransitionFilters(plan, filterParts)
            filterParts.clear()
            filterParts.add(transitionFilter)
            videoLabelAfterTransitions = "[v_out]"
        } else {
            // بدون انتقالات: وصل مباشر
            val allVideos = plan.inputFiles.indices.joinToString("") { "[v$it]" }
            filterParts.add("${allVideos}concat=n=${plan.inputFiles.size}:v=1:a=0[v_out]")
            videoLabelAfterTransitions = "[v_out]"
        }

        // ✅ إضافة فلتر الترجمة إذا وُجدت (BEFORE overlays so [v_out] exists)
        val videoLabelAfterSubtitles: String
        if (plan.subtitle != null) {
            val durationTotal = plan.inputFiles.sumOf { it.durationMs } / 1000.0
            val subtitleFilter = buildSubtitleFilter(plan.subtitle, durationTotal)
            // ✅ FIXED: Apply subtitle on [v_out] which is guaranteed to exist at this point
            filterParts.add("${videoLabelAfterTransitions}${subtitleFilter}[v_sub]")
            videoLabelAfterSubtitles = "[v_sub]"
        } else {
            videoLabelAfterSubtitles = videoLabelAfterTransitions
        }

        // إضافة التراكبات (نصوص، علامات مائية) - تُطبق بعد الترجمة
        plan.overlays.forEach { overlay ->
            when (overlay.type) {
                "text" -> {
                    val position = when (overlay.position) {
                        "center" -> "x=(w-text_w)/2:y=(h-text_h)/2"
                        "top_center" -> "x=(w-text_w)/2:y=10"
                        "bottom_center" -> "x=(w-text_w)/2:y=h-th-10"
                        else -> "x=(w-text_w)/2:y=(h-text_h)/2"
                    }
                    val escapedContent = overlay.content.replace("'", "\'")
                    filterParts.add(
                        "${videoLabelAfterSubtitles}drawtext=text='$escapedContent':fontsize=${overlay.fontSize}:fontcolor=${overlay.fontColor}:$position:enable='between(t,${overlay.startMs / 1000.0},${(overlay.startMs + overlay.durationMs) / 1000.0})'[v_text]"
                    )
                }
                "image" -> {
                    filterParts.add(
                        "${videoLabelAfterSubtitles}movie=${overlay.content}[logo];[v_out][logo]overlay=(W-w)/2:(H-h)/2:enable='between(t,${overlay.startMs / 1000.0},${(overlay.startMs + overlay.durationMs) / 1000.0})'[v_img]"
                    )
                }
            }
        }

        // تحديد المخرج النهائي للفيديو
        val finalVideoLabel = if (plan.overlays.isNotEmpty()) {
            // آخر overlay يُنتج [v_text] أو [v_img]
            val lastOverlay = plan.overlays.last()
            when (lastOverlay.type) {
                "text" -> "[v_text]"
                "image" -> "[v_img]"
                else -> videoLabelAfterSubtitles
            }
        } else {
            videoLabelAfterSubtitles
        }

        // معالجة الصوت (دمج، ضبط مستوى، تلاشي)
        // ✅ FIXED: Audio filters now properly chained with input/output labels
        val audioFilters = mutableListOf<String>()
        plan.audioTracks.forEachIndexed { index, audio ->
            val inputIndex = plan.inputFiles.size + index
            var audioChain = "[${inputIndex}:a]adelay=${audio.startMs}|${audio.startMs},volume=${audio.volume}"
            if (audio.fadeInMs > 0) {
                audioChain += ",afade=t=in:st=0:d=${audio.fadeInMs / 1000.0}"
            }
            if (audio.fadeOutMs > 0) {
                val fadeOutStart = maxOf(0.0, (audio.startMs + audio.durationMs - audio.fadeOutMs) / 1000.0)
                audioChain += ",afade=t=out:st=$fadeOutStart:d=${audio.fadeOutMs / 1000.0}"
            }
            audioChain += "[a$index]"
            audioFilters.add(audioChain)
        }

        // تجميع filter_complex النهائي
        val allFilters = mutableListOf<String>()
        allFilters.addAll(filterParts)
        if (audioFilters.isNotEmpty()) {
            allFilters.addAll(audioFilters)
            val allAudio = plan.audioTracks.indices.joinToString("") { "[a$it]" }
            allFilters.add("${allAudio}amix=inputs=${plan.audioTracks.size}:duration=first:dropout_transition=2[a_out]")
        }

        if (allFilters.isNotEmpty()) {
            sb.append("-filter_complex "")
            sb.append(allFilters.joinToString(";"))
            sb.append("" ")
        }

        // خريطة الإخراج
        sb.append("-map "$finalVideoLabel" ")
        if (audioFilters.isNotEmpty()) {
            sb.append("-map "[a_out]" ")
        }

        // إعدادات التصدير
        sb.append("-r ${plan.outputSettings.fps} ")
        sb.append("-c:v libx264 -crf 18 -preset medium ")
        // ✅ FIXED: Added audio codec for proper playback compatibility
        sb.append("-c:a aac -b:a 192k ")
        sb.append("-pix_fmt yuv420p ")
        sb.append("-movflags +faststart ") // ✅ NEW: Enable fast start for web playback

        // مسار المخرج
        sb.append(""${plan.outputSettings.outputPath}" -y")

        return sb.toString().trim()
    }

    private fun buildTransitionFilters(plan: MontagePlan, existingFilters: List<String>): String {
        val parts = mutableListOf<String>()

        plan.inputFiles.forEachIndexed { index, _ ->
            if (index == 0) {
                parts.add("[v0]setpts=PTS-STARTPTS[f0]")
            } else {
                val prevIndex = index - 1
                val transition = plan.transitions.firstOrNull { it.fromIndex == prevIndex && it.toIndex == index }
                val transType = transition?.type ?: "fade"
                val transDuration = transition?.durationMs ?: 500L

                when (transType) {
                    "fade" -> parts.add("[f${prevIndex}][v${index}]xfade=transition=fade:duration=${transDuration / 1000.0}:offset=${plan.inputFiles[prevIndex].durationMs / 1000.0 - transDuration / 1000.0}[f${index}]")
                    "cut" -> parts.add("[f${prevIndex}][v${index}]concat=n=2:v=1:a=0[f${index}]")
                    else -> parts.add("[f${prevIndex}][v${index}]xfade=transition=${transType}:duration=${transDuration / 1000.0}:offset=${plan.inputFiles[prevIndex].durationMs / 1000.0 - transDuration / 1000.0}[f${index}]")
                }
            }
        }

        parts.add("[f${plan.inputFiles.size - 1}]format=yuv420p[v_out]")
        return parts.joinToString(";")
    }

    /**
     * ✅ FIXED: Font paths now use assets/fonts/ directory.
     * System fonts like /system/fonts/Cairo.ttf don't exist on most Android devices.
     * Falls back to default font if custom font file is not found in assets.
     */
    private fun buildSubtitleFilter(subtitle: MontagePlan.SubtitleStyle, totalDurationSec: Double): String {
        val positionX = "(w-text_w)/2"
        val positionY = when (subtitle.position) {
            "top" -> "10"
            "bottom" -> "h-th-10"
            else -> "(h-text_h)/2"
        }

        // خلفية النص مع الشفافية
        val bgAlpha = (subtitle.backgroundOpacity / 100.0).toString()
        val boxColor = if (subtitle.backgroundOpacity > 0) "${subtitle.backgroundColor}@$bgAlpha" else "none"

        // الظل
        val shadowSetting = when (subtitle.shadow) {
            "خفيف" -> ":shadowcolor=black@0.5:shadowx=2:shadowy=2"
            "قوي" -> ":shadowcolor=black@0.8:shadowx=4:shadowy=4"
            else -> ""
        }

        // هروب النص من الأحرف الخاصة
        val escapedText = subtitle.text.replace("'", "\'").replace(":", "\:")

        // ✅ FIXED: Use assets/fonts/ directory with fallback
        val fontFile = if (subtitle.fontFamily != "default" && subtitle.fontFamily.isNotBlank()) {
            val assetFontPath = "fonts/${subtitle.fontFamily}.ttf"
            try {
                context.assets.open(assetFontPath).close()
                ":fontfile='${context.filesDir.parentFile?.absolutePath}/assets/${assetFontPath}'"
            } catch (e: Exception) {
                // Font not in assets, try to extract to files dir
                try {
                    val destFile = File(context.filesDir, "fonts/${subtitle.fontFamily}.ttf")
                    if (!destFile.exists()) {
                        destFile.parentFile?.mkdirs()
                        context.assets.open(assetFontPath).use { input ->
                            destFile.outputStream().use { output -> input.copyTo(output) }
                        }
                    }
                    if (destFile.exists()) ":fontfile='${destFile.absolutePath}'" else ""
                } catch (_: Exception) {
                    ""
                }
            }
        } else ""

        return "drawtext=text='$escapedText':fontsize=${subtitle.fontSize}:fontcolor=${subtitle.fontColor}:box=1:boxcolor=$boxColor:boxborderw=5:x=$positionX:y=$positionY$shadowSetting$fontFile"
    }
}
