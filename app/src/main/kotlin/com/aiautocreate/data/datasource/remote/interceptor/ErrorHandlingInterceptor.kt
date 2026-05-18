package com.aiautocreate.data.datasource.remote.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * معترض موحّد لمعالجة أخطاء HTTP.
 * - يسجل رموز الحالة غير المتوقعة.
 * - يمكنه رمي استثناءات مخصصة أو تحويل الردود لتكون قابلة للمعالجة في الطبقات الأعلى.
 */
@Singleton
class ErrorHandlingInterceptor @Inject constructor() : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response: Response
        try {
            response = chain.proceed(request)
        } catch (e: Exception) {
            Timber.e(e, "شبكة: فشل في تنفيذ الطلب إلى ${request.url}")
            throw e
        }

        if (!response.isSuccessful) {
            val bodyString = response.peekBody(Long.MAX_VALUE).string()
            Timber.w("شبكة: خطأ ${response.code} من ${request.url}: $bodyString")
            // لا نعيد توجيه الاستثناء هنا، نترك الرد يصل إلى المستودع ليتعامل معه.
        }

        return response
    }
}