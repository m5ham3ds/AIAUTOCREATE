package com.aiautocreate.domain.service

import com.aiautocreate.domain.model.MontagePlan
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FFmpegCommandBuilder @Inject constructor() {

    fun buildCommand(plan: MontagePlan): String {
        val sb = StringBuilder()

        // 1. إدخال الملفات
        plan.inputFiles.forEach { input ->
            when (input.type) {
                "video" -> sb.append("-i \"${input.path}\" ")
                "image" -> sb.append("-loop 1 -t ${input.durationMs / 1000} -i \"${input.path}\" ")
            }
        }

        // 2. إدخال الصوت
        plan.audioTracks.forEach { audio ->
            sb.append("-i \"${audio.path}\" ")
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
        if (plan.transitions.isNotEmpty()) {
            val transitionFilter = buildTransitionFilters(plan, filterParts)
            filterParts.clear()
            filterParts.add(transitionFilter)
        } else {
            // بدون انتقالات: وصل مباشر
            val allVideos = plan.inputFiles.indices.joinToString(" ") { "[v$it]" }
            filterParts.add("${allVideos}concat=n=${plan.inputFiles.size}:v=1:a=0[v_out]")
        }

        // إضافة التراكبات (نصوص، علامات مائية)
        plan.overlays.forEach { overlay ->
            when (overlay.type) {
                "text" -> {
                    val position = when (overlay.position) {
                        "center" -> "x=(w-text_w)/2:y=(h-text_h)/2"
                        "top_center" -> "x=(w-text_w)/2:y=10"
                        "bottom_center" -> "x=(w-text_w)/2:y=h-th-10"
                        else -> "x=(w-text_w)/2:y=(h-text_h)/2"
                    }
                    filterParts.add(
                        "drawtext=text='${overlay.content}':fontsize=${overlay.fontSize}:fontcolor=${overlay.fontColor}:$position:enable='between(t,${overlay.startMs / 1000.0},${(overlay.startMs + overlay.durationMs) / 1000.0})'"
                    )
                }
            }
        }

        // معالجة الصوت (دمج، ضبط مستوى، تلاشي)
        val audioFilters = mutableListOf<String>()
        plan.audioTracks.forEachIndexed { index, audio ->
            val inputIndex = plan.inputFiles.size + index
            audioFilters.add("[${inputIndex}:a]adelay=${audio.startMs}|${audio.startMs},volume=${audio.volume}")
            if (audio.fadeInMs > 0 || audio.fadeOutMs > 0) {
                audioFilters.add("afade=t=in:st=${audio.startMs / 1000.0}:d=${audio.fadeInMs / 1000.0}")
                audioFilters.add("afade=t=out:st=${(audio.startMs + audio.durationMs - audio.fadeOutMs) / 1000.0}:d=${audio.fadeOutMs / 1000.0}")
            }
            audioFilters.add("[a$index]")
        }

        // ✅ إضافة فلتر الترجمة إذا وُجدت
        val finalVideoLabel = if (plan.subtitle != null) {
            val durationTotal = plan.inputFiles.sumOf { it.durationMs } / 1000.0
            val subtitleFilter = buildSubtitleFilter(plan.subtitle, durationTotal)
            // نضيف الفلتر قبل أن يصبح [v_out] هو المخرج النهائي
            // نطبق الفلتر على [v_out] ونخرج [v_final]
            filterParts.add("$subtitleFilter[v_final]")
            "[v_final]"
        } else {
            "[v_out]"
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
            sb.append("-filter_complex \"")
            sb.append(allFilters.joinToString(";"))
            sb.append("\" ")
        }

        // خريطة الإخراج - استخدام المخرج النهائي المعدل (مع الترجمة إذا وجدت)
        sb.append("-map \"$finalVideoLabel\" ")
        if (audioFilters.isNotEmpty()) {
            sb.append("-map \"[a_out]\" ")
        }

        // إعدادات التصدير
        sb.append("-r ${plan.outputSettings.fps} ")
        sb.append("-c:v libx264 -crf 18 -preset medium ") // جودة عالية
        sb.append("-pix_fmt yuv420p ")

        // مسار المخرج
        sb.append("\"${plan.outputSettings.outputPath}\" -y")

        return sb.toString().trim()
    }

    private fun buildTransitionFilters(plan: MontagePlan, existingFilters: List<String>): String {
        // بناء انتقال سلس بين المقاطع باستخدام Xfade أو Overlay
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

    // ✅ دالة بناء فلتر الترجمة (drawtext)
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
        val escapedText = subtitle.text.replace("'", "\\'").replace(":", "\\:")

        // يمكن إضافة خط مخصص عبر fontfile (اختياري)
        val fontFile = if (subtitle.fontFamily != "default" && subtitle.fontFamily.isNotBlank()) {
            val fontPath = when (subtitle.fontFamily) {
                "Cairo" -> "/system/fonts/Cairo.ttf"
                "Amiri" -> "/system/fonts/Amiri.ttf"
                "Tajawal" -> "/system/fonts/Tajawal.ttf"
                "Rubik" -> "/system/fonts/Rubik.ttf"
                else -> ""
            }
            if (fontPath.isNotEmpty()) ":fontfile='$fontPath'" else ""
        } else ""

        return "[v_out]drawtext=text='$escapedText':fontsize=${subtitle.fontSize}:fontcolor=${subtitle.fontColor}:box=1:boxcolor=$boxColor:boxborderw=5:x=$positionX:y=$positionY$shadowSetting$fontFile"
    }
}