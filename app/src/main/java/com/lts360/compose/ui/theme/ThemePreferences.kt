package com.lts360.compose.ui.theme

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.core.content.edit
import org.koin.core.annotation.Factory

@Factory
class ThemePreferences (applicationContext: Context) {

    private val sharedPreferences = applicationContext.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)

    private val _themeFlow = MutableStateFlow(getThemeMode())
    val themeFlow  = _themeFlow.asStateFlow()

    fun getThemeMode(): Int {
        return sharedPreferences.getInt("theme_mode", -1)
    }

    fun setThemeMode(mode: Int) {
        sharedPreferences.edit { putInt("theme_mode", mode) }
        _themeFlow.value = mode
    }
}
