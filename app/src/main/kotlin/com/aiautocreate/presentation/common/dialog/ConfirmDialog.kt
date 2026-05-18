package com.aiautocreate.presentation.common.dialog

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun ConfirmDialog(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
    title: String,
    message: String,
    confirmText: String = "موافق",
    dismissText: String = "إلغاء"
) {
    AppDialog(
        onDismissRequest = onDismissRequest,
        title = title,
        text = message,
        confirmButton = {
            Button(
                onClick = {
                    onDismissRequest()
                    onConfirm()
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) { Text(confirmText) }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) { Text(dismissText) }
        }
    )
}
