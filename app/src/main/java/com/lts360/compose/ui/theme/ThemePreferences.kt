package com.lts360.compose.ui.theme

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class ThemePreferences @Inject constructor(@ApplicationContext private val context: Context) {

    private val sharedPreferences = context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)

    private val _themeFlow = MutableStateFlow(getThemeMode()) // Initialize with stored value
    val themeFlow  = _themeFlow.asStateFlow() // Expose as immutable Flow

    // Get theme mode synchronously
    fun getThemeMode(): Int {
        return sharedPreferences.getInt("theme_mode", -1) // Default to -1 if not found
    }

    // Save theme mode synchronously and update flow
    fun setThemeMode(mode: Int) {
        sharedPreferences.edit().putInt("theme_mode", mode).apply()
        _themeFlow.value = mode // Update flow to notify observers
    }
}
