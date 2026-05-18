package com.aiautocreate.presentation.ui.screens.audio

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
fun AudioReconstructorScreen(
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AudioReconstructorViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(state.successMessage) {
        state.successMessage?.let { snackbarHostState.showSnackbar(it); viewModel.clearMessages() }
    }
    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { snackbarHostState.showSnackbar(it); viewModel.clearMessages() }
    }

    val audioPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { viewModel.onAudioSelected(it.toString()) }
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

            // وصف الخدمة
            Text(
                "قم بترقية جودة الصوت باستخدام تقنيات الذكاء الاصطناعي المتقدمة.",
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.lg),
                textAlign = TextAlign.Center,
                color = TextHint,
                fontSize = AppFontSize.bodyMedium
            )

            Spacer(Modifier.height(Spacing.md))

            // منطقة رفع الملف الصوتي
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(AppSpecific.audioUploadHeight)
                    .padding(horizontal = Spacing.lg)
                    .clip(RoundedCornerShape(Radius.xxl))
                    .background(Brush.horizontalGradient(listOf(CardSecondary, CardDark)))
                    .border(Border.thick, Primary, RoundedCornerShape(Radius.xxl))
                    .clickable { audioPickerLauncher.launch("audio/*") },
                contentAlignment = Alignment.Center
            ) {
                if (state.inputAudioPath != null) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            painterResource(R.drawable.ic_check_circle),
                            null,
                            Modifier.size(IconSize.huge),
                            tint = SuccessGreen
                        )
                        Spacer(Modifier.height(Spacing.md))
                        Text(
                            "تم اختيار الملف",
                            color = TextPrimary,
                            fontSize = AppFontSize.titleMedium, // ✅ تقليل حجم الخط من titleLarge إلى titleMedium
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            state.inputAudioPath?.substringAfterLast("/") ?: "",
                            color = TextHint,
                            fontSize = AppFontSize.bodySmall // ✅ تقليل حجم الخط من bodyMedium إلى bodySmall
                        )
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            Modifier
                                .size(AppSpecific.uploadIconBoxSize)
                                .clip(RoundedCornerShape(Radius.round))
                                .background(CardSoft),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painterResource(R.drawable.ic_upload_file),
                                "Upload",
                                Modifier.size(IconSize.xxl),
                                tint = PrimaryLight
                            )
                        }
                        Spacer(Modifier.height(Spacing.lg)) // ✅ تقليل المسافة من Spacing.xxl إلى Spacing.lg
                        Text(
                            "اضغط لاختيار ملف صوتي",
                            color = TextPrimary,
                            fontSize = AppFontSize.titleMedium, // ✅ تقليل حجم الخط
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "يدعم MP3, WAV, M4A",
                            color = TextHint,
                            fontSize = AppFontSize.bodySmall // ✅ تقليل حجم الخط
                        )
                    }
                }
            }

            Spacer(Modifier.height(Spacing.md))

            // خيارات المعالجة
            Text(
                "اختر نوع المعالجة",
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.lg),
                textAlign = TextAlign.End,
                color = TextHint,
                fontSize = AppFontSize.bodyMedium
            )
            Spacer(Modifier.height(Spacing.sm))
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.lg),
                horizontalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                AudioOptionCard(
                    title = "عزل الضوضاء",
                    selected = state.selectedOption == "noise",
                    onClick = { viewModel.onOptionSelected("noise") },
                    modifier = Modifier.weight(1f)
                )
                AudioOptionCard(
                    title = "ترميم الترددات",
                    selected = state.selectedOption == "freq",
                    onClick = { viewModel.onOptionSelected("freq") },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(Spacing.sm))
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.lg),
                horizontalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                AudioOptionCard(
                    title = "تحسين الوضوح",
                    selected = state.selectedOption == "restore",
                    onClick = { viewModel.onOptionSelected("restore") },
                    modifier = Modifier.weight(1f)
                )
                AudioOptionCard(
                    title = "تعزيز الصوت",
                    selected = state.selectedOption == "enhance",
                    onClick = { viewModel.onOptionSelected("enhance") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(Spacing.md))

            // قوة المعالجة
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.lg)
                    .clip(RoundedCornerShape(Radius.xxl))
                    .background(CardPrimary)
                    .padding(Spacing.lg)
            ) {
                Column {
                    Row(Modifier.fillMaxWidth()) {
                        Text(
                            "${state.processingStrength.toInt()}%",
                            color = PrimaryLight,
                            fontSize = AppFontSize.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "قوة المعالجة",
                            Modifier.weight(1f),
                            textAlign = TextAlign.End,
                            color = TextPrimary,
                            fontSize = AppFontSize.titleMedium, // ✅ تقليل من titleLarge إلى titleMedium
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.height(Spacing.lg))
                    Slider(
                        value = state.processingStrength,
                        onValueChange = { viewModel.onStrengthChanged(it) },
                        valueRange = 0f..100f,
                        modifier = Modifier.fillMaxWidth(),
                        colors = SliderDefaults.colors(
                            thumbColor = PrimaryLight,
                            activeTrackColor = AccentBlue,
                            inactiveTrackColor = CardSoft
                        )
                    )
                }
            }

            Spacer(Modifier.height(Spacing.md))

            // زر البدء
            AppButton(
                text = if (state.isProcessing) "جاري المعالجة..." else "بدء إعادة البناء ✨",
                onClick = { viewModel.startReconstruction() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.lg),
                enabled = !state.isProcessing
            )

            // النتيجة
            if (state.generatedAudioPath != null) {
                Spacer(Modifier.height(Spacing.md))
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.lg)
                        .clip(RoundedCornerShape(Radius.xxl))
                        .background(CardPrimary)
                        .padding(Spacing.md),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "تم الحفظ: ${state.generatedAudioPath?.substringAfterLast("/")}",
                        color = SuccessGreen,
                        fontSize = AppFontSize.bodySmall, // ✅ تقليل من bodyMedium إلى bodySmall
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // ✅ تم إزالة Spacer السفلي الثابت (يتم ضبطه عبر Scaffold في MainActivity)
        }
    }
}

@Composable
private fun AudioOptionCard(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(AppSpecific.audioOptionCardHeight)
            .clip(RoundedCornerShape(Radius.xxl))
            .background(if (selected) CardSecondary else CardPrimary)
            .clickable(onClick = onClick)
            .padding(Spacing.md),
        contentAlignment = Alignment.Center
    ) {
        Text(
            title,
            color = if (selected) PrimaryLight else TextPrimary,
            fontSize = AppFontSize.bodySmall, // ✅ تقليل من bodyMedium إلى bodySmall
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}
