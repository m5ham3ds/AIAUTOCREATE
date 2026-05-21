package com.aiautocreate.presentation.ui.screens.models

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
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
fun ModelsSettingsScreen(
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ModelsSettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(state.saveSuccessMessage) {
        state.saveSuccessMessage?.let { snackbarHostState.showSnackbar(it); viewModel.clearMessages() }
    }
    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { snackbarHostState.showSnackbar(it); viewModel.clearMessages() }
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
            Spacer(modifier = Modifier.height(Spacing.md))

            ApiKeysSection(state, viewModel)
            Spacer(modifier = Modifier.height(Spacing.md))

            DynamicModelSelectionSection(state, viewModel)
            Spacer(modifier = Modifier.height(Spacing.md))

            if (state.selectedModels["tts"]?.isNotEmpty() == true) {
                VoiceCloneSection(state, viewModel)
                Spacer(modifier = Modifier.height(Spacing.md))
            }

            Button(
                onClick = { viewModel.refreshModelsInBackground() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.lg)
                    .height(ComponentSize.buttonHeightLg),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = RoundedCornerShape(Radius.xxl)
            ) {
                Text("تحديث القوائم ↻", color = TextPrimary, fontSize = AppFontSize.titleMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(Spacing.sm))

            Button(
                onClick = { viewModel.saveSettings() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.lg)
                    .height(ComponentSize.buttonHeightLg),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryLight),
                shape = RoundedCornerShape(Radius.xxl)
            ) {
                Text("حفظ التعديلات 💾", color = TextPrimary, fontSize = AppFontSize.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ApiKeysSection(state: ModelsSettingsState, viewModel: ModelsSettingsViewModel) {
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
                    painter = painterResource(id = R.drawable.ic_info),
                    contentDescription = "API Keys",
                    modifier = Modifier.size(IconSize.lg),
                    tint = PrimaryLight
                )
                Text(
                    text = "مفاتيح API",
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.End,
                    color = TextPrimary,
                    fontSize = AppFontSize.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.md), color = BorderSecondary)

            OutlinedTextField(
                value = state.geminiApiKey,
                onValueChange = { viewModel.onGeminiKeyChanged(it) },
                label = { Text("Gemini API Key") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation()
            )
            Spacer(modifier = Modifier.height(Spacing.md))

            OutlinedTextField(
                value = state.geminiUrl,
                onValueChange = { viewModel.onGeminiUrlChanged(it) },
                label = { Text("Gemini URL") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(Spacing.md))

            // بعد OutlinedTextField الخاص بـ Gemini API Key
Spacer(modifier = Modifier.height(Spacing.md))

OutlinedTextField(
    value = state.geminiKeysCsv,
    onValueChange = { viewModel.onGeminiKeysChanged(it) },
    label = { Text("قائمة مفاتيح Gemini (مفصولة بفواصل)") },
    placeholder = { Text("مثال: AIza..., AIza..., AIza...") },
    modifier = Modifier.fillMaxWidth(),
    singleLine = false,
    maxLines = 5,
    colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = PrimaryLight,
        unfocusedBorderColor = BorderInput,
        cursorColor = PrimaryLight
    )
)
            
            OutlinedTextField(
                value = state.huggingFaceToken,
                onValueChange = { viewModel.onHuggingFaceTokenChanged(it) },
                label = { Text("HuggingFace Token") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation()
            )
            Spacer(modifier = Modifier.height(Spacing.md))

// بعد OutlinedTextField الخاص بـ HuggingFace Token
Spacer(modifier = Modifier.height(Spacing.md))

OutlinedTextField(
    value = state.huggingFaceTokensCsv,
    onValueChange = { viewModel.onHuggingFaceTokensChanged(it) },
    label = { Text("قائمة توكنات HuggingFace (مفصولة بفواصل)") },
    placeholder = { Text("مثال: hf_token1, hf_token2, hf_token3") },
    modifier = Modifier.fillMaxWidth(),
    singleLine = false,
    maxLines = 5,
    colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = PrimaryLight,
        unfocusedBorderColor = BorderInput,
        cursorColor = PrimaryLight
    )
)
            
            OutlinedTextField(
                value = state.ttsUrl,
                onValueChange = { viewModel.onTtsUrlChanged(it) },
                label = { Text("TTS URL") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(Spacing.md))

            OutlinedTextField(
                value = state.ffmpegPath,
                onValueChange = { viewModel.onFfmpegPathChanged(it) },
                label = { Text("FFmpeg Path") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(Spacing.md))

            OutlinedTextField(
                value = state.elevenLabsApiKey,
                onValueChange = { viewModel.onElevenLabsKeyChanged(it) },
                label = { Text("ElevenLabs API Key") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(Spacing.md))

            OutlinedTextField(
                value = state.lotsofsoundsApiKey,
                onValueChange = { viewModel.onLotsOfSoundsKeyChanged(it) },
                label = { Text("Lots of Sounds API Key") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation()
            )
            Spacer(modifier = Modifier.height(Spacing.md))

            OutlinedTextField(
                value = state.openVfxApiKey,
                onValueChange = { viewModel.onOpenVfxKeyChanged(it) },
                label = { Text("OpenVFX API Key") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation()
            )
            Spacer(modifier = Modifier.height(Spacing.md))

            OutlinedTextField(
                value = state.pixabayApiKey,
                onValueChange = { viewModel.onPixabayKeyChanged(it) },
                label = { Text("Pixabay API Key") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation()
            )
            Spacer(modifier = Modifier.height(Spacing.md))

            OutlinedTextField(
                value = state.pexelsApiKey,
                onValueChange = { viewModel.onPexelsKeyChanged(it) },
                label = { Text("Pexels API Key") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation()
            )
            Spacer(modifier = Modifier.height(Spacing.md))

            OutlinedTextField(
                value = state.freesoundApiKey,
                onValueChange = { viewModel.onFreesoundKeyChanged(it) },
                label = { Text("Freesound API Key") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DynamicModelSelectionSection(state: ModelsSettingsState, viewModel: ModelsSettingsViewModel) {
    // داخل DynamicModelSelectionSection, استبدل قائمة categories بالقائمة التالية:
val categories = listOf(
    "text" to "نموذج نصوص",
    "image" to "نموذج توليد الصور",
    "video" to "نموذج تحويل الصورة إلى فيديو",
    "tts" to "نموذج تحويل النص إلى صوت",
    "analysis" to "نموذج التحليل والمعالجة",
    "reviewer" to "نموذج مراجعة وتصحيح",
    "orchestrator" to "نموذج التنسيق العام",
    "music" to "نموذج توليد موسيقى",
    "transition" to "نموذج الانتقالات",
    "subtitle" to "نموذج الترجمة",
    "ffmpeg" to "نموذج أوامر FFmpeg"
)

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
                    painterResource(id = R.drawable.ic_model_training),
                    contentDescription = "Models",
                    modifier = Modifier.size(IconSize.lg),
                    tint = AccentBlue
                )
                Text(
                    "اختيار النماذج",
                    Modifier.weight(1f),
                    textAlign = TextAlign.End,
                    color = TextPrimary,
                    fontSize = AppFontSize.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.md), color = BorderSecondary)

            categories.forEach { (category, displayName) ->
                val selectedModelId = state.selectedModels[category] ?: ""
                val availableModels = state.availableModelsByCategory[category] ?: emptyList()
                var dropdownExpanded by remember { mutableStateOf(false) }

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        displayName,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End,
                        color = TextHint,
                        fontSize = AppFontSize.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(Spacing.sm))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(ComponentSize.buttonHeightLg)
                            .clip(RoundedCornerShape(Radius.lg))
                            .background(CardSecondary)
                            .clickable { dropdownExpanded = true }
                            .padding(horizontal = Spacing.lg),
                        contentAlignment = Alignment.Center
                    ) {
                        val modelName = if (selectedModelId.isNotEmpty()) {
                            availableModels.find { it.modelId == selectedModelId }?.modelName ?: selectedModelId
                        } else {
                            "اختر نموذجاً..."
                        }
                        Text(
                            modelName,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.End,
                            color = TextPrimary,
                            fontSize = AppFontSize.bodyMedium
                        )
                    }
                    DropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false }
                    ) {
                        // ✅ إضافة onClick فارغ للعنصر "لا شيء" لتجنب خطأ "No value passed for parameter 'onClick'"
                        DropdownMenuItem(
                            text = { Text("لا شيء", color = TextHint) },
                            onClick = {
                                viewModel.onModelSelected(category, "")
                                dropdownExpanded = false
                            }
                        )
                        availableModels.forEach { model ->
                            DropdownMenuItem(
                                text = { Text(model.modelName) },
                                onClick = {
                                    viewModel.onModelSelected(category, model.modelId)
                                    dropdownExpanded = false
                                }
                            )
                        }
                        if (availableModels.isEmpty()) {
                            // ✅ العنصر المعطل يجب أن يحتوي أيضاً على onClick فارغ (أو null) لكن Material3 يتطلب onClick غير nullable
                            DropdownMenuItem(
                                text = { Text("لا توجد نماذج متاحة", color = TextHint) },
                                enabled = false,
                                onClick = {} // ✅ إضافة onClick فارغ لتجنب الخطأ
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(Spacing.md))
            }
        }
    }
}

@Composable
private fun VoiceCloneSection(state: ModelsSettingsState, viewModel: ModelsSettingsViewModel) {
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
                    painterResource(id = R.drawable.ic_record_voice),
                    contentDescription = "Voice",
                    modifier = Modifier.size(IconSize.lg),
                    tint = Color(0xFFF7A8C8)
                )
                Text(
                    "الاستنساخ الصوتي",
                    Modifier.weight(1f),
                    textAlign = TextAlign.End,
                    color = TextPrimary,
                    fontSize = AppFontSize.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                "قم برفع عينة صوتية واضحة لتدريب النموذج.",
                Modifier
                    .fillMaxWidth()
                    .padding(top = Spacing.lg),
                textAlign = TextAlign.Center,
                lineHeight = AppFontSize.titleLarge.value.sp,
                color = TextBody,
                fontSize = AppFontSize.bodyMedium
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(AppSpecific.voiceCloneBoxHeight)
                    .padding(top = Spacing.lg)
                    .clip(RoundedCornerShape(Radius.xxl))
                    .background(BackgroundMain)
                    .border(Border.thick, TextHint, RoundedCornerShape(Radius.xxl))
                    .clickable { /* TODO: launch file picker */ },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    state.ttsVoiceSamplePath.ifEmpty { "اختر ملف صوتي (WAV, MP3)" },
                    color = TextPrimary,
                    fontSize = AppFontSize.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(Spacing.md))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = state.ttsUseVoiceClone,
                    onCheckedChange = { viewModel.onUseVoiceCloneChanged(it) }
                )
                Text("استخدام استنساخ الصوت", color = TextPrimary, fontSize = AppFontSize.bodyMedium)
            }
        }
    }
}
