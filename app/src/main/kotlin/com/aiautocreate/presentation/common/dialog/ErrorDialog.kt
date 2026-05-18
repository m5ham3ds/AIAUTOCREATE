package com.aiautocreate.presentation.common.dialog

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

/**
 * حوار خطأ بأيقونة تحذير وزر للإغلاق.
 */
@Composable
fun ErrorDialog(
    onDismissRequest: () -> Unit,
    title: String = "خطأ",
    errorMessage: String
) {
    AppDialog(
        onDismissRequest = onDismissRequest,
        title = title,
        text = errorMessage,
        icon = Icons.Filled.Error,
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("حسناً")
            }
        }
    )
}