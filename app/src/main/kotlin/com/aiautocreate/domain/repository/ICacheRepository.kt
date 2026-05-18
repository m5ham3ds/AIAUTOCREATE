package com.aiautocreate.domain.repository

interface ICacheRepository {
    suspend fun <T : Any> cache(key: String, data: T, maxAgeMs: Long = Long.MAX_VALUE)
    suspend fun <T : Any> getCached(key: String, clazz: Class<T>): T?
    suspend fun clearCache(key: String)
    suspend fun clearAllCache()
    suspend fun isCacheValid(key: String, maxAgeMillis: Long): Boolean
}
