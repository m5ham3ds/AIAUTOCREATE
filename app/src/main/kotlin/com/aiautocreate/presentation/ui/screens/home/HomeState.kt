package com.aiautocreate.presentation.ui.screens.home

data class HomeState(
    // حالة الاتصال
    val connectionStatus: String = "جارٍ التحقق...",
    val isConnected: Boolean = false,

    // القوائم المنسدلة (قيم مختارة + الخيارات المتاحة)
    val selectedMontageStyle: String = "قصص وروايات",
    val selectedImageStyle: String = "واقعي",
    val selectedCoverStyle: String = "غلاف بسيط",
    val selectedVoice: String = "صوت 1",
    val selectedVideoStyle: String = "درامي",

    val montageStyles: List<String> = emptyList(),
    val imageStyles: List<String> = emptyList(),
    val coverStyles: List<String> = emptyList(),
    val voiceOptions: List<String> = emptyList(),
    val videoStyles: List<String> = emptyList(),

    // حقل الإدخال
    val promptText: String = "",

    // السجلات واخراج الوسائط
    val logs: List<String> = emptyList(),
    val outputVideoPath: String? = null,

    // حالة المعالجة
    val isProcessing: Boolean = false,
    val progress: Float = 0f,
    val progressText: String = "0%",

    // ✅ حوار إلغاء العملية
    val showCancelDialog: Boolean = false,

    // عام
    val errorMessage: String? = null,
    val isLoading: Boolean = true
)
