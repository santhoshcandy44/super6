package com.lts360.compose.ui.settings

import android.content.Context
import android.telephony.TelephonyManager
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.lts360.compose.ui.auth.navhost.slideComposable
import com.lts360.compose.ui.settings.viewmodels.RegionalSettingsSelectionViewModel
import com.lts360.compose.ui.theme.ThemeSettingsScreen
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import javax.inject.Inject

data class Language(
    val code: String,
    val name: String,
    val icon: ImageVector = Icons.Filled.Language
)


data class Country(
    @SerialName("code")
    val code: String,
    @SerialName("name")
    val name: String,
    @Transient
    val icon: ImageVector = Icons.Filled.Flag
)


sealed class SettingsItem {
    data class Header(val title: String) : SettingsItem()
    data class Option(
        val title: String,
        val description: String,
        val icon: ImageVector,
        val iconTint: Color,
        val cardColor: Color,
        val onClick: () -> Unit
    ) : SettingsItem()
}

@Serializable
sealed class SettingsRoute {
    @Serializable
    data object General : SettingsRoute()

    @Serializable
    data object Theme : SettingsRoute()

    @Serializable
    data object Language : SettingsRoute()

    @Serializable
    data object Country : SettingsRoute()

}

@Composable
fun SettingsScreen(
    viewModel: RegionalSettingsSelectionViewModel = hiltViewModel(),
    onFinish: () -> Unit
) {

    val navController = rememberNavController()

    val currentCountry by viewModel.selectedCountry.collectAsState()
    val currentLanguage by viewModel.selectedLanguage.collectAsState()

    NavHost(
        navController = navController,
        startDestination = SettingsRoute.General
    ) {
        slideComposable<SettingsRoute.General> {
            GeneralSettingsScreen(
                currentCountry = currentCountry,
                currentLanguage = currentLanguage,
                onNavigateToTheme = { navController.navigate(SettingsRoute.Theme) },
                onNavigateToLanguage = {
                    navController.navigate(SettingsRoute.Language)
                },
                onNavigateToCountry = { navController.navigate(SettingsRoute.Country) },
                onPopBackStack = onFinish
            )
        }

        slideComposable<SettingsRoute.Language> {
            LanguageSelectionScreen(viewModel) {
                navController.popBackStack()
            }
        }

        slideComposable<SettingsRoute.Country> {
            CountrySelectionScreen(viewModel) {
                navController.popBackStack()
            }
        }

        slideComposable<SettingsRoute.Theme> {
            ThemeSettingsScreen {
                navController.popBackStack()
            }
        }

    }
}




