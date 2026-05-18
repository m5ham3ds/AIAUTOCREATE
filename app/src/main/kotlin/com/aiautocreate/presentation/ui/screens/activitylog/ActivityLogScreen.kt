package com.aiautocreate.presentation.ui.screens.activitylog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aiautocreate.R
import com.aiautocreate.domain.model.ActivityLog
import com.aiautocreate.presentation.common.components.*
import com.aiautocreate.presentation.ui.theme.*
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun ActivityLogScreen(
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ActivityLogViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundMain)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // مسافة علوية بسيطة للتباعد عن الهيدر
            Spacer(modifier = Modifier.height(Spacing.md))

            // بطاقة الفلترة
            FilterChipsRow(state, viewModel)

            Spacer(modifier = Modifier.height(Spacing.md))

            // عرض السجلات
            if (state.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(AppSpecific.emptyStateHeight)
                        .padding(horizontal = Spacing.lg),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Primary)
                }
            } else if (state.filteredLogs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(AppSpecific.emptyStateHeight)
                        .padding(horizontal = Spacing.lg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "لا توجد سجلات",
                        color = TextHint,
                        fontSize = AppFontSize.bodyMedium
                    )
                }
            } else {
                state.filteredLogs.forEach { log ->
                    when {
                        log.type == "error" || !log.isSuccess -> LogErrorCard(log)
                        log.type == "api_warning" -> LogWarningCard(log)
                        else -> LogInfoCard(log)
                    }
                    Spacer(modifier = Modifier.height(Spacing.md))
                }
            }

            // ✅ تم إزالة Spacer السفلي الثابت (يتم ضبطه عبر Scaffold في MainActivity)
        }
    }
}

@Composable
private fun FilterChipsRow(state: ActivityLogState, viewModel: ActivityLogViewModel) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg)
            .clip(RoundedCornerShape(Radius.xxl))
            .background(CardPrimary)
            .padding(Spacing.lg)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm) // ✅ إضافة مسافة بين الشرائح
        ) {
            FilterChip(
                selected = state.selectedFilter == "all",
                onClick = { viewModel.setFilter("all") },
                label = { Text("الكل") },
                modifier = Modifier.weight(1f).height(ComponentSize.buttonHeight),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Primary,
                    selectedLabelColor = TextPrimary
                )
            )
            FilterChip(
                selected = state.selectedFilter == "info",
                onClick = { viewModel.setFilter("info") },
                label = { Text("Info") },
                modifier = Modifier.weight(1f).height(ComponentSize.buttonHeight),
                trailingIcon = {
                    Box(
                        modifier = Modifier
                            .size(AppSpecific.filterDotSize)
                            .clip(RoundedCornerShape(Radius.round))
                            .background(AccentBlue)
                    )
                }
            )
            FilterChip(
                selected = state.selectedFilter == "warning",
                onClick = { viewModel.setFilter("warning") },
                label = { Text("Warning") },
                modifier = Modifier.weight(1f).height(ComponentSize.buttonHeight),
                trailingIcon = {
                    Box(
                        modifier = Modifier
                            .size(AppSpecific.filterDotSize)
                            .clip(RoundedCornerShape(Radius.round))
                            .background(Color(0xFFF8A8C5))
                    )
                }
            )
            FilterChip(
                selected = state.selectedFilter == "error",
                onClick = { viewModel.setFilter("error") },
                label = { Text("Error") },
                modifier = Modifier.weight(1f).height(ComponentSize.buttonHeight),
                trailingIcon = {
                    Box(
                        modifier = Modifier
                            .size(AppSpecific.filterDotSize)
                            .clip(RoundedCornerShape(Radius.round))
                            .background(Color(0xFFFFD6D6))
                    )
                }
            )
        }
    }
}

@Composable
private fun LogInfoCard(log: ActivityLog) {
    val timestamp = remember(log.timestamp) {
        val instant = Instant.ofEpochMilli(log.timestamp)
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault())
            .format(instant)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg)
            .clip(RoundedCornerShape(Radius.xxl))
            .background(CardPrimary)
            .padding(Spacing.md) // ✅ تقليل padding الداخلي من Spacing.lg إلى md
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .clip(RoundedCornerShape(Radius.sm))
                        .background(CardSoft)
                        .padding(horizontal = Spacing.sm, vertical = Spacing.xs)
                ) {
                    Text(
                        text = "[${log.type.uppercase()}]",
                        color = TextHint,
                        fontSize = AppFontSize.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = timestamp,
                    modifier = Modifier.padding(start = Spacing.md),
                    color = TextHint,
                    fontSize = AppFontSize.bodySmall
                )
                Spacer(Modifier.weight(1f))
                Box(
                    Modifier
                        .size(Spacing.sm) // ✅ تصغير الحجم من md إلى sm
                        .clip(RoundedCornerShape(Radius.round))
                        .background(AccentBlue)
                )
            }
            Spacer(Modifier.height(Spacing.md)) // ✅ تقليل المسافة من lg إلى md
            Text(
                text = log.title,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = TextPrimary,
                fontSize = AppFontSize.titleSmall, // ✅ تصغير حجم الخط من titleMedium إلى titleSmall
                fontWeight = FontWeight.Bold
            )
            if (log.description.isNotBlank()) {
                Spacer(Modifier.height(Spacing.sm))
                Text(
                    text = log.description,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = TextBody,
                    fontSize = AppFontSize.bodySmall // ✅ تصغير حجم الخط من bodyMedium إلى bodySmall
                )
            }
        }
    }
}

