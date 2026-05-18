package com.aiautocreate.util

import java.util.Locale
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

/**
 * مساعد لتغيير لغة التطبيق بشكل فوري.
 */
object LocaleHelper {

    /**
     * يطبق اللغة المختارة على مستوى التطبيق بالكامل.
     * @param languageCode رمز اللغة ("ar", "en").
     */
    fun applyLanguage(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        AppCompatDelegate.setApplicationLocales(
            LocaleListCompat.create(locale)
        )
        // ملاحظة: النشاط الحالي سيُعاد بناؤه تلقائياً إذا استخدمنا NavGraph مع Locale مدمج،
        // أو يمكن إعادة تشغيله يدويًا حسب الحاجة.
    }
}