package com.aiautocreate.presentation.common.state

import com.google.firebase.crashlytics.FirebaseCrashlytics
import retrofit2.HttpException
import java.io.IOException

/**
 * معالج الأخطاء الموحد: يحول Throwable إلى رسالة نصية ويُسجلها في Crashlytics.
 */
object ErrorHandler {

    fun getMessage(throwable: Throwable): String {
        return when (throwable) {
            is HttpException -> "خطأ في الخادم (${throwable.code()})"
            is IOException -> "تعذر الاتصال بالإنترنت"
            else -> throwable.message ?: "حدث خطأ غير معروف"
        }
    }

    fun logError(throwable: Throwable, tag: String = "AIAutoCreate") {
        FirebaseCrashlytics.getInstance().apply {
            log("[$tag] ${getMessage(throwable)}")
            recordException(throwable)
        }
    }
}