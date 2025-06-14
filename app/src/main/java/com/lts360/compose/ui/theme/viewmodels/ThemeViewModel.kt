package com.lts360.compose.ui.theme.viewmodels


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lts360.compose.ui.theme.ThemePreferences
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

sealed class ThemeMode {
    data object SystemDefault : ThemeMode()
    data object Dark : ThemeMode()
    data object Light : ThemeMode()
}

@KoinViewModel
class ThemeViewModel (private val themePreferences: ThemePreferences) : ViewModel() {

    val themeMode = themePreferences.themeFlow

    fun setThemeMode(mode: ThemeMode) {
        val modeInt = when (mode) {
            is ThemeMode.Dark -> 1
            is ThemeMode.Light -> 0
            is ThemeMode.SystemDefault -> -1
        }
        viewModelScope.launch {
            themePreferences.setThemeMode(modeInt)
        }
    }
}
