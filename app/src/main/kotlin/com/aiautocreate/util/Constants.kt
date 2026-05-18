package com.aiautocreate.util

object Constants {
    const val APP_TAG = "AIAutoCreate"
    const val DATABASE_NAME = "aiautocreate.db"
    const val DATASTORE_NAME = "aiautocreate_settings"
    const val SECURE_PREFS_NAME = "aiautocreate_secure_prefs"

    const val GEMINI_API_KEY_PLACEHOLDER = "PLACEHOLDER"
    const val HF_API_KEY_PLACEHOLDER = "PLACEHOLDER"

    const val MAX_VIDEO_DURATION_MS = 300_000L  // 5 دقائق
    const val MAX_IMAGE_DIMENSION = 1024
    const val DEFAULT_JPEG_QUALITY = 85

    const val WORK_NAME_VIDEO_CREATION = "video_creation"
    const val WORK_NAME_SYNC = "data_sync"
    const val WORK_NAME_ANALYTICS = "analytics_upload"
}