package com.aiautocreate

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment   // ✅ إضافة الاستيراد المفقود
import android.provider.Settings
import android.view.View
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

    private val requestManageStorageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                Timber.d("تم منح صلاحية MANAGE_EXTERNAL_STORAGE")
            } else {
                Timber.w("لم يتم منح صلاحية MANAGE_EXTERNAL_STORAGE")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

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
                SideEffect {
                    window.statusBarColor = BackgroundMain.toArgb()
                    window.navigationBarColor = BackgroundMain.toArgb()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        window.decorView.systemUiVisibility = if (darkTheme) {
                            window.decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
                        } else {
                            window.decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                        }
                    }
                }
                MainScreen()
            }
        }
    }

    private fun requestPermissionsIfNeeded() {
        val permissionsToRequest = mutableListOf<String>()

        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                permissionsToRequest.add(Manifest.permission.READ_MEDIA_VIDEO)
                permissionsToRequest.add(Manifest.permission.READ_MEDIA_AUDIO)
                permissionsToRequest.add(Manifest.permission.READ_MEDIA_IMAGES)
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
                permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
            else -> {
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.data = Uri.parse("package:$packageName")
                requestManageStorageLauncher.launch(intent)
            }
        }
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
                Route.Settings::class.qualifiedName -> "الإعدادات"
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
