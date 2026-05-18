package com.aiautocreate.domain.model

data class MontagePlan(
    val inputFiles: List<MontageInput>,
    val transitions: List<MontageTransition>,
    val overlays: List<MontageOverlay>,
    val audioTracks: List<MontageAudio>,
    val outputSettings: OutputSettings,
    val subtitle: SubtitleStyle? = null  // ✅ إضافة الترجمة
) {
    data class MontageInput(
        val path: String,
        val type: String,
        val durationMs: Long,
        val startMs: Long = 0
    )

    data class MontageTransition(
        val fromIndex: Int,
        val toIndex: Int,
        val type: String,
        val durationMs: Long
    )

    data class MontageOverlay(
        val type: String,
        val content: String,
        val startMs: Long,
        val durationMs: Long,
        val position: String,
        val fontSize: Int = 24,
        val fontColor: String = "white"
    )

    data class MontageAudio(
        val path: String,
        val startMs: Long,
        val durationMs: Long = 0L,
        val volume: Double = 1.0,
        val fadeInMs: Long = 0,
        val fadeOutMs: Long = 0
    )

    data class OutputSettings(
        val width: Int,
        val height: Int,
        val fps: Int,
        val aspectRatio: String,
        val quality: String,
        val outputPath: String
    )

    // ✅ بيانات الترجمة من شاشة SubtitleStyle
    data class SubtitleStyle(
        val text: String,               // النص المعروض
        val fontSize: Int,              // حجم الخط (px)
        val fontColor: String,          // لون النص (Hex)
        val backgroundColor: String,    // لون الخلفية (Hex)
        val backgroundOpacity: Int,     // 0-100
        val position: String,           // "top", "center", "bottom"
        val shadow: String,             // "بدون", "خفيف", "قوي"
        val fontFamily: String = "default"
    )
}