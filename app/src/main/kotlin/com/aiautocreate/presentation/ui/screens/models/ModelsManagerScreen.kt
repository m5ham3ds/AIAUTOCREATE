package com.aiautocreate.presentation.ui.screens.models

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.aiautocreate.domain.model.ModelConfig
import com.aiautocreate.presentation.common.components.*
import com.aiautocreate.presentation.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelsManagerScreen(
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ModelsManagerViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf("active") }

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

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ComponentSize.buttonHeightLg)
                    .padding(horizontal = Spacing.lg),
                horizontalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                TabButton("الموجودة", selectedTab == "active") { selectedTab = "active" }
                TabButton("إضافة نموذج", selectedTab == "add") { selectedTab = "add" }
            }

            Spacer(modifier = Modifier.height(Spacing.md))

            when (selectedTab) {
                "active" -> {
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
                    } else {
                        state.models.forEach { model ->
                            ModelManagerCard(model, viewModel)
                            Spacer(modifier = Modifier.height(Spacing.sm))
                        }
                    }
                }
                "add" -> {
                    AddModelContent(viewModel, state)
                }
            }
        }

        FloatingActionButton(
            onClick = { viewModel.showAddDialog() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(Spacing.xxl)
                .size(ComponentSize.fabSize),
            containerColor = PrimaryLight,
            shape = RoundedCornerShape(Radius.round)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_add),
                contentDescription = "Add Model",
                modifier = Modifier.size(IconSize.xl),
                tint = TextPrimary
            )
        }
    }
}

