package com.aiautocreate.presentation.common.state

/**
 * أحداث واجهة المستخدم التي تُمرر من ViewModel إلى View.
 */
sealed class UiEvent {
    data class ShowSnackbar(val message: String, val isError: Boolean = false) : UiEvent()
    data class Navigate(val route: Any) : UiEvent()
    data object GoBack : UiEvent()
}