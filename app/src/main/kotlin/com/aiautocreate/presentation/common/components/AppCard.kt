package com.aiautocreate.presentation.common.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.aiautocreate.presentation.ui.theme.ComponentSize
import com.aiautocreate.presentation.ui.theme.Spacing

@Composable
fun AppCard(
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    elevation: Dp = ComponentSize.cardElevation,
    contentPadding: Dp = Spacing.md,
    content: @Composable () -> Unit
) {
    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier,
            elevation = CardDefaults.cardElevation(defaultElevation = elevation),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier.padding(contentPadding)
            ) {
                content()
            }
        }
    } else {
        Card(
            modifier = modifier,
            elevation = CardDefaults.cardElevation(defaultElevation = elevation),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier.padding(contentPadding)
            ) {
                content()
            }
        }
    }
}
