package com.aiautocreate.domain.service

/**
 * واجهة تتبع التحليلات.
 */
interface AnalyticsTracker {
    fun logEvent(eventName: String, params: Map<String, Any>? = null)
    fun setUserProperty(name: String, value: String)
    fun logError(error: Throwable, message: String? = null)
}