@Composable
private fun TabButton(text: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .height(ComponentSize.buttonHeightLg)
            .clip(RoundedCornerShape(Radius.xl))
            .background(
                if (selected) Brush.horizontalGradient(colors = listOf(Primary, PrimaryLight))
                else Brush.horizontalGradient(colors = listOf(BackgroundMain, BackgroundMain))
            )
            .then(
                if (!selected) Modifier.border(Border.thick, Primary, RoundedCornerShape(Radius.xl)) else Modifier
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = TextPrimary, fontSize = AppFontSize.headlineSmall, fontWeight = FontWeight.Bold)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddModelContent(viewModel: ModelsManagerViewModel, state: ModelsManagerState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg)
    ) {
        Text(
            text = "البحث حسب الفئة",
            color = TextSecondary,
            fontSize = AppFontSize.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.End
        )
        Spacer(modifier = Modifier.height(Spacing.xs))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            Text("الفئة:", color = TextPrimary, fontSize = AppFontSize.bodyMedium)
            var expanded by remember { mutableStateOf(false) }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(Radius.lg))
                    .background(CardInputDark)
                    .clickable { expanded = true }
                    .padding(horizontal = Spacing.md, vertical = Spacing.sm),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (state.selectedCategoryForSearch) {
                        "text-to-image" -> "توليد الصور"
                        "image-to-image" -> "تعديل الصور"
                        "text-to-video" -> "توليد الفيديو"
                        "image-to-video" -> "صورة إلى فيديو"
                        "text-to-speech" -> "نص إلى صوت"
                        "audio-to-audio" -> "معالجة الصوت"
                        "automatic-speech-recognition" -> "نسخ الصوت"
                        "text-generation" -> "توليد النص"
                        "text2text-generation" -> "معالجة النص"
                        else -> state.selectedCategoryForSearch
                    },
                    color = TextPrimary,
                    fontSize = AppFontSize.titleMedium,
                    textAlign = TextAlign.End
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                listOf(
                    "text-to-image" to "توليد الصور",
                    "image-to-image" to "تعديل الصور",
                    "text-to-video" to "توليد الفيديو",
                    "image-to-video" to "صورة إلى فيديو",
                    "text-to-speech" to "نص إلى صوت",
                    "audio-to-audio" to "معالجة الصوت",
                    "automatic-speech-recognition" to "نسخ الصوت",
                    "text-generation" to "توليد النص",
                    "text2text-generation" to "معالجة النص"
                ).forEach { (tag, label) ->
                    DropdownMenuItem(
                        text = { Text(label) },
                        onClick = {
                            viewModel.setSelectedCategory(tag)
                            expanded = false
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(Spacing.sm))

        Button(
            onClick = { viewModel.searchModelsByCategory() },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isSearchingByCategory,
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryLight)
        ) {
            if (state.isSearchingByCategory) {
                CircularProgressIndicator(modifier = Modifier.size(IconSize.sm), color = TextPrimary)
                Spacer(modifier = Modifier.width(Spacing.sm))
            }
            Text("🔍 بحث عن نماذج بهذه الفئة")
        }
        Spacer(modifier = Modifier.height(Spacing.md))

        if (state.categorySearchResults.isNotEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(Spacing.md)) {
                    Text(
                        "نتائج البحث (${state.categorySearchResults.size} نموذج):",
                        color = PrimaryLight,
                        fontSize = AppFontSize.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(Spacing.sm))
                    state.categorySearchResults.take(10).forEach { model ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.addModelFromSearchResult(model) }
                                .padding(vertical = Spacing.xs),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(model.id, color = TextPrimary, fontSize = AppFontSize.bodyMedium, fontWeight = FontWeight.Bold)
                                Text(model.pipelineTag ?: "غير محدد", color = TextHint, fontSize = AppFontSize.caption)
                            }
                            Icon(
                                painter = painterResource(id = R.drawable.ic_add),
                                contentDescription = "إضافة",
                                modifier = Modifier.size(IconSize.md),
                                tint = PrimaryLight
                            )
                        }
                        Divider(color = BorderInput, modifier = Modifier.padding(vertical = Spacing.xs))
                    }
                }
            }
            Spacer(modifier = Modifier.height(Spacing.md))
        }

        Text(
            text = "أو ابحث بمعرف النموذج",
            color = TextSecondary,
            fontSize = AppFontSize.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.End
        )
        Spacer(modifier = Modifier.height(Spacing.xs))

        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = { viewModel.onSearchQueryChanged(it) },
            label = { Text("معرف النموذج في HuggingFace") },
            placeholder = { Text("مثال: mistralai/Mistral-7B-Instruct") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryLight,
                unfocusedBorderColor = BorderInput,
                cursorColor = PrimaryLight
            )
        )
        Spacer(modifier = Modifier.height(Spacing.sm))

        Button(
            onClick = { viewModel.searchModelOnHuggingFace() },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isSearching,
            colors = ButtonDefaults.buttonColors(containerColor = Primary)
        ) {
            if (state.isSearching) {
                CircularProgressIndicator(modifier = Modifier.size(IconSize.sm), color = TextPrimary)
                Spacer(modifier = Modifier.width(Spacing.sm))
            }
            Text("بحث عن النموذج 🔍")
        }
        Spacer(modifier = Modifier.height(Spacing.md))

        if (state.searchError != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = state.searchError!!,
                    modifier = Modifier.padding(Spacing.lg),
                    color = ErrorRed,
                    fontSize = AppFontSize.bodyMedium
                )
            }
            Spacer(modifier = Modifier.height(Spacing.md))
        }

        if (state.searchedModel != null && state.editableModel != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardSecondary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(Spacing.lg)) {
                    Text("نتيجة البحث:", color = PrimaryLight, fontSize = AppFontSize.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(Spacing.sm))

                    OutlinedTextField(
                        value = state.editableModel.customName,
                        onValueChange = { viewModel.updateEditableModel(it, state.editableModel.customDescription) },
                        label = { Text("اسم النموذج (قابل للتعديل)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryLight,
                            unfocusedBorderColor = BorderInput,
                            cursorColor = PrimaryLight
                        )
                    )
                    Spacer(modifier = Modifier.height(Spacing.sm))

                    OutlinedTextField(
                        value = state.editableModel.customDescription,
                        onValueChange = { viewModel.updateEditableModel(state.editableModel.customName, it) },
                        label = { Text("الوصف (قابل للتعديل)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryLight,
                            unfocusedBorderColor = BorderInput,
                            cursorColor = PrimaryLight
                        )
                    )
                    Spacer(modifier = Modifier.height(Spacing.sm))

                    Text("الفئة", color = TextHint, fontSize = AppFontSize.bodyMedium, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End)
                    Spacer(modifier = Modifier.height(Spacing.xs))
                    var categoryExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = categoryExpanded,
                        onExpandedChange = { categoryExpanded = it }
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(ComponentSize.buttonHeightLg)
                                .clip(RoundedCornerShape(Radius.lg))
                                .background(CardInputDark)
                                .menuAnchor()
                                .clickable { categoryExpanded = true }
                                .padding(horizontal = Spacing.lg),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                when (state.editableModel.category) {
                                    "image" -> "توليد الصور"
                                    "video" -> "تحويل الصورة إلى فيديو"
                                    "tts" -> "تحويل النص إلى صوت"
                                    "analysis" -> "تحليل ومعالجة"
                                    "reviewer" -> "مراجعة وتصحيح"
                                    "orchestrator" -> "تنسيق عام"
                                    "music" -> "توليد موسيقى"
                                    "transition" -> "انتقالات"
                                    "subtitle" -> "ترجمة"
                                    else -> "تحليل ومعالجة"
                                },
                                color = TextPrimary,
                                fontSize = AppFontSize.titleMedium,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.End
                            )
                        }
                        ExposedDropdownMenu(
                            expanded = categoryExpanded,
                            onDismissRequest = { categoryExpanded = false }
                        ) {
                            listOf(
                                "image" to "توليد الصور",
                                "video" to "تحويل الصورة إلى فيديو",
                                "tts" to "تحويل النص إلى صوت",
                                "analysis" to "تحليل ومعالجة",
                                "reviewer" to "مراجعة وتصحيح",
                                "orchestrator" to "تنسيق عام",
                                "music" to "توليد موسيقى",
                                "transition" to "انتقالات",
                                "subtitle" to "ترجمة"
                            ).forEach { (value, label) ->
                                DropdownMenuItem(
                                    text = { Text(label) },
                                    onClick = {
                                        viewModel.updateEditableCategory(value)
                                        categoryExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(Spacing.sm))

                    OutlinedTextField(
                        value = state.editableModel.settingsUrl,
                        onValueChange = { viewModel.updateEditableSettingsUrl(it) },
                        label = { Text("رابط الإعدادات (اختياري)") },
                        placeholder = { Text("https://...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryLight,
                            unfocusedBorderColor = BorderInput,
                            cursorColor = PrimaryLight
                        )
                    )
                    Spacer(modifier = Modifier.height(Spacing.sm))

                    OutlinedTextField(
                        value = state.editableModel.readmeUrl,
                        onValueChange = { viewModel.updateEditableReadmeUrl(it) },
                        label = { Text("رابط README") },
                        placeholder = { Text("https://huggingface.co/.../README.md") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryLight,
                            unfocusedBorderColor = BorderInput,
                            cursorColor = PrimaryLight
                        )
                    )
                    Spacer(modifier = Modifier.height(Spacing.sm))

                    OutlinedTextField(
                        value = state.editableModel.supportedStyles,
                        onValueChange = { viewModel.updateEditableSupportedStyles(it) },
                        label = { Text("الأنماط المدعومة (مفصولة بفواصل)") },
                        placeholder = { Text("واقعي, كرتوني, 3D") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryLight,
                            unfocusedBorderColor = BorderInput,
                            cursorColor = PrimaryLight
                        )
                    )
                    Spacer(modifier = Modifier.height(Spacing.sm))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = state.editableModel.supportsVoiceCloning,
                            onCheckedChange = { viewModel.updateEditableVoiceCloning(it) }
                        )
                        Text("يدعم استنساخ الصوت", color = TextPrimary, fontSize = AppFontSize.bodyMedium)
                    }
                    Spacer(modifier = Modifier.height(Spacing.sm))

                    Text("المعرف: ${state.searchedModel.id}", color = TextHint, fontSize = AppFontSize.bodySmall)
                    if (!state.searchedModel.pipelineTag.isNullOrEmpty()) {
                        Text("التصنيف: ${state.searchedModel.pipelineTag}", color = TextHint, fontSize = AppFontSize.bodySmall)
                    }
                    if (!state.searchedModel.tags.isNullOrEmpty()) {
                        Text("الوسوم: ${state.searchedModel.tags.take(3).joinToString(", ")}", color = TextHint, fontSize = AppFontSize.bodySmall)
                    }

                    Spacer(modifier = Modifier.height(Spacing.md))

                    Button(
                        onClick = { viewModel.addModelFromSearch() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isAdding,
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                    ) {
                        if (state.isAdding) {
                            CircularProgressIndicator(modifier = Modifier.size(IconSize.sm), color = TextPrimary)
                            Spacer(modifier = Modifier.width(Spacing.sm))
                        }
                        Text("إضافة النموذج +", color = TextPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(Spacing.lg))
        Text(
            text = "قريباً: دعم OpenAI و Google AI",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            color = TextHint,
            fontSize = AppFontSize.bodySmall
        )
    }
}

@Composable
private fun ModelManagerCard(model: ModelConfig, viewModel: ModelsManagerViewModel) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg, vertical = Spacing.xs)
            .clip(RoundedCornerShape(Radius.xxl))
            .background(CardPrimary)
            .padding(Spacing.lg)
    ) {
        Column {
            Row(Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .size(IconSize.huge)
                        .clip(RoundedCornerShape(Radius.xl))
                        .background(CardSecondary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_model_training),
                        contentDescription = model.modelName,
                        modifier = Modifier.size(IconSize.xl),
                        tint = PrimaryLight
                    )
                }
                Spacer(modifier = Modifier.width(Spacing.lg))
                Column(modifier = Modifier.weight(1f)) {
                    Text(model.modelName, color = TextPrimary, fontSize = AppFontSize.headlineMedium, fontWeight = FontWeight.Bold)
                    Text(model.modelId, color = TextHint, fontSize = AppFontSize.bodySmall)
                    if (model.description.isNotEmpty()) {
                        Text(model.description.take(80), color = TextBody, fontSize = AppFontSize.bodySmall)
                    }
                    if (model.category.isNotEmpty()) {
                        Text("الفئة: ${model.category}", color = TextHint, fontSize = AppFontSize.caption)
                    }
                }
                Box(
                    modifier = Modifier
                        .width(AppSpecific.statusLabelWidth)
                        .height(ComponentSize.buttonHeight)
                        .clip(RoundedCornerShape(Radius.round))
                        .background(CardSecondary),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (model.isEnabled) "نشط" else "متوقف",
                            color = if (model.isEnabled) SuccessGreen else TextHint,
                            fontSize = AppFontSize.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(Spacing.sm))
                        Box(
                            modifier = Modifier
                                .size(Spacing.sm)
                                .clip(RoundedCornerShape(Radius.round))
                                .background(if (model.isEnabled) SuccessGreen else Color(0xFFFFD6D6))
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(Spacing.lg))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                Button(
                    onClick = { viewModel.toggleModel(model.id, !model.isEnabled) },
                    modifier = Modifier.weight(1f).height(ComponentSize.buttonHeightLg),
                    shape = RoundedCornerShape(Radius.lg),
                    colors = ButtonDefaults.buttonColors(containerColor = CardSecondary)
                ) {
                    Text("تغيير الحالة", color = TextPrimary, fontSize = AppFontSize.titleMedium, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = { viewModel.deleteModel(model) },
                    modifier = Modifier.weight(1f).height(ComponentSize.buttonHeightLg),
                    shape = RoundedCornerShape(Radius.lg),
                    colors = ButtonDefaults.buttonColors(containerColor = ButtonPrimaryStart)
                ) {
                    Text("حذف", color = TextPrimary, fontSize = AppFontSize.titleMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
