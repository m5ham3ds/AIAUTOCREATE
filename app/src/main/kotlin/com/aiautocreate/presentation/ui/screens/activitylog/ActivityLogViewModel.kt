package com.aiautocreate.presentation.ui.screens.activitylog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aiautocreate.data.repository.ActivityLogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ActivityLogViewModel @Inject constructor(
    private val repository: ActivityLogRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ActivityLogState())
    val state: StateFlow<ActivityLogState> = _state.asStateFlow()

    init {
        loadLogs()
    }

    private fun loadLogs(isRefreshing: Boolean = false) {
        viewModelScope.launch {
            if (isRefreshing) _state.update { it.copy(isRefreshing = true) }
            repository.getAllLogs()
                .catch { e ->
                    _state.update { 
                        it.copy(
                            isLoading = false, 
                            isRefreshing = false,
                            errorMessage = "فشل تحميل السجلات: ${e.message}"
                        ) 
                    }
                }
                .collect { logs ->
                    _state.update { state ->
                        val sorted = logs.sortedByDescending { it.timestamp }
                        val filtered = applyFilter(sorted, state.selectedFilter, state.searchQuery)
                        state.copy(
                            logs = sorted,
                            filteredLogs = filtered,
                            isLoading = false,
                            isRefreshing = false,
                            errorMessage = null
                        )
                    }
                }
        }
    }

    fun refresh() {
        loadLogs(isRefreshing = true)
    }

    fun setFilter(filter: String) {
        _state.update { state ->
            val filtered = applyFilter(state.logs, filter, state.searchQuery)
            state.copy(selectedFilter = filter, filteredLogs = filtered)
        }
    }

    fun updateSearchQuery(query: String) {
        _state.update { state ->
            val filtered = applyFilter(state.logs, state.selectedFilter, query)
            state.copy(searchQuery = query, filteredLogs = filtered)
        }
    }

    private fun applyFilter(
        logs: List<com.aiautocreate.domain.model.ActivityLog>,
        filter: String,
        searchQuery: String
    ): List<com.aiautocreate.domain.model.ActivityLog> {
        var filtered = when (filter) {
            "error" -> logs.filter { it.type == "error" || !it.isSuccess }
            "warning" -> logs.filter { it.type == "api_warning" || (it.isSuccess && it.type in listOf("warning", "api_warning", "validation")) }
            "info" -> logs.filter { it.isSuccess && it.type !in listOf("error", "api_warning") }
            else -> logs
        }
        if (searchQuery.isNotBlank()) {
            filtered = filtered.filter { 
                it.title.contains(searchQuery, ignoreCase = true) ||
                it.description.contains(searchQuery, ignoreCase = true) ||
                it.type.contains(searchQuery, ignoreCase = true)
            }
        }
        return filtered
    }

    fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }
}
