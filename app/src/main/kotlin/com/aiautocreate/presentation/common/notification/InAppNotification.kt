package com.aiautocreate.presentation.common.notification

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * نموذج بياني موحّد لإشعار داخل التطبيق (Snackbar Visuals).
 */
data class InAppNotification(
    override val message: String,
    override val actionLabel: String? = null,
    override val duration: SnackbarDuration = SnackbarDuration.Short,
    override val withDismissAction: Boolean = false,
    val type: NotificationType = NotificationType.INFO,
    val icon: ImageVector? = null
) : SnackbarVisuals {
    val iconToShow: ImageVector
        get() = icon ?: when (type) {
            NotificationType.SUCCESS -> Icons.Filled.CheckCircle
            NotificationType.ERROR -> Icons.Filled.Error
            NotificationType.INFO -> Icons.Filled.Info
        }
}

enum class NotificationType { SUCCESS, ERROR, INFO }