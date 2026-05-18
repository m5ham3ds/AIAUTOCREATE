package com.aiautocreate.presentation.ui.theme

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.ui.unit.IntOffset

object AppAnimations {
    val defaultEnterTransition: EnterTransition =
        fadeIn(animationSpec = tween(300)) + slideInVertically(
            initialOffsetY = { it / 4 },
            animationSpec = tween(300)
        )

    val defaultExitTransition: ExitTransition =
        fadeOut(animationSpec = tween(300)) + slideOutVertically(
            targetOffsetY = { it / 4 },
            animationSpec = tween(300)
        )
}
