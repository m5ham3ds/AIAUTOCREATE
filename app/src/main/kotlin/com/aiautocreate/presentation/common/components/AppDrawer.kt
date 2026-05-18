package com.aiautocreate.presentation.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.aiautocreate.R
import com.aiautocreate.presentation.ui.navigation.Route
import com.aiautocreate.presentation.ui.theme.*

data class DrawerMenuItem(
    val label: String,
    val icon: ImageVector,
    val route: Route,
    val isDivider: Boolean = false
)

@Composable
fun AppDrawer(
    onItemClick: (Route) -> Unit,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val drawerWidth = (configuration.screenWidthDp * 0.85).dp // 85% من عرض الشاشة

    val menuItems = listOf(
        DrawerMenuItem("الرئيسية", Icons.Filled.Home, Route.Home),
        DrawerMenuItem("الإعدادات", Icons.Filled.Settings, Route.Settings),
        DrawerMenuItem("إدارة النماذج", Icons.Filled.Build, Route.ModelsManager),
        DrawerMenuItem("تنسيق الترجمة", Icons.Filled.Subtitles, Route.SubtitleStyle),
        DrawerMenuItem("", Icons.Filled.HorizontalRule, Route.Home, isDivider = true),
        DrawerMenuItem("الوكيل الذكي", Icons.Filled.Psychology, Route.Agent),
        DrawerMenuItem("تحسين جودة الفيديو", Icons.Filled.AutoFixHigh, Route.VideoReimaginer),
        DrawerMenuItem("استخراج فيديو مشابه", Icons.Filled.FindReplace, Route.SimilarVideo),
        DrawerMenuItem("معالج الصوت الذكي", Icons.Filled.Audiotrack, Route.AudioReconstructor),
        DrawerMenuItem("", Icons.Filled.HorizontalRule, Route.Home, isDivider = true),
        DrawerMenuItem("إعدادات FFmpeg", Icons.Filled.Movie, Route.Ffmpeg),
        DrawerMenuItem("النتائج والتجارب", Icons.Filled.Science, Route.Results),
        DrawerMenuItem("إعدادات النماذج", Icons.Filled.Tune, Route.ModelsSettings),
        DrawerMenuItem("سجل النشاطات", Icons.Filled.History, Route.ActivityLog)
    )

    ModalDrawerSheet(
        modifier = modifier
            .width(drawerWidth)
            .fillMaxHeight()
            .background(BackgroundTopbar),
        drawerContainerColor = BackgroundTopbar,
        drawerContentColor = TextPrimary,
        drawerShape = RoundedCornerShape(0.dp) // حواف حادة أو يمكن جعلها مدورة
    ) {
        // قسم الرأس
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Primary, PrimaryLight)
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Spacing.xxl, horizontal = Spacing.lg)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_logo_ai),
                    contentDescription = "Logo",
                    modifier = Modifier.size(IconSize.huge),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.height(Spacing.md))
                Text(
                    text = "AI AutoCreate",
                    color = Color.White,
                    fontSize = AppFontSize.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "الإصدار 1.0.0",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = AppFontSize.bodySmall
                )
            }
        }

        // القائمة القابلة للتمرير
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(vertical = Spacing.md)
        ) {
            menuItems.forEach { item ->
                if (item.isDivider) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.sm),
                        color = BorderPrimary,
                        thickness = Border.thin
                    )
                } else {
                    NavigationDrawerItem(
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                modifier = Modifier.size(IconSize.md),
                                tint = PrimaryLight
                            )
                        },
                        label = {
                            Text(
                                text = item.label,
                                fontSize = AppFontSize.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )
                        },
                        selected = false,
                        onClick = { onItemClick(item.route) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Spacing.md, vertical = Spacing.xs)
                            .clip(RoundedCornerShape(Radius.md)),
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = CardPrimary,
                            unselectedContainerColor = Color.Transparent,
                            selectedTextColor = PrimaryLight,
                            unselectedTextColor = TextPrimary,
                            selectedIconColor = PrimaryLight,
                            unselectedIconColor = TextHint
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(Spacing.md))
        Text(
            text = "© 2025 AI AutoCreate",
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md),
            textAlign = TextAlign.Center,
            color = TextHint,
            fontSize = AppFontSize.caption
        )
    }
}