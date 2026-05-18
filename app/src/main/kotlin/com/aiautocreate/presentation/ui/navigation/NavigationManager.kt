package com.aiautocreate.presentation.ui.navigation

import androidx.navigation.NavHostController
import javax.inject.Inject
import javax.inject.Singleton

/**
 * مدير تنقل مركزي (اختياري) يمكن حقنه في ViewModels للتنقل.
 */
@Singleton
class NavigationManager @Inject constructor() {
    private var navController: NavHostController? = null

    fun setNavController(controller: NavHostController) {
        navController = controller
    }

    fun navigate(route: Route) {
        navController?.navigate(route) {
            popUpTo(Route.Home) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun goBack() {
        navController?.popBackStack()
    }
}