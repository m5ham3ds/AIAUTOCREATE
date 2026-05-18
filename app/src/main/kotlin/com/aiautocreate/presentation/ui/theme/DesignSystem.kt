package com.aiautocreate.presentation.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// =========================================================
// 1. نظام المسافات (Spacing)
// =========================================================
object Spacing {
    val xxs = 2.dp
    val xs = 4.dp
    val sm = 6.dp
    val md = 10.dp
    val lg = 14.dp
    val xl = 18.dp
    val xxl = 22.dp
    val xxxl = 28.dp
    val huge = 36.dp
}

// =========================================================
// 2. أبعاد الأيقونات
// =========================================================
object IconSize {
    val xs: Dp = 14.dp
    val sm: Dp = 18.dp
    val md: Dp = 22.dp
    val lg: Dp = 26.dp
    val xl: Dp = 30.dp
    val xxl: Dp = 36.dp
    val huge: Dp = 44.dp
}

// =========================================================
// 3. أبعاد المكونات العامة
// =========================================================
object ComponentSize {
    val buttonHeight: Dp = 44.dp
    val buttonHeightLg: Dp = 52.dp
    val fabSize: Dp = 52.dp
    val iconButton: Dp = 36.dp
    val textFieldHeight: Dp = 52.dp
    val textFieldHeightSm: Dp = 44.dp
    val cardElevation: Dp = 1.dp
    val cardElevationPressed: Dp = 2.dp
    val topBarHeight: Dp = 60.dp
    val bottomBarHeight: Dp = 56.dp
    val drawerWidth: Dp = 270.dp
    val checkboxSize: Dp = 18.dp
    val radioButtonSize: Dp = 18.dp
    val switchWidth: Dp = 46.dp
    val switchHeight: Dp = 28.dp
    val sliderThumbSize: Dp = 18.dp
    val chipHeight: Dp = 36.dp
    val badgeSize: Dp = 18.dp
}

// =========================================================
// 4. أحجام الخطوط
// =========================================================
object AppFontSize {
    val caption = 10.sp
    val bodySmall = 11.sp
    val bodyMedium = 13.sp
    val bodyLarge = 15.sp
    val titleSmall = 16.sp
    val titleMedium = 18.sp
    val titleLarge = 20.sp
    val headlineSmall = 22.sp
    val headlineMedium = 24.sp
    val headlineLarge = 26.sp
    val displaySmall = 30.sp
}

// =========================================================
// 5. أنصاف أقطار الزوايا
// =========================================================
object Radius {
    val xs: Dp = 4.dp
    val sm: Dp = 6.dp
    val md: Dp = 10.dp
    val lg: Dp = 14.dp
    val xl: Dp = 18.dp
    val xxl: Dp = 22.dp
    val round: Dp = 50.dp
}

// =========================================================
// 6. حدود الفواصل
// =========================================================
object Border {
    val thin: Dp = 1.dp
    val normal: Dp = 1.5.dp
    val thick: Dp = 2.dp
}

// =========================================================
// 7. أبعاد خاصة بعناصر محددة
// =========================================================
object AppSpecific {
    val videoPreviewHeight: Dp = 180.dp
    val logsCardHeight: Dp = 180.dp
    val chatBubbleMaxWidth: Dp = 240.dp
    val audioUploadHeight: Dp = 180.dp
    val videoUploadHeight: Dp = 200.dp
    val voiceCloneBoxHeight: Dp = 130.dp
    val subtitlePreviewHeight: Dp = 200.dp
    val optionCardHeight: Dp = 72.dp
    val statusLabelWidth: Dp = 120.dp
    val emptyStateHeight: Dp = 150.dp
    val filterDotSize: Dp = 8.dp
    val audioOptionCardHeight: Dp = 100.dp
    val addModelContentHeight: Dp = 300.dp
    val uploadIconBoxSize: Dp = 80.dp
    val progressLineHeight: Dp = 4.dp
    val settingToggleWidth: Dp = 80.dp
    val settingRowHeight: Dp = 96.dp
    val similarVideoUploadHeight: Dp = 180.dp
    val exportOptionWidth: Dp = 200.dp
    val colorCircleSelectedSize: Dp = 48.dp
    val colorCircleUnselectedSize: Dp = 44.dp
    val colorCircleInnerSize: Dp = 36.dp
    
    // ✅ أبعاد خاصة بشاشة الإعدادات (جديدة)
    val settingsIconSize: Dp = 28.dp
    val settingsToggleHeight: Dp = 72.dp
    val settingsRowHorizontalPadding: Dp = 16.dp
    val settingsRowVerticalPadding: Dp = 12.dp
}
