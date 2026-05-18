package com.aiautocreate.presentation.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Primary,
    onPrimary = Color.White,
    primaryContainer = PrimaryDark,
    onPrimaryContainer = PrimaryLight,
    secondary = AccentBlue,
    onSecondary = Color.Black,
    secondaryContainer = AccentBlueDark,
    onSecondaryContainer = AccentBlueLight,
    tertiary = SuccessGreen,
    onTertiary = Color.Black,
    tertiaryContainer = SuccessGreenDark,
    onTertiaryContainer = Color.White,
    error = ErrorRed,
    onError = Color.White,
    errorContainer = ErrorRedSoft,
    onErrorContainer = Color.Black,
    background = BackgroundMain,
    onBackground = TextPrimary,
    surface = CardPrimary,
    onSurface = TextPrimary,
    surfaceVariant = CardSecondary,
    onSurfaceVariant = TextBody,
    outline = BorderPrimary,
    outlineVariant = BorderSecondary,
    inverseSurface = Color.White,
    inverseOnSurface = Color.Black
)

@Composable
fun AIAutoCreateTheme(
    darkTheme: Boolean = true, // Default to Dark
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> DarkColorScheme // We are enforcing Dark Theme for now
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as androidx.activity.ComponentActivity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}