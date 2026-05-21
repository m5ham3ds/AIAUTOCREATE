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
import androidx.compose.ui.graphics.Color
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
            Spacer(modifier = Modifier.height(Spacing.md))

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

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.lg),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                AgentTab("الدردشة", 0, state.selectedTab) { viewModel.onTabSelected(0) }
                AgentTab("سجل التدخلات", 1, state.selectedTab) { viewModel.onTabSelected(1) }
                AgentTab("الصلاحيات", 2, state.selectedTab) { viewModel.onTabSelected(2) }
                AgentTab("الإحصائيات", 3, state.selectedTab) { viewModel.onTabSelected(3) }
            }

            Spacer(Modifier.height(Spacing.md))

            when (state.selectedTab) {
                0 -> ChatTab(state, viewModel, listState)
                1 -> InterventionsTab(state)
                2 -> PermissionsTab(state, viewModel)
                3 -> StatsTab(state, viewModel)
            }
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
        // ✅ صف الأزرار الثلاثة
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.lg, vertical = Spacing.sm),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            AssistChip(
                onClick = { viewModel.performQuickScan() },
                label = { Text("⚡ فحص سريع", fontSize = AppFontSize.bodySmall) },
                enabled = !state.isPerformingQuickScan && !state.isChatLoading,
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = CardSoft,
                    labelColor = TextPrimary
                ),
                modifier = Modifier.weight(1f)
            )
            AssistChip(
                onClick = { viewModel.performFullAnalysis() },
                label = { Text("📊 تحليل شامل", fontSize = AppFontSize.bodySmall) },
                enabled = !state.isPerformingFullAnalysis && !state.isChatLoading,
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = CardSoft,
                    labelColor = TextPrimary
                ),
                modifier = Modifier.weight(1f)
            )
            AssistChip(
                onClick = { viewModel.performCriticalErrorsCheck() },
                label = { Text("⚠️ أخطاء خطيرة", fontSize = AppFontSize.bodySmall) },
                enabled = !state.isCheckingErrors && !state.isChatLoading,
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = CardSoft,
                    labelColor = TextPrimary
                ),
                modifier = Modifier.weight(1f)
            )
        }

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
            .padding(Spacing.md)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            Icon(
                painterResource(statusIcon),
                intervention.status,
                Modifier.size(IconSize.md),
                tint = statusColor
            )
            Spacer(Modifier.width(Spacing.md))
            Column(Modifier.weight(1f)) {
                Text(
                    intervention.title,
                    color = TextPrimary,
                    fontSize = AppFontSize.titleSmall,
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
                Modifier.size(IconSize.lg),
                tint = if (checked) PrimaryLight else TextHint
            )
            Spacer(Modifier.width(Spacing.md))
            Text(
                title,
                Modifier.weight(1f),
                color = TextPrimary,
                fontSize = AppFontSize.titleSmall,
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

@Composable
private fun StatsTab(state: AgentState, viewModel: AgentViewModel) {
    val stats = state.stats
    if (state.isRefreshingStats) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Primary)
        }
        return
    }
    if (stats == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("لا توجد إحصائيات متاحة", color = TextHint)
                Spacer(Modifier.height(Spacing.md))
                Button(onClick = { viewModel.onTabSelected(3) }) {
                    Text("تحديث")
                }
            }
        }
        return
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Spacing.lg, vertical = Spacing.md)
    ) {
        Card(colors = CardDefaults.cardColors(containerColor = CardPrimary)) {
            Column(modifier = Modifier.padding(Spacing.lg)) {
                Text("📊 إحصائيات المشروع", fontWeight = FontWeight.Bold, fontSize = AppFontSize.headlineSmall)
                Spacer(Modifier.height(Spacing.md))
                StatItem("عدد المشاريع", stats.projectCount.toString())
                StatItem("النماذج النشطة", "${stats.activeModelCount} / ${stats.totalModelCount}")
                StatItem("إجمالي النشاطات", stats.totalLogs.toString())
                StatItem("العمليات الناجحة", stats.successCount.toString(), SuccessGreen)
                StatItem("الأخطاء", stats.errorCount.toString(), ErrorRed)
                StatItem("آخر نشاط", formatTimestamp(stats.lastActivityTimestamp))
                StatItem("عنوان آخر نشاط", stats.lastActivityTitle.take(50))
            }
        }

        Spacer(Modifier.height(Spacing.md))

        Button(
            onClick = { viewModel.onTabSelected(0) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryLight)
        ) {
            Text("💬 اذهب إلى الدردشة واسأل عن التفاصيل")
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, color: Color = TextPrimary) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.xs),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = TextHint, fontSize = AppFontSize.bodyMedium)
        Text(value, color = color, fontWeight = FontWeight.Bold, fontSize = AppFontSize.bodyMedium)
    }
}

private fun formatTimestamp(timestamp: Long): String {
    if (timestamp == 0L) return "غير متوفر"
    val sdf = java.text.SimpleDateFormat("yyyy/MM/dd HH:mm", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(timestamp))
}