@Composable
private fun LogWarningCard(log: ActivityLog) {
    val timestamp = remember(log.timestamp) {
        val instant = Instant.ofEpochMilli(log.timestamp)
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault())
            .format(instant)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg)
            .clip(RoundedCornerShape(Radius.xxl))
            .background(CardPrimary)
            .padding(Spacing.md) // ✅ تقليل padding الداخلي
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .clip(RoundedCornerShape(Radius.sm))
                        .background(CardSoft)
                        .padding(horizontal = Spacing.sm, vertical = Spacing.xs)
                ) {
                    Text(
                        text = "[${log.type.uppercase()}]",
                        color = TextHint,
                        fontSize = AppFontSize.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = timestamp,
                    modifier = Modifier.padding(start = Spacing.md),
                    color = TextHint,
                    fontSize = AppFontSize.bodySmall
                )
                Spacer(Modifier.weight(1f))
                Box(
                    Modifier
                        .size(Spacing.sm) // ✅ تصغير الحجم
                        .clip(RoundedCornerShape(Radius.round))
                        .background(Color(0xFFF8A8C5))
                )
            }
            Spacer(Modifier.height(Spacing.md))
            Text(
                text = log.title,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = TextPrimary,
                fontSize = AppFontSize.titleSmall,
                fontWeight = FontWeight.Bold
            )
            if (log.description.isNotBlank()) {
                Spacer(Modifier.height(Spacing.sm))
                Text(
                    text = log.description,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = TextBody,
                    fontSize = AppFontSize.bodySmall
                )
            }
        }
    }
}

@Composable
private fun LogErrorCard(log: ActivityLog) {
    var expanded by remember { mutableStateOf(false) }
    val timestamp = remember(log.timestamp) {
        val instant = Instant.ofEpochMilli(log.timestamp)
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault())
            .format(instant)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg)
            .clip(RoundedCornerShape(Radius.xxl))
            .background(CardPrimary)
            .clickable { expanded = !expanded }
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.md), // ✅ تقليل padding من lg إلى md
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(
                        id = if (expanded) R.drawable.ic_keyboard_arrow_up
                        else R.drawable.ic_keyboard_arrow_down
                    ),
                    contentDescription = "Expand",
                    modifier = Modifier.size(IconSize.sm), // ✅ تصغير حجم الأيقونة
                    tint = TextHint
                )
                Box(
                    modifier = Modifier
                        .padding(start = Spacing.md)
                        .clip(RoundedCornerShape(Radius.sm))
                        .background(CardSoft)
                        .padding(horizontal = Spacing.sm, vertical = Spacing.xs)
                ) {
                    Text(
                        text = "[${log.type.uppercase()}]",
                        color = TextHint,
                        fontSize = AppFontSize.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = timestamp,
                    modifier = Modifier.padding(start = Spacing.md),
                    color = TextHint,
                    fontSize = AppFontSize.bodySmall
                )
                Spacer(modifier = Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .size(Spacing.sm)
                        .clip(RoundedCornerShape(Radius.round))
                        .background(Color(0xFFFFD6D6))
                )
            }
            Text(
                text = log.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.lg),
                textAlign = TextAlign.Center,
                color = TextPrimary,
                fontSize = AppFontSize.titleSmall,
                fontWeight = FontWeight.Bold
            )
            if (log.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(Spacing.sm))
                Text(
                    text = log.description,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.lg),
                    textAlign = TextAlign.Center,
                    color = TextBody,
                    fontSize = AppFontSize.bodySmall
                )
            }
            if (expanded && log.description.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = Spacing.md) // ✅ تقليل من lg إلى md
                        .clip(RoundedCornerShape(bottomStart = Radius.xxl, bottomEnd = Radius.xxl))
                        .background(CardSoft)
                        .padding(Spacing.md),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = log.description,
                        modifier = Modifier.fillMaxWidth(),
                        fontFamily = FontFamily.Monospace,
                        lineHeight = AppFontSize.titleMedium.value.sp, // ✅ تقليل lineHeight
                        color = TextPrimary,
                        fontSize = AppFontSize.bodySmall
                    )
                }
            }
        }
    }
}
