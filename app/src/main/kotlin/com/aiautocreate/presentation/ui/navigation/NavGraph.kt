package com.aiautocreate.presentation.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.aiautocreate.presentation.ui.screens.home.HomeScreen
import com.aiautocreate.presentation.ui.screens.results.ResultsScreen
import com.aiautocreate.presentation.ui.screens.ffmpeg.FfmpegScreen
import com.aiautocreate.presentation.ui.screens.subtitle.SubtitleStyleScreen
import com.aiautocreate.presentation.ui.screens.models.ModelsSettingsScreen
import com.aiautocreate.presentation.ui.screens.models.ModelsManagerScreen
import com.aiautocreate.presentation.ui.screens.settings.SettingsScreen
import com.aiautocreate.presentation.ui.screens.agent.AgentScreen
import com.aiautocreate.presentation.ui.screens.other.VideoReimaginerScreen
import com.aiautocreate.presentation.ui.screens.similarvideo.SimilarVideoScreen
import com.aiautocreate.presentation.ui.screens.audio.AudioReconstructorScreen
import com.aiautocreate.presentation.ui.screens.activitylog.ActivityLogScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier,
    startDestination: Route = Route.Home
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable<Route.Home> {
            HomeScreen(onMenuClick = onMenuClick)
        }
        composable<Route.Results> {
            ResultsScreen(onMenuClick = onMenuClick)
        }
        composable<Route.Ffmpeg> {
            FfmpegScreen(onMenuClick = onMenuClick)
        }
        composable<Route.SubtitleStyle> {
            SubtitleStyleScreen(onMenuClick = onMenuClick)
        }
        composable<Route.ModelsSettings> {
            ModelsSettingsScreen(onMenuClick = onMenuClick)
        }
        composable<Route.ModelsManager> {
            ModelsManagerScreen(onMenuClick = onMenuClick)
        }
        composable<Route.Settings> {
            SettingsScreen(onMenuClick = onMenuClick)
        }
        composable<Route.Agent> {
            AgentScreen(onMenuClick = onMenuClick)
        }
        composable<Route.VideoReimaginer> {
            VideoReimaginerScreen(onMenuClick = onMenuClick)
        }
        composable<Route.SimilarVideo> {
            SimilarVideoScreen(onMenuClick = onMenuClick)
        }
        composable<Route.AudioReconstructor> {
            AudioReconstructorScreen(onMenuClick = onMenuClick)
        }
        composable<Route.ActivityLog> {
            ActivityLogScreen(onMenuClick = onMenuClick)
        }
    }
}
