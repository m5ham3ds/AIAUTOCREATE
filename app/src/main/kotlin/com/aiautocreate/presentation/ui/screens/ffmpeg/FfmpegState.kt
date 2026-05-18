package com.aiautocreate.presentation.ui.screens.ffmpeg

data class FfmpegState(
    // نمط المونتاج
    val selectedMontageStyle: String = "قصص وروايات",
    val montageStyles: List<String> = listOf(
        "قصص وروايات", "حماسي وجذاب", "احترافية وأنيق", "مخصص"
    ),

    // مدة الفيديو
    val videoMinutes: Int = 1,
    val videoSeconds: Int = 30,

    // الجودة ونسبة العرض
    val selectedQuality: String = "1080p",
    val qualities: List<String> = listOf("480p", "720p", "1080p", "2k", "4k"),
    val selectedAspectRatio: String = "16:9",

    // FPS
    val selectedFps: String = "30",

    // مفاتيح AI Pipeline (تفعيل/تعطيل المهام)
    val isMasterModelEnabled: Boolean = false,
    val isAudioFxEnabled: Boolean = false,
    val isVisualFxEnabled: Boolean = false,
    val isTransitionsEnabled: Boolean = false,
    val isSmartCountEnabled: Boolean = false,
    val isSubtitlesEnabled: Boolean = false,
    val isMusicEnabled: Boolean = false,
    val isReviewerEnabled: Boolean = false,
    val isMasterOrchestratorEnabled: Boolean = false,

    // ✅ العناصر الجديدة – ملحقات خارجية
    val isExternalVideoEnabled: Boolean = false,   // إضافة فيديوهات خارجية
    val isExternalImageEnabled: Boolean = false,   // إضافة صور خارجية

    // النماذج المختارة لكل مهمة
    val masterModelId: String = "",
    val audioFxModelId: String = "",
    val visualFxModelId: String = "",
    val transitionsModelId: String = "",
    val subtitlesModelId: String = "",
    val musicModelId: String = "",
    val reviewerModelId: String = "",
    val orchestratorModelId: String = "",

    // ملخص الإخراج
    val outputResolution: String = "",
    val outputFormat: String = "MP4 (H.264)",
    val outputEstimatedSize: String = "",

    // حالة الحفظ
    val isSaving: Boolean = false,
    val saveSuccessMessage: String? = null,
    val errorMessage: String? = null
)