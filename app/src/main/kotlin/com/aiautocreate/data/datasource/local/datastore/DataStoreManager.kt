package com.aiautocreate.data.datasource.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.aiautocreate.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DataStoreManager(
    internal val dataStore: DataStore<Preferences> // تم التغيير إلى internal
) {

    private companion object Keys {
        val LANGUAGE_KEY = stringPreferencesKey("language")
        val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
        val DYNAMIC_COLOR_KEY = booleanPreferencesKey("dynamic_color")
    }

    val language: Flow<String> = dataStore.data.map { preferences ->
        preferences[LANGUAGE_KEY] ?: "ar"
    }

    val themeMode: Flow<String> = dataStore.data.map { preferences ->
        preferences[THEME_MODE_KEY] ?: "system"
    }

    val dynamicColor: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[DYNAMIC_COLOR_KEY] ?: true
    }

    val userPreferences: Flow<UserPreferences> = dataStore.data.map { preferences ->
        UserPreferences(
            language = preferences[LANGUAGE_KEY] ?: "ar",
            themeMode = preferences[THEME_MODE_KEY] ?: "system",
            dynamicColor = preferences[DYNAMIC_COLOR_KEY] ?: true
        )
    }

    suspend fun setLanguage(language: String) {
        dataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = language
        }
    }

    suspend fun setThemeMode(themeMode: String) {
        dataStore.edit { preferences ->
            preferences[THEME_MODE_KEY] = themeMode
        }
    }

    suspend fun setDynamicColor(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[DYNAMIC_COLOR_KEY] = enabled
        }
    }

    suspend fun updateAll(preferences: UserPreferences) {
        dataStore.edit { prefs ->
            prefs[LANGUAGE_KEY] = preferences.language
            prefs[THEME_MODE_KEY] = preferences.themeMode
            prefs[DYNAMIC_COLOR_KEY] = preferences.dynamicColor
        }
    }
}
