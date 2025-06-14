package com.lts360.compose.ui.settings



import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Language
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSavedStateNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.ui.rememberSceneSetupNavEntryDecorator
import com.lts360.compose.ui.settings.viewmodels.RegionalSettingsSelectionViewModel
import com.lts360.compose.ui.theme.ThemeSettingsScreen
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel

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

sealed class SettingsRoute : NavKey {
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
    viewModel: RegionalSettingsSelectionViewModel = koinViewModel(),
    onFinish: () -> Unit
) {
    val backStack = rememberNavBackStack(SettingsRoute.General)
    val currentCountry by viewModel.selectedCountry.collectAsState()
    val currentLanguage by viewModel.selectedLanguage.collectAsState()

    NavDisplay(
        backStack = backStack,
        entryDecorators = listOf(
            rememberSceneSetupNavEntryDecorator(),
            rememberSavedStateNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        entryProvider = entryProvider {
            entry<SettingsRoute.General> {
                GeneralSettingsScreen(
                    currentCountry = currentCountry,
                    currentLanguage = currentLanguage,
                    onNavigateToTheme = { backStack.add(SettingsRoute.Theme) },
                    onNavigateToLanguage = { backStack.add(SettingsRoute.Language) },
                    onNavigateToCountry = { backStack.add(SettingsRoute.Country) },
                    onPopBackStack = onFinish
                )
            }

            entry<SettingsRoute.Language> {
                LanguageSelectionScreen(viewModel) {
                    backStack.removeLastOrNull()
                }
            }

            entry<SettingsRoute.Country> {
                CountrySelectionScreen(viewModel) {
                    backStack.removeLastOrNull()
                }
            }

            entry<SettingsRoute.Theme> {
                ThemeSettingsScreen {
                    backStack.removeLastOrNull()
                }
            }
        }
    )
}




