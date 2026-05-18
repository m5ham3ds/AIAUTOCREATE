package com.aiautocreate.presentation.common.dialog

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * حوار عام قابل للتخصيص يُستخدم كأساس للحوارات الأخرى.
 */
@Composable
fun AppDialog(
    onDismissRequest: () -> Unit,
    title: String? = null,
    text: String? = null,
    icon: ImageVector? = null,
    confirmButton: @Composable (() -> Unit)? = null,
    dismissButton: @Composable (() -> Unit)? = null
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        icon = icon?.let {
            { Icon(it, contentDescription = null) }
        },
        title = title?.let { { Text(it) } },
        text = text?.let { { Text(it) } },
        confirmButton = confirmButton ?: {},
        dismissButton = dismissButton ?: {}
    )
}