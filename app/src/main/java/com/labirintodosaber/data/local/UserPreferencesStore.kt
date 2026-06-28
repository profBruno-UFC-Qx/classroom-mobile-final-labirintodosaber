package com.labirintodosaber.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferencesStore @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    val isDarkTheme: Flow<Boolean> = dataStore.data.map { it[DARK_THEME_KEY] ?: false }

    suspend fun setDarkTheme(enabled: Boolean) {
        dataStore.edit { it[DARK_THEME_KEY] = enabled }
    }

    private companion object {
        val DARK_THEME_KEY = booleanPreferencesKey("theme_is_dark")
    }
}
