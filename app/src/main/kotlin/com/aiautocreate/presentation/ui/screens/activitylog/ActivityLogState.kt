package com.aiautocreate.presentation.ui.screens.activitylog

import com.aiautocreate.domain.model.ActivityLog

data class ActivityLogState(
    val logs: List<ActivityLog> = emptyList(),
    val filteredLogs: List<ActivityLog> = emptyList(),
    val selectedFilter: String = "all", // "all", "info", "warning", "error"
    val searchQuery: String = "",        // ✅ جديد
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,   // ✅ جديد
    val errorMessage: String? = null
)
