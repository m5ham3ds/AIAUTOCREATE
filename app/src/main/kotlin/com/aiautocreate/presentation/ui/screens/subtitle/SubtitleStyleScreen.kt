package com.aiautocreate.presentation.ui.screens.subtitle

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.border
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubtitleStyleScreen(
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SubtitleStyleViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(state.saveSuccessMessage) { state.saveSuccessMessage?.let { snackbarHostState.showSnackbar(it); viewModel.clearMessages() } }
    LaunchedEffect(state.errorMessage) { state.errorMessage?.let { snackbarHostState.showSnackbar(it); viewModel.clearMessages() } }

    var showFontDropdown by remember { mutableStateOf(false) }
    var showWeightDropdown by remember { mutableStateOf(false) }
    var showShadowDropdown by remember { mutableStateOf(false) }

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

            // ========== 1. منطقة المعاينة ==========
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(AppSpecific.subtitlePreviewHeight)
                    .padding(horizontal = Spacing.lg)
                    .clip(RoundedCornerShape(Radius.xxl))
                    .background(CardPrimary)
            ) {
                Box(Modifier.fillMaxSize().background(CardDark))
                
                // تطبيق لون الخلفية مع الشفافية
                val backgroundColor = try {
                    Color(android.graphics.Color.parseColor(state.backgroundColorHex))
                        .copy(alpha = state.backgroundColorOpacity / 100f)
                } catch (_: Exception) { Color.Black.copy(alpha = state.backgroundColorOpacity / 100f) }
                
                Box(
                    modifier = Modifier
                        .align(
                            when (state.selectedAlignment) {
                                "top" -> Alignment.TopCenter
                                "bottom" -> Alignment.BottomCenter
                                else -> Alignment.Center
                            }
                        )
                        .padding(Spacing.lg)
                        .clip(RoundedCornerShape(Radius.lg))
                        .background(backgroundColor)
                        .padding(Spacing.lg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        state.previewText,
                        color = try { Color(android.graphics.Color.parseColor(state.textColorHex)) } catch (_: Exception) { Color.White },
                        fontSize = state.fontSize.sp,
                        fontWeight = when (state.selectedWeight) {
                            "عادي" -> FontWeight.Normal
                            "متوسط" -> FontWeight.Medium
                            "أسود" -> FontWeight.Black
                            else -> FontWeight.Bold
                        },
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.md))

            // ========== 2. قسم الخط ==========
            AppCard(modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.lg)) {
                Column {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painterResource(R.drawable.ic_text_fields),
                            "Font",
                            Modifier.size(IconSize.lg),
                            tint = PrimaryLight
                        )
                        Text(
                            "الخط والنص",
                            Modifier.weight(1f),
                            textAlign = TextAlign.End,
                            color = TextPrimary,
                            fontSize = AppFontSize.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(Spacing.md))

                    Text("عائلة الخط", Modifier.fillMaxWidth(), textAlign = TextAlign.End, color = TextHint, fontSize = AppFontSize.bodyMedium)
                    Spacer(Modifier.height(Spacing.sm))
                    ExposedDropdownMenuBox(expanded = showFontDropdown, onExpandedChange = { showFontDropdown = it }) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(ComponentSize.buttonHeightLg)
                                .clip(RoundedCornerShape(Radius.lg))
                                .background(CardInputDark)
                                .menuAnchor()
                                .clickable { showFontDropdown = true }
                                .padding(horizontal = Spacing.lg),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(state.selectedFont, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End, color = TextPrimary, fontSize = AppFontSize.titleMedium)
                        }
                        ExposedDropdownMenu(expanded = showFontDropdown, onDismissRequest = { showFontDropdown = false }) {
                            state.availableFonts.forEach { font ->
                                DropdownMenuItem(
                                    text = { Text(font) },
                                    onClick = { viewModel.onFontSelected(font); showFontDropdown = false }
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(Spacing.md))

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                        // الوزن
                        ExposedDropdownMenuBox(expanded = showWeightDropdown, onExpandedChange = { showWeightDropdown = it }, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(ComponentSize.buttonHeightLg)
                                    .clip(RoundedCornerShape(Radius.lg))
                                    .background(CardInputDark)
                                    .menuAnchor()
                                    .clickable { showWeightDropdown = true }
                                    .padding(horizontal = Spacing.lg),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(state.selectedWeight, color = TextPrimary, fontSize = AppFontSize.titleMedium, fontWeight = FontWeight.Bold)
                            }
                            ExposedDropdownMenu(expanded = showWeightDropdown, onDismissRequest = { showWeightDropdown = false }) {
                                state.availableWeights.forEach { w ->
                                    DropdownMenuItem(
                                        text = { Text(w) },
                                        onClick = { viewModel.onWeightSelected(w); showWeightDropdown = false }
                                    )
                                }
                            }
                        }

                        // حجم الخط
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(ComponentSize.buttonHeightLg)
                                .clip(RoundedCornerShape(Radius.lg))
                                .background(CardInputDark),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                IconButton(onClick = { viewModel.decreaseFontSize() }, modifier = Modifier.size(IconSize.lg)) {
                                    Icon(painterResource(R.drawable.ic_remove), "Decrease", tint = TextPrimary)
                                }
                                Text(
                                    "${state.fontSize}",
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Center,
                                    color = TextPrimary,
                                    fontSize = AppFontSize.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                IconButton(onClick = { viewModel.increaseFontSize() }, modifier = Modifier.size(IconSize.lg)) {
                                    Icon(painterResource(R.drawable.ic_add), "Increase", tint = TextPrimary)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.md))

            // ========== 3. قسم الألوان والظل ==========
            AppCard(modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.lg)) {
                Column {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Icon(painterResource(R.drawable.ic_palette), "Colors", Modifier.size(IconSize.lg), tint = PrimaryLight)
                        Text("الألوان والظل", Modifier.weight(1f), textAlign = TextAlign.End, color = TextPrimary, fontSize = AppFontSize.titleLarge, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(Spacing.md))

                    // لون النص (حقل قابل للكتابة + معاينة)
                    Text("لون النص", Modifier.fillMaxWidth(), textAlign = TextAlign.End, color = TextHint, fontSize = AppFontSize.bodyMedium)
                    Spacer(Modifier.height(Spacing.sm))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = state.textColorHex,
                            onValueChange = { viewModel.onTextColorChanged(it) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            placeholder = { Text("#FFFFFF", color = TextHint) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryLight,
                                unfocusedBorderColor = BorderInput,
                                cursorColor = PrimaryLight,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                        Spacer(Modifier.width(Spacing.sm))
                        // معاينة اللون
                        Box(
                            modifier = Modifier
                                .size(IconSize.xl)
                                .clip(RoundedCornerShape(Radius.round))
                                .background(try { Color(android.graphics.Color.parseColor(state.textColorHex)) } catch (_: Exception) { Color.White })
                                .border(Border.thin, BorderInput, RoundedCornerShape(Radius.round))
                        )
                    }

                    Spacer(Modifier.height(Spacing.md))

                    // لون الخلفية (حقل قابل للكتابة)
                    Text("لون الخلفية", Modifier.fillMaxWidth(), textAlign = TextAlign.End, color = TextHint, fontSize = AppFontSize.bodyMedium)
                    Spacer(Modifier.height(Spacing.sm))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = state.backgroundColorHex,
                            onValueChange = { viewModel.onBackgroundColorChanged(it) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            placeholder = { Text("#000000", color = TextHint) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryLight,
                                unfocusedBorderColor = BorderInput,
                                cursorColor = PrimaryLight,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                        Spacer(Modifier.width(Spacing.sm))
                        Box(
                            modifier = Modifier
                                .size(IconSize.xl)
                                .clip(RoundedCornerShape(Radius.round))
                                .background(try { Color(android.graphics.Color.parseColor(state.backgroundColorHex)) } catch (_: Exception) { Color.Black })
                                .border(Border.thin, BorderInput, RoundedCornerShape(Radius.round))
                        )
                    }

                    Spacer(Modifier.height(Spacing.md))

                    Text("شفافية الخلفية", Modifier.fillMaxWidth(), textAlign = TextAlign.End, color = TextHint, fontSize = AppFontSize.bodyMedium)
                    Slider(
                        value = state.backgroundColorOpacity.toFloat(),
                        onValueChange = { viewModel.onBackgroundOpacityChanged(it.toInt()) },
                        valueRange = 0f..100f,
                        modifier = Modifier.fillMaxWidth(),
                        colors = SliderDefaults.colors(thumbColor = PrimaryLight, activeTrackColor = PrimaryLight, inactiveTrackColor = CardSoft)
                    )

                    Spacer(Modifier.height(Spacing.md))

                    Text("ظل النص", Modifier.fillMaxWidth(), textAlign = TextAlign.End, color = TextHint, fontSize = AppFontSize.bodyMedium)
                    Spacer(Modifier.height(Spacing.sm))
                    ExposedDropdownMenuBox(expanded = showShadowDropdown, onExpandedChange = { showShadowDropdown = it }) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(ComponentSize.buttonHeight)
                                .clip(RoundedCornerShape(Radius.lg))
                                .background(CardInputDark)
                                .menuAnchor()
                                .clickable { showShadowDropdown = true }
                                .padding(horizontal = Spacing.lg),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(state.selectedShadow, color = TextPrimary, fontSize = AppFontSize.bodyLarge, fontWeight = FontWeight.Bold)
                        }
                        ExposedDropdownMenu(expanded = showShadowDropdown, onDismissRequest = { showShadowDropdown = false }) {
                            state.shadowOptions.forEach { s ->
                                DropdownMenuItem(
                                    text = { Text(s) },
                                    onClick = { viewModel.onShadowSelected(s); showShadowDropdown = false }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.md))

            // ========== 4. قسم الموضع ==========
            AppCard(modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.lg)) {
                Column {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Icon(painterResource(R.drawable.ic_format_align_center), "Align", Modifier.size(IconSize.lg), tint = PrimaryLight)
                        Text("الموضع", Modifier.weight(1f), textAlign = TextAlign.End, color = TextPrimary, fontSize = AppFontSize.titleLarge, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(Spacing.md))

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                        AlignmentButton("أعلى", state.selectedAlignment == "top") { viewModel.onAlignmentSelected("top") }
                        AlignmentButton("وسط", state.selectedAlignment == "center") { viewModel.onAlignmentSelected("center") }
                        AlignmentButton("أسفل", state.selectedAlignment == "bottom") { viewModel.onAlignmentSelected("bottom") }
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.md))

            // ========== 5. زر الحفظ ==========
            AppButton(
                text = "حفظ التعديلات",
                onClick = { viewModel.saveSettings() },
                modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.lg)
            )
        }
    }
}

@Composable
private fun AlignmentButton(text: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(Radius.lg))
            .background(if (selected) CardBlueDark else CardInputDark)
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.lg, vertical = Spacing.md)
    ) {
        Text(
            text,
            color = if (selected) PrimaryLight else TextHint,
            fontSize = AppFontSize.bodyMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
