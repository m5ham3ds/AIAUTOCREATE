package com.aiautocreate.presentation.ui.screens.ffmpeg

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
fun FfmpegScreen(
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FfmpegViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(state.saveSuccessMessage) {
        state.saveSuccessMessage?.let { snackbarHostState.showSnackbar(it); viewModel.clearMessages() }
    }
    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { snackbarHostState.showSnackbar(it); viewModel.clearMessages() }
    }

    var showStyleDropdown by remember { mutableStateOf(false) }
    var showQualityDropdown by remember { mutableStateOf(false) }

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
            Spacer(modifier = Modifier.height(Spacing.md))

            // نمط المونتاج
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.lg)
                    .clip(RoundedCornerShape(Radius.xxl))
                    .background(CardSecondary)
                    .padding(Spacing.lg)
            ) {
                Column {
                    Text(
                        "نمط المونتاج",
                        Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End,
                        color = TextSecondary,
                        fontSize = AppFontSize.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(Spacing.md))
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(ComponentSize.buttonHeightLg)
                            .clip(RoundedCornerShape(Radius.xl))
                            .background(CardBlueDark)
                            .clickable { showStyleDropdown = true }
                            .padding(horizontal = Spacing.lg),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            state.selectedMontageStyle,
                            Modifier.fillMaxWidth(),
                            textAlign = TextAlign.End,
                            color = TextPrimary,
                            fontSize = AppFontSize.bodyLarge
                        )
                    }
                    DropdownMenu(
                        expanded = showStyleDropdown,
                        onDismissRequest = { showStyleDropdown = false }
                    ) {
                        state.montageStyles.forEach { style ->
                            DropdownMenuItem(
                                text = { Text(style) },
                                onClick = {
                                    viewModel.onMontageStyleSelected(style)
                                    showStyleDropdown = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(Spacing.md))

            // جودة الإخراج
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.lg)
                    .clip(RoundedCornerShape(Radius.xxl))
                    .background(CardSecondary)
                    .padding(Spacing.lg)
            ) {
                Column {
                    Text(
                        "جودة الإخراج",
                        Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End,
                        color = TextSecondary,
                        fontSize = AppFontSize.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(Spacing.md))
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(ComponentSize.buttonHeightLg)
                            .clip(RoundedCornerShape(Radius.xl))
                            .background(CardBlueDark)
                            .clickable { showQualityDropdown = true }
                            .padding(horizontal = Spacing.lg),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            state.selectedQuality,
                            Modifier.fillMaxWidth(),
                            textAlign = TextAlign.End,
                            color = TextPrimary,
                            fontSize = AppFontSize.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    DropdownMenu(
                        expanded = showQualityDropdown,
                        onDismissRequest = { showQualityDropdown = false }
                    ) {
                        state.qualities.forEach { q ->
                            DropdownMenuItem(
                                text = { Text(q) },
                                onClick = {
                                    viewModel.onQualitySelected(q)
                                    showQualityDropdown = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(Spacing.md))

            // نسبة العرض
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.lg)
                    .clip(RoundedCornerShape(Radius.xxl))
                    .background(CardSecondary)
                    .padding(Spacing.lg)
            ) {
                Column {
                    Text(
                        "نسبة العرض",
                        Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End,
                        color = TextSecondary,
                        fontSize = AppFontSize.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(top = Spacing.md),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                    ) {
                        RatioChip("9:16 (شورتس)", selected = state.selectedAspectRatio == "9:16") {
                            viewModel.onAspectRatioSelected("9:16")
                        }
                        RatioChip("16:9 (يوتيوب)", selected = state.selectedAspectRatio == "16:9") {
                            viewModel.onAspectRatioSelected("16:9")
                        }
                    }
                }
            }

            Spacer(Modifier.height(Spacing.md))

            // قسم مدة الفيديو
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.lg)
                    .clip(RoundedCornerShape(Radius.xxl))
                    .background(CardSecondary)
                    .padding(Spacing.lg)
            ) {
                Column {
                    Text(
                        "مدة الفيديو",
                        Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End,
                        color = TextSecondary,
                        fontSize = AppFontSize.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = Spacing.md),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                    ) {
                        DurationPicker(
                            label = "الدقائق",
                            value = state.videoMinutes,
                            onIncrement = { viewModel.onMinutesChanged(state.videoMinutes + 1) },
                            onDecrement = { viewModel.onMinutesChanged(state.videoMinutes - 1) },
                            modifier = Modifier.weight(1f)
                        )
                        DurationPicker(
                            label = "الثواني",
                            value = state.videoSeconds,
                            onIncrement = { viewModel.onSecondsChanged(state.videoSeconds + 1) },
                            onDecrement = { viewModel.onSecondsChanged(state.videoSeconds - 1) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(Modifier.height(Spacing.md))

            // FPS
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.lg)
                    .clip(RoundedCornerShape(Radius.xxl))
                    .background(CardSecondary)
                    .padding(Spacing.lg)
            ) {
                Column {
                    Text(
                        "FPS",
                        Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End,
                        color = TextSecondary,
                        fontSize = AppFontSize.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(top = Spacing.md),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        FpsChip("24", selected = state.selectedFps == "24") { viewModel.onFpsSelected("24") }
                        FpsChip("30", selected = state.selectedFps == "30") { viewModel.onFpsSelected("30") }
                        FpsChip("60", selected = state.selectedFps == "60") { viewModel.onFpsSelected("60") }
                    }
                }
            }

            Spacer(Modifier.height(Spacing.md))

            // مفاتيح AI Pipeline (مع وصف لكل عنصر)
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.lg)
                    .clip(RoundedCornerShape(Radius.xxl))
                    .background(CardSecondary)
                    .padding(Spacing.lg)
            ) {
                Column {
                    Text(
                        "AI Pipeline",
                        Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End,
                        color = TextPrimary,
                        fontSize = AppFontSize.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(Spacing.md))

                    AppToggleCard(
                        title = "النموذج الرئيسي",
                        description = "يقوم بتحليل النص العام للمشروع وتوليد الهيكل الرئيسي للفيديو، مثل تقسيم السيناريو واقتراح المشاهد.",
                        isChecked = state.isMasterModelEnabled,
                        onCheckedChange = { viewModel.onMasterModelToggled(it) }
                    )
                    Spacer(Modifier.height(Spacing.xs))

                    AppToggleCard(
                        title = "مؤثرات صوتية",
                        description = "يضيف مؤثرات صوتية تلقائية (مثل الضوضاء، الصدى، الموسيقى الخلفية) حسب المشهد.",
                        isChecked = state.isAudioFxEnabled,
                        onCheckedChange = { viewModel.onAudioFxToggled(it) }
                    )
                    Spacer(Modifier.height(Spacing.xs))

                    AppToggleCard(
                        title = "مؤثرات بصرية",
                        description = "يطبق تأثيرات بصرية على الفيديو مثل التلاشي، التمويه، تغيير الألوان، لزيادة الجاذبية.",
                        isChecked = state.isVisualFxEnabled,
                        onCheckedChange = { viewModel.onVisualFxToggled(it) }
                    )
                    Spacer(Modifier.height(Spacing.xs))

                    AppToggleCard(
                        title = "انتقالات",
                        description = "يضيف انتقالات سلسة بين المشاهد (مثل الانزلاق، التلاشي، التكبير) لتحسين تدفق الفيديو.",
                        isChecked = state.isTransitionsEnabled,
                        onCheckedChange = { viewModel.onTransitionsToggled(it) }
                    )
                    Spacer(Modifier.height(Spacing.xs))

                    AppToggleCard(
                        title = "عدد ذكي",
                        description = "يقوم بتحليل الصور الناتجة ويختار أفضل الصور لتناسب المونتاج، ويتجاهل الصور المتشابهة أو الضعيفة.",
                        isChecked = state.isSmartCountEnabled,
                        onCheckedChange = { viewModel.onSmartCountToggled(it) }
                    )
                    Spacer(Modifier.height(Spacing.xs))

                    AppToggleCard(
                        title = "ترجمة",
                        description = "يضيف ترجمة (subtitles) للفيديو بناءً على النص المقروء أو الصوت، مع إمكانية تخصيص الخط والحجم.",
                        isChecked = state.isSubtitlesEnabled,
                        onCheckedChange = { viewModel.onSubtitlesToggled(it) }
                    )
                    Spacer(Modifier.height(Spacing.xs))

                    AppToggleCard(
                        title = "موسيقى",
                        description = "يولّد أو يضيف موسيقى خلفية مناسبة لمزاج الفيديو، أو يمكنه اختيار مسارات موسيقية من مكتبة خارجية.",
                        isChecked = state.isMusicEnabled,
                        onCheckedChange = { viewModel.onMusicToggled(it) }
                    )
                    Spacer(Modifier.height(Spacing.xs))

                    AppToggleCard(
                        title = "مراجع",
                        description = "يقوم بمراجعة النص أو الفيديو النهائي ويقدم اقتراحات للتحسين (مثل تغيير الترتيب، التوقيت، الأسلوب).",
                        isChecked = state.isReviewerEnabled,
                        onCheckedChange = { viewModel.onReviewerToggled(it) }
                    )
                    Spacer(Modifier.height(Spacing.xs))

                    AppToggleCard(
                        title = "منسق رئيسي",
                        description = "يقوم بتنسيق جميع المهام الفرعية (صور، صوت، فيديو) لضمان اتساق الجودة والوقت والإخراج النهائي.",
                        isChecked = state.isMasterOrchestratorEnabled,
                        onCheckedChange = { viewModel.onMasterOrchToggled(it) }
                    )
                    Spacer(Modifier.height(Spacing.xs))

                    AppToggleCard(
                        title = "فيديوهات خارجية",
                        description = "يبحث في APIs خارجية (مثل Pexels, Pixabay) ويضيف فيديوهات إضافية (B-roll) تتناسب مع المشهد.",
                        isChecked = state.isExternalVideoEnabled,
                        onCheckedChange = { viewModel.onExternalVideoToggled(it) }
                    )
                    Spacer(Modifier.height(Spacing.xs))

                    AppToggleCard(
                        title = "صور خارجية",
                        description = "يبحث في APIs خارجية ويضيف صوراً كخلفيات أو تراكبات (overlays) لتحسين العناصر البصرية.",
                        isChecked = state.isExternalImageEnabled,
                        onCheckedChange = { viewModel.onExternalImageToggled(it) }
                    )
                }
            }

            Spacer(Modifier.height(Spacing.lg))

            AppButton(
                text = "حفظ التعديلات 💾",
                onClick = { viewModel.saveSettings() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.lg)
            )
        }
    }
}

