package com.aiautocreate.presentation.common.state

/**
 * حالة الشاشة العامة (Loading, Success, Error).
 * @param T نوع البيانات.
 */
sealed class UiState<out T> {
    data object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}