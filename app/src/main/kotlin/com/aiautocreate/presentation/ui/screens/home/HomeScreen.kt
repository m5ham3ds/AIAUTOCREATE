package com.aiautocreate.presentation.ui.screens.home

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
import androidx.compose.ui.platform.LocalContext
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
fun HomeScreen(
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
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

            // حالة الاتصال
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                StatusCapsule(
                    text = state.connectionStatus,
                    modifier = Modifier.wrapContentWidth()
                )
            }

            Spacer(modifier = Modifier.height(Spacing.lg))

            // بطاقة إعدادات الإنشاء
            AppCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.lg)
            ) {
                Column {
                    Text(
                        text = "إعدادات الإنشاء",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = Spacing.sm),
                        textAlign = TextAlign.End,
                        color = TextSecondary,
                        fontSize = AppFontSize.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    // ✅ قائمة الصور
                    DropdownSelector(
                        label = state.selectedImageStyle,
                        options = state.imageStyles,
                        onSelected = { viewModel.onImageStyleSelected(it) }
                    )
                    Spacer(modifier = Modifier.height(Spacing.md))

                    // ✅ قائمة الأغلفة
                    DropdownSelector(
                        label = state.selectedCoverStyle,
                        options = state.coverStyles,
                        onSelected = { viewModel.onCoverStyleSelected(it) }
                    )
                    Spacer(modifier = Modifier.height(Spacing.md))

                    // ✅ قائمة الأصوات
                    DropdownSelector(
                        label = state.selectedVoice,
                        options = state.voiceOptions,
                        onSelected = { viewModel.onVoiceSelected(it) }
                    )
                    Spacer(modifier = Modifier.height(Spacing.md))

                    // ✅ قائمة أساليب الفيديو
                    DropdownSelector(
                        label = state.selectedVideoStyle,
                        options = state.videoStyles,
                        onSelected = { viewModel.onVideoStyleSelected(it) }
                    )
                    Spacer(modifier = Modifier.height(Spacing.md))

                    // ✅ قائمة أساليب المونتاج
                    DropdownSelector(
                        label = state.selectedMontageStyle,
                        options = state.montageStyles,
                        onSelected = { viewModel.onMontageStyleSelected(it) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.lg))

            // منطقة السجلات
            AppCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(AppSpecific.logsCardHeight)
                    .padding(horizontal = Spacing.lg)
            ) {
                Column {
                    state.logs.takeLast(6).forEach { log ->
                        Text(
                            text = log,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.End,
                            color = TextBody,
                            fontSize = AppFontSize.bodySmall
                        )
                        Spacer(modifier = Modifier.height(Spacing.xs))
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.lg))

            // معاينة الفيديو
            if (state.outputVideoPath != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(AppSpecific.videoPreviewHeight)
                        .padding(horizontal = Spacing.lg)
                        .clip(RoundedCornerShape(Radius.xxl))
                        .background(CardOverlay)
                ) {
                    androidx.compose.ui.viewinterop.AndroidView(
                        factory = { ctx ->
                            android.widget.VideoView(ctx).apply {
                                setVideoPath(state.outputVideoPath)
                                start()
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(AppSpecific.videoPreviewHeight)
                        .padding(horizontal = Spacing.lg)
                        .clip(RoundedCornerShape(Radius.xxl))
                        .background(CardOverlay)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_play_large),
                        contentDescription = "Preview",
                        modifier = Modifier
                            .size(IconSize.huge)
                            .align(Alignment.Center),
                        tint = TextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.lg))

            // شريط المعالجة
            if (state.isProcessing || state.progress > 0f) {
                AppProgressSection(
                    progress = state.progress,
                    progressText = state.progressText,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.lg)
                )
                Spacer(modifier = Modifier.height(Spacing.sm))
            }

            // مساحة سفلية لمنع تداخل المحتوى مع لوحة الإدخال
            Spacer(modifier = Modifier.height(ComponentSize.bottomBarHeight + Spacing.xl))
        }

        // لوحة الإدخال السفلية
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(BackgroundMain)
                .padding(horizontal = Spacing.lg, vertical = Spacing.md)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = state.promptText,
                    onValueChange = { viewModel.onPromptChanged(it) },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("ادخل فكرة الفيديو هنا...", color = TextHint) },
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
                Spacer(modifier = Modifier.width(Spacing.md))
                IconButton(
                    onClick = { viewModel.startProcessing() },
                    enabled = state.promptText.isNotBlank() && !state.isProcessing,
                    modifier = Modifier
                        .size(ComponentSize.fabSize)
                        .clip(RoundedCornerShape(Radius.round))
                        .background(Primary)
                ) {
                    if (state.isProcessing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(IconSize.md),
                            color = TextPrimary,
                            strokeWidth = Border.thick
                        )
                    } else {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_send),
                            contentDescription = "إرسال",
                            modifier = Modifier.size(IconSize.md),
                            tint = TextPrimary
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownSelector(label: String, options: List<String>, onSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = label,
            onValueChange = {},
            readOnly = true,
            enabled = true,
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryLight,
                unfocusedBorderColor = BorderInput,
                focusedContainerColor = CardInputDark,
                unfocusedContainerColor = CardInputDark,
                disabledContainerColor = CardInputDark,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            shape = RoundedCornerShape(Radius.lg)
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