@Composable
private fun RowScope.RatioChip(text: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .weight(1f)
            .height(ComponentSize.buttonHeightLg)
            .clip(RoundedCornerShape(Radius.lg))
            .background(if (selected) Color(0xFF1A2240) else CardBlueDark)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (selected) PrimaryLight else TextBody,
            fontSize = AppFontSize.bodyMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun FpsChip(text: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        text = text,
        modifier = Modifier
            .padding(horizontal = Spacing.lg)
            .clickable(onClick = onClick),
        color = if (selected) PrimaryLight else TextBody,
        fontSize = AppFontSize.bodyMedium,
        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
    )
}

@Composable
private fun DurationPicker(
    label: String,
    value: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            color = TextHint,
            fontSize = AppFontSize.bodySmall,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(Spacing.sm))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(ComponentSize.buttonHeightLg)
                .clip(RoundedCornerShape(Radius.lg))
                .background(CardBlueDark),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            IconButton(
                onClick = onDecrement,
                enabled = value > 0,
                modifier = Modifier.size(IconSize.md)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_remove),
                    contentDescription = "نقص",
                    tint = TextPrimary
                )
            }
            Text(
                text = value.toString().padStart(2, '0'),
                color = TextPrimary,
                fontSize = AppFontSize.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(IconSize.xxl),
                textAlign = TextAlign.Center
            )
            IconButton(
                onClick = onIncrement,
                enabled = value < 60,
                modifier = Modifier.size(IconSize.md)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_add),
                    contentDescription = "زيادة",
                    tint = TextPrimary
                )
            }
        }
    }
}
