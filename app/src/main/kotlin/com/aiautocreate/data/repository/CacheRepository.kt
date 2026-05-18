package com.aiautocreate.data.repository

import com.aiautocreate.domain.repository.ICacheRepository
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CacheRepository @Inject constructor() : ICacheRepository {

    private val cache = ConcurrentHashMap<String, CacheEntry<*>>()

    @Suppress("UNCHECKED_CAST")
    override suspend fun <T : Any> getCached(key: String, clazz: Class<T>): T? {
        val entry = cache[key] ?: return null
        if (System.currentTimeMillis() - entry.timestamp > entry.maxAgeMs) {
            cache.remove(key)
            return null
        }
        return entry.data as? T
    }

    // تم دمج الدالتين في دالة واحدة تطابق الواجهة تماماً
    override suspend fun <T : Any> cache(key: String, data: T, maxAgeMs: Long) {
        cache[key] = CacheEntry(data = data, timestamp = System.currentTimeMillis(), maxAgeMs = maxAgeMs)
    }

    override suspend fun clearCache(key: String) {
        cache.remove(key)
    }

    override suspend fun clearAllCache() {
        cache.clear()
    }

    override suspend fun isCacheValid(key: String, maxAgeMillis: Long): Boolean {
        val entry = cache[key] ?: return false
        return System.currentTimeMillis() - entry.timestamp <= maxAgeMillis
    }

    private data class CacheEntry<T : Any>(
        val data: T,
        val timestamp: Long,
        val maxAgeMs: Long = Long.MAX_VALUE
    )
}
