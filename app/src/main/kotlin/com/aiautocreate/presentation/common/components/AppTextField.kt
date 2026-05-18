package com.aiautocreate.presentation.common.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * حقل نصي بتصميم AI AutoCreate.
 * @param value القيمة الحالية.
 * @param onValueChange حدث تغيير القيمة.
 * @param label العنوان العلوي.
 * @param placeholder نص توضيحي مؤقت.
 * @param leadingIcon أيقونة بداية (اختياري).
 * @param trailingIcon أيقونة نهاية (اختياري).
 * @param modifier تعديل إضافي.
 * @param enabled تفعيل/تعطيل.
 * @param isError وضع خطأ.
 * @param supportingText نص مساعد (يظهر أسفل الحقل).
 * @param singleLine سطر واحد فقط.
 * @param maxLines أقصى عدد أسطر.
 */
@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "",
    leadingIcon: ImageVector? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isError: Boolean = false,
    supportingText: String? = null,
    singleLine: Boolean = true,
    maxLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        isError = isError,
        singleLine = singleLine,
        maxLines = maxLines,
        label = { Text(text = label) },
        placeholder = if (placeholder.isNotEmpty()) {
            { Text(text = placeholder) }
        } else null,
        leadingIcon = leadingIcon?.let {
            { Icon(imageVector = it, contentDescription = label) }
        },
        trailingIcon = trailingIcon,
        supportingText = supportingText?.let {
            { Text(text = it) }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            errorBorderColor = MaterialTheme.colorScheme.error,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            cursorColor = MaterialTheme.colorScheme.primary
        ),
        shape = MaterialTheme.shapes.medium
    )
}