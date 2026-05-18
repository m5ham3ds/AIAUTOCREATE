package com.aiautocreate.presentation.ui.navigation

import kotlinx.serialization.Serializable

sealed interface Route {

    @Serializable
    data object Home : Route

    @Serializable
    data object Results : Route

    @Serializable
    data object Ffmpeg : Route

    @Serializable
    data object SubtitleStyle : Route

    @Serializable
    data object ModelsSettings : Route

    @Serializable
    data object ModelsManager : Route

    @Serializable
    data object Settings : Route

    @Serializable
    data object Agent : Route

    @Serializable
    data object VideoReimaginer : Route

    @Serializable
    data object SimilarVideo : Route

    @Serializable
    data object AudioReconstructor : Route

    @Serializable
    data object ActivityLog : Route

    @Serializable
    data object TestScreen : Route
}