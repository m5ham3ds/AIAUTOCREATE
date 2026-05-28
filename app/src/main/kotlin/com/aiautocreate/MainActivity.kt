package com.aiautocreate

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.compose.rememberNavController
import com.aiautocreate.presentation.common.components.AppBottomBar
import com.aiautocreate.presentation.common.components.AppDrawer
import com.aiautocreate.presentation.common.components.AppTopBar
import com.aiautocreate.presentation.ui.navigation.NavGraph
import com.aiautocreate.presentation.ui.navigation.Route
import com.aiautocreate.presentation.ui.screens.settings.SettingsViewModel
import com.aiautocreate.presentation.ui.theme.AIAutoCreateTheme
import com.aiautocreate.presentation.ui.theme.BackgroundMain
import com.aiautocreate.util.LocaleHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val settingsViewModel: SettingsViewModel by viewModels()

    private val requestMultiplePermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (!allGranted) {
            Timber.w("بعض الصلاحيات لم يتم منحها")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        // ✅ Enable edge-to-edge for modern insets handling
        WindowCompat.setDecorFitsSystemWindows(window, false)

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                settingsViewModel.state
                    .map { it.language }
                    .distinctUntilChanged()
                    .collect { language ->
                        val currentLang = java.util.Locale.getDefault().language
                        if (currentLang != language) {
                            LocaleHelper.applyLanguage(language)
                            recreate()
                        }
                    }
            }
        }

        requestPermissionsIfNeeded()

        setContent {
            val state by settingsViewModel.state.collectAsStateWithLifecycle()
            val darkTheme = when (state.themeMode) {
                "dark" -> true
                "light" -> false
                else -> isSystemInDarkTheme()
            }

            AIAutoCreateTheme(
                darkTheme = darkTheme,
                dynamicColor = state.dynamicColor
            ) {
                /**
                 * ✅ FIXED: Replaced deprecated systemUiVisibility with WindowInsetsControllerCompat.
                 * systemUiVisibility was deprecated in API 30 and unreliable on newer devices.
                 * WindowInsetsControllerCompat provides consistent behavior across API levels.
                 */
                SideEffect {
                    window.statusBarColor = BackgroundMain.toArgb()
                    window.navigationBarColor = BackgroundMain.toArgb()

                    val insetsController = WindowInsetsControllerCompat(window, window.decorView)
                    insetsController.isAppearanceLightStatusBars = !darkTheme
                    insetsController.isAppearanceLightNavigationBars = !darkTheme
                }
                MainScreen()
            }
        }
    }

    /**
     * ✅ FIXED: Simplified permission logic. Removed MANAGE_EXTERNAL_STORAGE request
     * and WRITE_EXTERNAL_STORAGE for Android 10+ (API 29+).
     *
     * Rationale:
     * - The app uses getExternalFilesDir() (app-private storage) via AppSettingsRepository
     * - No need for broad storage access which triggers Google Play review
     * - Android 10+: Scoped Storage makes WRITE_EXTERNAL_STORAGE ineffective
     * - Android 13+: Uses granular media permissions (READ_MEDIA_*)
     */
    private fun requestPermissionsIfNeeded() {
        val permissionsToRequest = mutableListOf<String>()

        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                // Android 13+: Granular media permissions
                permissionsToRequest.add(Manifest.permission.READ_MEDIA_VIDEO)
                permissionsToRequest.add(Manifest.permission.READ_MEDIA_AUDIO)
                permissionsToRequest.add(Manifest.permission.READ_MEDIA_IMAGES)
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                // Android 10-12: READ only, WRITE is not needed for app-private dirs
                permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
                // ❌ REMOVED: WRITE_EXTERNAL_STORAGE - ineffective on Android 10+
                // App uses getExternalFilesDir() which doesn't require this permission
            }
            else -> {
                // Android 9 and below: Legacy permissions
                permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
                permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }

        val needed = permissionsToRequest.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (needed.isNotEmpty()) {
            requestMultiplePermissionsLauncher.launch(needed.toTypedArray())
        }

        // ❌ REMOVED: MANAGE_EXTERNAL_STORAGE request
        // Not needed since app uses getExternalFilesDir() for all file operations
        // This permission triggers special Google Play review and should be avoided
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {

    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val currentTitle by remember {
        derivedStateOf {
            when (navController.currentDestination?.route) {
                Route.Home::class.qualifiedName -> "الرئيسية"
                Route.Results::class.qualifiedName -> "النتائج"
                Route.Ffmpeg::class.qualifiedName -> "إعدادات FFmpeg"
                Route.SubtitleStyle::class.qualifiedName -> "تنسيق الترجمة"
                Route.ActivityLog::class.qualifiedName -> "سجل النشاطات"
                Route.AudioReconstructor::class.qualifiedName -> "معالج الصوت الذكي"
                Route.ModelsSettings::class.qualifiedName -> "إعدادات النماذج"
                Route.ModelsManager::class.qualifiedName -> "مدير النماذج"
                Route.Settings::class.qualifiedName -> "إعدادات"
                Route.Agent::class.qualifiedName -> "الوكيل الذكي"
                Route.VideoReimaginer::class.qualifiedName -> "تحسين جودة الفيديو"
                Route.SimilarVideo::class.qualifiedName -> "فيديو مشابه"
                else -> "الرئيسية"
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawer(
                onItemClick = { route ->
                    scope.launch {
                        drawerState.close()
                        navController.navigate(route) {
                            popUpTo(Route.Home) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    ) {
        Scaffold(
            modifier = Modifier
                .statusBarsPadding()
                .navigationBarsPadding(),
            topBar = {
                AppTopBar(
                    title = currentTitle,
                    onMenuClick = { scope.launch { drawerState.open() } }
                )
            },
            bottomBar = {
                AppBottomBar(navController = navController)
            }
        ) { innerPadding ->
            NavGraph(
                navController = navController,
                onMenuClick = { scope.launch { drawerState.open() } },
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
