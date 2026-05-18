package com.aiautocreate.presentation.ui.screens.agent

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aiautocreate.R
import com.aiautocreate.presentation.common.components.*
import com.aiautocreate.presentation.ui.theme.*
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun AgentScreen(
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AgentViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.chatError) {
        state.chatError?.let { snackbarHostState.showSnackbar(it) }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundMain)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // مسافة علوية بسيطة للتباعد عن الهيدر
            Spacer(modifier = Modifier.height(Spacing.md))

            // وصف الخدمة
            Text(
                "مركز التحكم والمراقبة الذكي للمشروع",
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.lg, vertical = Spacing.xxs),
                textAlign = TextAlign.Center,
                color = TextHint,
                fontSize = AppFontSize.bodyMedium
            )

            Spacer(Modifier.height(Spacing.md))

            // التبويبات
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.lg),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                AgentTab("الدردشة", 0, state.selectedTab) { viewModel.onTabSelected(0) }
                AgentTab("سجل التدخلات", 1, state.selectedTab) { viewModel.onTabSelected(1) }
                AgentTab("الصلاحيات", 2, state.selectedTab) { viewModel.onTabSelected(2) }
            }

            Spacer(Modifier.height(Spacing.md))

            // محتوى التبويب
            when (state.selectedTab) {
                0 -> ChatTab(state, viewModel, listState)
                1 -> InterventionsTab(state)
                2 -> PermissionsTab(state, viewModel)
            }

            // ✅ تم إزالة Spacer السفلي الثابت (يتم ضبطه عبر Scaffold في MainActivity)
        }
    }
}

@Composable
private fun RowScope.AgentTab(text: String, tabIndex: Int, selectedTab: Int, onClick: () -> Unit) {
    val isSelected = selectedTab == tabIndex
    Box(
        Modifier
            .weight(1f)
            .height(ComponentSize.buttonHeight)
            .clip(RoundedCornerShape(Radius.md))
            .background(
                if (isSelected) Brush.horizontalGradient(listOf(Primary, PrimaryLight))
                else Brush.horizontalGradient(listOf(CardSoft, CardSoft))
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            color = if (isSelected) TextPrimary else TextHint,
            fontSize = AppFontSize.bodyMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun ChatTab(state: AgentState, viewModel: AgentViewModel, listState: LazyListState) {
    Column(Modifier.fillMaxSize()) {
        LazyColumn(
            Modifier
                .weight(1f)
                .padding(horizontal = Spacing.lg),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
            contentPadding = PaddingValues(vertical = Spacing.md)
        ) {
            items(state.chatMessages) { message -> ChatBubble(message) }
            if (state.isChatLoading) {
                item {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = Spacing.sm),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Box(
                            Modifier
                                .clip(RoundedCornerShape(Radius.lg))
                                .background(CardSecondary)
                                .padding(Spacing.md)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    color = PrimaryLight,
                                    modifier = Modifier.size(IconSize.sm),
                                    strokeWidth = Border.thick
                                )
                                Spacer(Modifier.width(Spacing.sm))
                                Text("الوكيل يكتب...", color = TextHint, fontSize = AppFontSize.bodySmall)
                            }
                        }
                    }
                }
            }
        }

        Box(
            Modifier
                .fillMaxWidth()
                .background(BackgroundMain)
                .padding(horizontal = Spacing.lg, vertical = Spacing.md)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = state.inputText,
                    onValueChange = { viewModel.onInputChanged(it) },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("اسأل الوكيل...", color = TextHint) },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = TextPrimary),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryLight,
                        unfocusedBorderColor = BorderInput,
                        cursorColor = PrimaryLight
                    ),
                    shape = RoundedCornerShape(Radius.xxl),
                    singleLine = false,
                    maxLines = 3
                )
                Spacer(Modifier.width(Spacing.md))
                IconButton(
                    onClick = { viewModel.sendMessage() },
                    enabled = state.inputText.isNotBlank() && !state.isChatLoading,
                    modifier = Modifier
                        .size(ComponentSize.fabSize)
                        .clip(RoundedCornerShape(Radius.round))
                        .background(Primary)
                ) {
                    Icon(
                        painterResource(R.drawable.ic_send),
                        "إرسال",
                        tint = TextPrimary,
                        modifier = Modifier.size(IconSize.md)
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(message: ChatMessage) {
    val alignment = if (message.isUser) Arrangement.End else Arrangement.Start
    val bgColor = if (message.isUser) Primary.copy(alpha = 0.3f) else CardSecondary
    val textColor = if (message.isUser) PrimaryLight else TextBody
    Row(Modifier.fillMaxWidth(), horizontalArrangement = alignment) {
        Box(
            Modifier
                .widthIn(max = AppSpecific.chatBubbleMaxWidth)
                .clip(RoundedCornerShape(Radius.lg))
                .background(bgColor)
                .padding(Spacing.md)
        ) {
            Text(
                message.text,
                color = textColor,
                fontSize = AppFontSize.bodySmall,
                lineHeight = AppFontSize.titleMedium.value.sp
            )
        }
    }
}

@Composable
private fun InterventionsTab(state: AgentState) {
    if (state.interventions.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("لا توجد تدخلات مسجلة", color = TextHint, fontSize = AppFontSize.bodyMedium)
        }
    } else {
        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(horizontal = Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
            contentPadding = PaddingValues(vertical = Spacing.md)
        ) {
            items(state.interventions) { intervention -> InterventionCard(intervention) }
        }
    }
}

@Composable
private fun InterventionCard(intervention: AgentIntervention) {
    val statusIcon = when (intervention.status) {
        "success" -> R.drawable.ic_check_circle
        "warning" -> R.drawable.ic_warning
        "failed" -> R.drawable.ic_error
        else -> R.drawable.ic_info
    }
    val statusColor = when (intervention.status) {
        "success" -> SuccessGreen
        "warning" -> WarningOrange
        "failed" -> ErrorRed
        else -> AccentBlue
    }
    val timestamp = remember(intervention.timestamp) {
        val instant = Instant.ofEpochMilli(intervention.timestamp)
        DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")
            .withZone(ZoneId.systemDefault())
            .format(instant)
    }

    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.xl))
            .background(CardPrimary)
            .padding(Spacing.md) // ✅ تقليل padding الداخلي من Spacing.lg إلى Spacing.md
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            Icon(
                painterResource(statusIcon),
                intervention.status,
                Modifier.size(IconSize.md), // ✅ تصغير حجم الأيقونة من lg إلى md
                tint = statusColor
            )
            Spacer(Modifier.width(Spacing.md))
            Column(Modifier.weight(1f)) {
                Text(
                    intervention.title,
                    color = TextPrimary,
                    fontSize = AppFontSize.titleSmall, // ✅ تصغير من bodyMedium إلى titleSmall
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(Spacing.xs))
                Text(
                    intervention.description,
                    color = TextBody,
                    fontSize = AppFontSize.bodySmall,
                    lineHeight = AppFontSize.bodyLarge.value.sp
                )
                Text(timestamp, color = TextHint, fontSize = AppFontSize.caption)
            }
        }
    }
}

