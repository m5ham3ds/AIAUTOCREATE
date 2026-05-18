package com.aiautocreate.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ModelStyleParser {

    /**
     * استخراج الأنماط المدعومة من نص README.
     * يعتمد على بحث بسيط عن كلمات مفتاحية مثل "styles:", "supported styles", "tags:".
     * يمكن لاحقاً استخدام Gemini API لتحليل أكثر دقة.
     */
    suspend fun extractStyles(readmeText: String): List<String> = withContext(Dispatchers.Default) {
        val lower = readmeText.lowercase()
        val patterns = listOf(
            Regex("styles?:\\s*([^\\n.]+)", RegexOption.IGNORE_CASE),
            Regex("supported styles?:\\s*([^\\n.]+)", RegexOption.IGNORE_CASE),
            Regex("tags?:\\s*([^\\n.]+)", RegexOption.IGNORE_CASE),
            Regex("\\b(realistic|cartoon|anime|3d|abstract|watercolor|oil painting|sketch|fantasy|sci-fi|cinematic|vintage|modern|minimalist|grunge|neon|pastel|monochrome|vibrant|muted|dark|light)\\b", RegexOption.IGNORE_CASE)
        )

        val foundStyles = mutableSetOf<String>()
        for (pattern in patterns) {
            val matches = pattern.findAll(lower)
            for (match in matches) {
                val extracted = match.groupValues.getOrNull(1)?.trim()
                if (!extracted.isNullOrBlank()) {
                    // قد يكون النص مفصولاً بفواصل أو مسافات
                    val parts = extracted.split(Regex("[,;\\n]")).map { it.trim() }.filter { it.isNotEmpty() }
                    foundStyles.addAll(parts)
                }
            }
        }
        // إذا لم نجد شيئاً، نعيد قائمة فارغة
        foundStyles.toList()
    }
}