package com.aiautocreate.presentation.ui.screens.results

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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

@Composable
fun ResultsScreen(
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ResultsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

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

            // بطاقة التقدم العام
            if (state.isProcessing || state.operations.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.lg)
                        .clip(RoundedCornerShape(Radius.xxl))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF16192B), Color(0xFF0F1220))
                            )
                        )
                        .padding(Spacing.lg)
                ) {
                    Column {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier
                                    .clip(RoundedCornerShape(Radius.round))
                                    .background(CapsuleLight)
                                    .padding(
                                        horizontal = Spacing.lg,
                                        vertical = Spacing.sm
                                    )
                            ) {
                                Text(
                                    text = "الخطوة ${state.overallStep} من ${state.overallTotal}",
                                    color = PrimaryLight,
                                    fontSize = AppFontSize.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = state.overallStatusText,
                                modifier = Modifier
                                    .weight(1f)
                                    .wrapContentWidth(Alignment.End),
                                textAlign = TextAlign.Center,
                                color = PrimaryLight,
                                fontSize = AppFontSize.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(
                                painterResource(id = R.drawable.ic_processing),
                                null,
                                Modifier.size(IconSize.md),
                                tint = Primary
                            )
                        }
                        Spacer(Modifier.height(Spacing.lg))
                        LinearProgressIndicator(
                            progress = { state.overallProgress },
                            modifier = Modifier.fillMaxWidth(),
                            color = ProgressPurple,
                            trackColor = ProgressBackground
                        )
                    }
                }
                Spacer(Modifier.height(Spacing.md))
            }

            // حالة عدم وجود عمليات
            if (state.operations.isEmpty() && !state.isProcessing) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(AppSpecific.emptyStateHeight)
                        .padding(horizontal = Spacing.lg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "لا توجد عمليات جارية حاليًا",
                        color = TextHint,
                        fontSize = AppFontSize.bodyMedium
                    )
                }
            }

            // بطاقات العمليات
            state.operations.forEach { op ->
                OperationCard(op)
                Spacer(Modifier.height(Spacing.md))
            }

            // ✅ تم إزالة المسافة السفلية الثابتة (يتم ضبطها عبر Scaffold في MainActivity)
        }
    }
}

@Composable
private fun OperationCard(op: OperationResult) {
    val statusColor = when (op.status) {
        "completed" -> SuccessGreen
        "in_progress" -> WarningOrange
        "failed" -> ErrorRed
        else -> TextHint
    }
    val iconRes = when (op.status) {
        "completed" -> R.drawable.ic_done_green
        "in_progress" -> R.drawable.ic_processing
        "failed" -> R.drawable.ic_error
        else -> R.drawable.ic_info
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg)
            .clip(RoundedCornerShape(Radius.xxl))
            .background(CardPrimary)
            .padding(Spacing.lg)
    ) {
        Column {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painterResource(id = iconRes),
                    null,
                    Modifier.size(IconSize.xl),
                    tint = statusColor
                )
                Spacer(Modifier.width(Spacing.md))
                Text(
                    op.title,
                    Modifier.weight(1f),
                    textAlign = TextAlign.End,
                    color = TextPrimary,
                    fontSize = AppFontSize.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            if (op.detailText.isNotEmpty()) {
                Spacer(Modifier.height(Spacing.sm))
                Text(op.detailText, color = TextBody, fontSize = AppFontSize.bodyMedium)
            }
            if (op.status == "in_progress") {
                Spacer(Modifier.height(Spacing.md))
                LinearProgressIndicator(
                    progress = { op.progress },
                    modifier = Modifier.fillMaxWidth(),
                    color = statusColor,
                    trackColor = ProgressBackground
                )
            } else {
                Spacer(Modifier.height(Spacing.sm))
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(AppSpecific.progressLineHeight)
                        .clip(RoundedCornerShape(Radius.xs))
                        .background(statusColor)
                )
            }
        }
    }
}
