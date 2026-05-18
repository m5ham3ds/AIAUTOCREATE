package com.aiautocreate.presentation.ui.screens.settings

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
fun SettingsScreen(
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
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
            // مسافة علوية بسيطة بعد الهيدر
            Spacer(modifier = Modifier.height(Spacing.md))

            // 1. قسم المظهر
            SettingsSectionHeader(title = "المظهر (VISUAL APPEARANCE)")

            SettingsToggleCard(
                iconRes = R.drawable.ic_dark_mode,
                iconTint = TextHint,
                title = "الوضع الداكن",
                subtitle = "تفعيل الواجهة الداكنة للتطبيق",
                isChecked = state.themeMode == "dark",
                onCheckedChange = { enabled ->
                    viewModel.onThemeModeChanged(if (enabled) "dark" else "light")
                }
            )

            SettingsToggleCard(
                iconRes = R.drawable.ic_colorize,
                iconTint = Color(0xFFEFB8C8),
                title = "الألوان الديناميكية",
                subtitle = "استخدام ألوان النظام (Android 12+)",
                isChecked = state.dynamicColor,
                onCheckedChange = { enabled ->
                    viewModel.onDynamicColorChanged(enabled)
                }
            )

            Spacer(modifier = Modifier.height(Spacing.md))

            // 2. قسم اللغة
            SettingsSectionHeader(title = "اللغة (LANGUAGE)")

            SettingsToggleCard(
                iconRes = R.drawable.ic_language,
                iconTint = AccentBlue,
                title = if (state.language == "ar") "العربية" else "English",
                subtitle = "تغيير لغة التطبيق إلى ${if (state.language == "ar") "English" else "العربية"}",
                isChecked = state.language == "ar",
                onCheckedChange = { useArabic ->
                    viewModel.onLanguageToggled(useArabic)
                }
            )

            Spacer(modifier = Modifier.height(Spacing.md))

            // 3. قسم الدعم
            SettingsSectionHeader(title = "الدعم (SUPPORT)")

            SettingsInfoCard(
                iconRes = R.drawable.ic_info,
                iconTint = Color(0xFFF6A8C7),
                title = "حول التطبيق",
                subtitle = "الإصدار 1.0.0 | شروط الخدمة | الخصوصية"
            )

            // ✅ تم إزالة Spacer السفلي الثابت – المسافة تضاف تلقائياً من Scaffold في MainActivity
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        modifier = Modifier
            .fillMaxWidth()
            .background(BackgroundMain)
            .padding(horizontal = Spacing.lg, vertical = Spacing.md),
        color = TextHint,
        fontSize = AppFontSize.titleMedium,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun SettingsToggleCard(
    iconRes: Int,
    iconTint: Color,
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg)
            .padding(vertical = Spacing.xs),
        shape = RoundedCornerShape(Radius.xxl),
        colors = CardDefaults.cardColors(
            containerColor = CardPrimary,
            contentColor = TextPrimary
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = ComponentSize.cardElevation)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.lg, vertical = Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = title,
                modifier = Modifier.size(IconSize.lg),
                tint = iconTint
            )
            Spacer(modifier = Modifier.width(Spacing.md))

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = AppFontSize.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.End
                )
                Text(
                    text = subtitle,
                    color = TextHint,
                    fontSize = AppFontSize.bodySmall,
                    textAlign = TextAlign.End
                )
            }
            Spacer(modifier = Modifier.width(Spacing.lg))

            Switch(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
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
private fun SettingsInfoCard(
    iconRes: Int,
    iconTint: Color,
    title: String,
    subtitle: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg)
            .padding(vertical = Spacing.xs),
        shape = RoundedCornerShape(Radius.xxl),
        colors = CardDefaults.cardColors(
            containerColor = CardPrimary,
            contentColor = TextPrimary
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = ComponentSize.cardElevation)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.lg, vertical = Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = title,
                modifier = Modifier.size(IconSize.lg),
                tint = iconTint
            )
            Spacer(modifier = Modifier.width(Spacing.md))

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = AppFontSize.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.End
                )
                Text(
                    text = subtitle,
                    color = TextHint,
                    fontSize = AppFontSize.bodySmall,
                    textAlign = TextAlign.End
                )
            }
            Spacer(modifier = Modifier.width(Spacing.lg))

            Icon(
                painter = painterResource(id = R.drawable.ic_chevron_left),
                contentDescription = "مزيد من المعلومات",
                modifier = Modifier.size(IconSize.md),
                tint = TextHint
            )
        }
    }
}