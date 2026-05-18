package com.aiautocreate.presentation.ui.screens.other

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
fun VideoReimaginerScreen(
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: VideoReimaginerViewModel = hiltViewModel()
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

    val videoPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val fileName = getFileNameFromUri(context, it) ?: "video.mp4"
            viewModel.onVideoSelected(it.toString(), fileName)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = BackgroundMain,
        modifier = modifier
    ) { paddingValues ->
        Box(
            Modifier
                .fillMaxSize()
                .background(BackgroundMain)
                .padding(paddingValues)
        ) {
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // مسافة علوية بسيطة للتباعد عن الهيدر
                Spacer(modifier = Modifier.height(Spacing.md))

                // وصف الخدمة
                Text(
                    "ارفع الفيديو الخاص بك وسنقوم بترقية جودته إلى دقة أعلى مع تحسين الألوان وإزالة التشويش.",
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.lg),
                    textAlign = TextAlign.Center,
                    lineHeight = AppFontSize.titleLarge.value.sp,
                    color = TextHint,
                    fontSize = AppFontSize.bodyMedium
                )

                Spacer(Modifier.height(Spacing.md))

                // منطقة رفع الفيديو
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(AppSpecific.videoUploadHeight)
                        .padding(horizontal = Spacing.lg)
                        .clip(RoundedCornerShape(Radius.xxl))
                        .background(Brush.horizontalGradient(listOf(CardSecondary, CardDark)))
                        .border(Border.thick, Primary, RoundedCornerShape(Radius.xxl))
                        .clickable { videoPickerLauncher.launch("video/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (state.selectedVideoPath != null) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                painterResource(R.drawable.ic_check_circle),
                                null,
                                Modifier.size(IconSize.huge),
                                tint = SuccessGreen
                            )
                            Spacer(Modifier.height(Spacing.md))
                            Text(
                                "تم اختيار الفيديو",
                                color = TextPrimary,
                                fontSize = AppFontSize.titleMedium, // ✅ تقليل من titleLarge إلى titleMedium
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                state.selectedVideoName ?: "",
                                color = TextHint,
                                fontSize = AppFontSize.bodySmall // ✅ تقليل من bodyMedium إلى bodySmall
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
                            Spacer(Modifier.height(Spacing.lg)) // ✅ تقليل من Spacing.xxl إلى Spacing.lg
                            Text(
                                "اضغط لاختيار فيديو",
                                color = TextPrimary,
                                fontSize = AppFontSize.titleMedium, // ✅ تقليل من titleLarge إلى titleMedium
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "يدعم MP4, MOV, AVI",
                                color = TextHint,
                                fontSize = AppFontSize.bodySmall // ✅ تقليل من bodyMedium إلى bodySmall
                            )
                        }
                    }
                }

                Spacer(Modifier.height(Spacing.md))

                // إعدادات التحسين
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.lg)
                        .clip(RoundedCornerShape(Radius.xxl))
                        .background(CardPrimary)
                        .padding(Spacing.lg)
                ) {
                    Column {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painterResource(R.drawable.ic_hd),
                                "Export",
                                Modifier.size(IconSize.lg),
                                tint = TextHint
                            )
                            Text(
                                "إعدادات التصدير",
                                Modifier.weight(1f),
                                textAlign = TextAlign.End,
                                color = TextHint,
                                fontSize = AppFontSize.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            "دقة العرض",
                            Modifier
                                .fillMaxWidth()
                                .padding(top = Spacing.lg),
                            textAlign = TextAlign.End,
                            color = TextHint,
                            fontSize = AppFontSize.bodyLarge
                        )
                        Spacer(Modifier.height(Spacing.sm))
                        Row(
                            Modifier
                                .width(AppSpecific.exportOptionWidth)
                                .height(ComponentSize.buttonHeightLg)
                                .clip(RoundedCornerShape(Radius.lg))
                                .background(Color(0xFFC5C2CE))
                                .padding(Spacing.xs),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(Radius.md))
                                    .background(if (state.selectedResolution == "4K") Color.White else Color.Transparent)
                                    .clickable { viewModel.onResolutionSelected("4K") },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "4K",
                                    color = if (state.selectedResolution == "4K") BackgroundMain else TextHint,
                                    fontSize = AppFontSize.titleMedium, // ✅ تقليل من titleLarge إلى titleMedium
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Box(
                                Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(Radius.md))
                                    .background(if (state.selectedResolution == "1080p") Color.White else Color.Transparent)
                                    .clickable { viewModel.onResolutionSelected("1080p") },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "1080p",
                                    color = if (state.selectedResolution == "1080p") BackgroundMain else TextHint,
                                    fontSize = AppFontSize.titleMedium, // ✅ تقليل من titleLarge إلى titleMedium
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Text(
                            "معدل الإطارات",
                            Modifier
                                .fillMaxWidth()
                                .padding(top = Spacing.lg),
                            textAlign = TextAlign.End,
                            color = TextHint,
                            fontSize = AppFontSize.bodyLarge
                        )
                        Spacer(Modifier.height(Spacing.sm))
                        Row(
                            Modifier
                                .width(AppSpecific.exportOptionWidth)
                                .height(ComponentSize.buttonHeightLg)
                                .clip(RoundedCornerShape(Radius.lg))
                                .background(Color(0xFFC5C2CE))
                                .padding(Spacing.xs),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(Radius.md))
                                    .background(if (state.selectedFps == "60fps") Color.White else Color.Transparent)
                                    .clickable { viewModel.onFpsSelected("60fps") },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "60fps",
                                    color = if (state.selectedFps == "60fps") BackgroundMain else TextHint,
                                    fontSize = AppFontSize.titleMedium, // ✅ تقليل من titleLarge إلى titleMedium
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Box(
                                Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(Radius.md))
                                    .background(if (state.selectedFps == "30fps") Color.White else Color.Transparent)
                                    .clickable { viewModel.onFpsSelected("30fps") },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "30fps",
                                    color = if (state.selectedFps == "30fps") BackgroundMain else TextHint,
                                    fontSize = AppFontSize.titleMedium, // ✅ تقليل من titleLarge إلى titleMedium
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(Spacing.md))

                // شريط التقدم
                if (state.isProcessing) {
                    AppProgressSection(
                        progress = state.progress,
                        progressText = state.progressText,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Spacing.lg)
                    )
                    Spacer(Modifier.height(Spacing.md))
                }

                // زر الإجراء
                AppButton(
                    text = if (state.isProcessing) "جاري التحسين..." else "بدء تحسين الجودة ✨",
                    onClick = { viewModel.startEnhancement() },
                    enabled = state.selectedVideoPath != null && !state.isProcessing,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.lg)
                )

                // نتيجة
                if (state.enhancedVideoPath != null) {
                    Spacer(Modifier.height(Spacing.md))
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Spacing.lg)
                            .clip(RoundedCornerShape(Radius.xl))
                            .background(CardPrimary)
                            .padding(Spacing.md),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "تم حفظ الفيديو المحسّن بنجاح ✓",
                            color = SuccessGreen,
                            fontSize = AppFontSize.bodyMedium, // ✅ تقليل من bodyLarge إلى bodyMedium
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // ✅ تم إزالة Spacer السفلي الثابت (يتم ضبطه عبر Scaffold في MainActivity)
            }
        }
    }
}

fun getFileNameFromUri(context: android.content.Context, uri: Uri): String? {
    var name: String? = null
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
        if (nameIndex >= 0 && cursor.moveToFirst()) name = cursor.getString(nameIndex)
    }
    return name
}
