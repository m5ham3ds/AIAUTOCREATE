package com.aiautocreate.presentation.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.aiautocreate.R
import com.aiautocreate.presentation.ui.navigation.Route
import com.aiautocreate.presentation.ui.theme.*

// =========================================================
// 1. حقل نصي موحد
// =========================================================
@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .height(ComponentSize.textFieldHeight),
        placeholder = { Text(placeholder, color = TextHint) },
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = TextPrimary),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PrimaryLight,
            unfocusedBorderColor = BorderInput,
            cursorColor = PrimaryLight
        ),
        shape = RoundedCornerShape(Radius.xl),
        trailingIcon = trailingIcon
    )
}

// =========================================================
// 2. قائمة منسدلة بسيطة
// =========================================================
@Composable
fun AppDropdown(
    label: String,
    modifier: Modifier = Modifier,
    onDropdownClick: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(ComponentSize.buttonHeightLg)
            .clip(RoundedCornerShape(Radius.lg))
            .background(CardInputDark)
            .padding(horizontal = Spacing.lg),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_dropdown_arrow),
            contentDescription = "Dropdown",
            modifier = Modifier.size(IconSize.md),
            tint = TextMuted
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(text = label, color = TextSoft, fontSize = AppFontSize.bodyLarge)
        Spacer(modifier = Modifier.width(Spacing.md))
        Icon(
            painter = painterResource(id = R.drawable.ic_palette),
            contentDescription = "Icon",
            modifier = Modifier.size(IconSize.lg),
            tint = AccentBlue
        )
    }
}

// =========================================================
// 3. بطاقة تبديل (Toggle Card)
// =========================================================
@Composable
fun AppToggleCard(
    title: String,
    description: String,
    isChecked: Boolean,
    modifier: Modifier = Modifier,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.xl))
            .background(CardDark)
            .padding(horizontal = Spacing.lg, vertical = Spacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = AppFontSize.bodyMedium)
            if (description.isNotEmpty()) {
                Text(text = description, color = TextHint, fontSize = AppFontSize.caption)
            }
        }
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = SwitchThumb,
                checkedTrackColor = SwitchTrackActive,
                uncheckedThumbColor = SwitchThumb,
                uncheckedTrackColor = SwitchTrackInactive
            ),
            modifier = Modifier.padding(start = Spacing.sm)
        )
    }
}

// =========================================================
// 4. كبسولة حالة
// =========================================================
@Composable
fun StatusCapsule(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(color = CardInput, shape = RoundedCornerShape(Radius.round))
            .padding(horizontal = Spacing.xl, vertical = Spacing.sm),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = TextBody, fontSize = AppFontSize.caption)
    }
}

// =========================================================
// 5. شريط تقدم
// =========================================================
@Composable
fun AppProgressSection(progress: Float, progressText: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(ComponentSize.buttonHeight)
            .background(
                brush = Brush.horizontalGradient(colors = listOf(CardSecondary, CardDark)),
                shape = RoundedCornerShape(Radius.xl)
            )
            .padding(horizontal = Spacing.lg),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.weight(1f),
            color = ProgressBlue,
            trackColor = ProgressBackground
        )
        Spacer(modifier = Modifier.width(Spacing.md))
        Text(
            text = "%${(progress * 100).toInt()}",
            color = TextSecondary,
            fontWeight = FontWeight.Bold,
            fontSize = AppFontSize.bodyMedium
        )
    }
}

// =========================================================
// 6. بيانات عنصر التنقل السفلي
// =========================================================
sealed class BottomNavItem(
    val route: Route,
    val iconRes: Int,
    val label: String
) {
    object Home : BottomNavItem(Route.Home, R.drawable.ic_home, "الرئيسية")
    object Results : BottomNavItem(Route.Results, R.drawable.ic_results, "النتائج")
    object Ffmpeg : BottomNavItem(Route.Ffmpeg, R.drawable.ic_ffmpeg, "FFmpeg")
}

// =========================================================
// 7. شريط التنقل السفلي (متصل بـ NavController)
// =========================================================
@Composable
fun AppBottomBar(
    navController: NavController,
    items: List<BottomNavItem> = listOf(BottomNavItem.Home, BottomNavItem.Results, BottomNavItem.Ffmpeg),
    modifier: Modifier = Modifier
) {
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    NavigationBar(
        modifier = modifier
            .fillMaxWidth()
            .height(ComponentSize.bottomBarHeight)
            .background(NavBackground),
        containerColor = NavBackground,
        tonalElevation = 0.dp
    ) {
        items.forEach { item ->
            val selected = currentRoute == item.route::class.qualifiedName
            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (!selected) {
                        navController.navigate(item.route) {
                            popUpTo(Route.Home) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        painter = painterResource(id = item.iconRes),
                        contentDescription = item.label,
                        modifier = Modifier.size(IconSize.lg),
                        tint = if (selected) NavSelected else NavUnselected
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        color = if (selected) NavSelected else NavUnselected,
                        fontSize = AppFontSize.caption
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = NavSelected,
                    selectedTextColor = NavSelected,
                    unselectedIconColor = NavUnselected,
                    unselectedTextColor = NavUnselected,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}