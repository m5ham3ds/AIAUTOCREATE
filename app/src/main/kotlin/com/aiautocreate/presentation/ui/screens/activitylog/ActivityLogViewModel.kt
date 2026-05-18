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

    private fun loadLogs() {
        viewModelScope.launch {
            repository.getAllLogs()
                .catch { e ->
                    _state.update { it.copy(isLoading = false, errorMessage = "فشل تحميل السجلات: ${e.message}") }
                }
                .collect { logs ->
                    _state.update { state ->
                        val sorted = logs.sortedByDescending { it.timestamp }
                        val filtered = applyFilter(sorted, state.selectedFilter)
                        state.copy(logs = sorted, filteredLogs = filtered, isLoading = false, errorMessage = null)
                    }
                }
        }
    }

    fun setFilter(filter: String) {
        _state.update { state ->
            val filtered = applyFilter(state.logs, filter)
            state.copy(selectedFilter = filter, filteredLogs = filtered)
        }
    }

    private fun applyFilter(logs: List<com.aiautocreate.domain.model.ActivityLog>, filter: String): List<com.aiautocreate.domain.model.ActivityLog> {
        return when (filter) {
            "error" -> logs.filter { it.type == "error" || !it.isSuccess }
            "warning" -> logs.filter { it.type == "api_warning" || (it.isSuccess && it.type in listOf("warning", "api_warning", "validation")) }
            "info" -> logs.filter { it.isSuccess && it.type !in listOf("error", "api_warning") }
            else -> logs // "all"
        }
    }

    fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }
}