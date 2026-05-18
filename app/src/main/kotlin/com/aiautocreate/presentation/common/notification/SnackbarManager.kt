package com.aiautocreate.presentation.common.notification

import androidx.compose.material3.SnackbarDuration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SnackbarManager @Inject constructor() {

    private val _notifications = MutableSharedFlow<InAppNotification>()
    val notifications = _notifications.asSharedFlow()

    fun show(
        message: String,
        type: NotificationType = NotificationType.INFO,
        actionLabel: String? = null,
        duration: SnackbarDuration = SnackbarDuration.Short
    ) {
        CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
            _notifications.emit(
                InAppNotification(
                    message = message,
                    type = type,
                    actionLabel = actionLabel,
                    duration = duration
                )
            )
        }
    }

    fun showSuccess(message: String) = show(message, NotificationType.SUCCESS)
    fun showError(message: String) = show(message, NotificationType.ERROR, duration = SnackbarDuration.Long)
    fun showInfo(message: String) = show(message, NotificationType.INFO)
}
