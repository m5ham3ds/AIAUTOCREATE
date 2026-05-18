package com.aiautocreate.presentation.ui.screens.home

data class HomeState(
    // حالة الاتصال
    val connectionStatus: String = "جارٍ التحقق...",
    val isConnected: Boolean = false,

    // القوائم المنسدلة (قيم مختارة + الخيارات المتاحة)
    val selectedImageStyle: String = "واقعي",
    val selectedCoverStyle: String = "غلاف بسيط",
    val selectedVoice: String = "صوت 1",
    val selectedVideoStyle: String = "درامي",
    val selectedMontageStyle: String = "قصص وروايات",

    // ✅ سيتم تعبئة هذه القوائم ديناميكياً من النماذج النشطة
    val imageStyles: List<String> = emptyList(),
    val coverStyles: List<String> = emptyList(),   // قد يبقى من CSV أو يتم دمجه مع أنماط الصور
    val voiceOptions: List<String> = emptyList(),
    val videoStyles: List<String> = emptyList(),
    val montageStyles: List<String> = emptyList(),

    // حقل الإدخال
    val promptText: String = "",

    // السجلات واخراج الوسائط
    val logs: List<String> = emptyList(),
    val outputVideoPath: String? = null,

    // حالة المعالجة
    val isProcessing: Boolean = false,
    val progress: Float = 0f,
    val progressText: String = "0%",

    // عام
    val errorMessage: String? = null,
    val isLoading: Boolean = true
)