@Composable
private fun PermissionsTab(state: AgentState, viewModel: AgentViewModel) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = Spacing.lg)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            "الصلاحيات الممنوحة للوكيل",
            Modifier
                .fillMaxWidth()
                .padding(vertical = Spacing.md),
            textAlign = TextAlign.End,
            color = TextHint,
            fontSize = AppFontSize.bodyMedium,
            fontWeight = FontWeight.Bold
        )

        PermissionRow(R.drawable.ic_build, "الإصلاح التلقائي للأخطاء", state.permissions.autoFixErrors) {
            viewModel.togglePermission("autoFixErrors")
        }
        PermissionRow(R.drawable.ic_refresh, "إعادة المحاولة التلقائية", state.permissions.autoRetry) {
            viewModel.togglePermission("autoRetry")
        }
        PermissionRow(R.drawable.ic_tune, "تحسين الموارد", state.permissions.optimizeResources) {
            viewModel.togglePermission("optimizeResources")
        }
        PermissionRow(R.drawable.ic_folder, "الوصول للمشاريع", state.permissions.accessProjects) {
            viewModel.togglePermission("accessProjects")
        }

        Spacer(Modifier.height(Spacing.lg))
        Text(
            "عمق التدخل المسموح: ${state.permissions.maxInterventionDepth}",
            Modifier.fillMaxWidth(),
            textAlign = TextAlign.End,
            color = TextPrimary,
            fontSize = AppFontSize.bodyMedium,
            fontWeight = FontWeight.Bold
        )
        Slider(
            value = state.permissions.maxInterventionDepth.toFloat(),
            onValueChange = { viewModel.onMaxDepthChanged(it.toInt()) },
            valueRange = 1f..5f,
            steps = 4,
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(thumbColor = PrimaryLight, activeTrackColor = Primary)
        )
        Spacer(Modifier.height(Spacing.xl))
    }
}

@Composable
private fun PermissionRow(iconRes: Int, title: String, checked: Boolean, onToggle: () -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.xs)
            .clip(RoundedCornerShape(Radius.lg))
            .background(CardSoft)
            .padding(Spacing.md)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painterResource(iconRes),
                title,
                Modifier.size(IconSize.lg), // ✅ تصغير من xl إلى lg
                tint = if (checked) PrimaryLight else TextHint
            )
            Spacer(Modifier.width(Spacing.md))
            Text(
                title,
                Modifier.weight(1f),
                color = TextPrimary,
                fontSize = AppFontSize.titleSmall, // ✅ تصغير من bodyMedium إلى titleSmall
                fontWeight = FontWeight.Bold
            )
            Switch(
                checked,
                { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = SwitchThumb,
                    checkedTrackColor = SwitchTrackActive,
                    uncheckedThumbColor = SwitchThumb,
                    uncheckedTrackColor = SwitchTrackInactive
                )
            )
        }
    }
}
