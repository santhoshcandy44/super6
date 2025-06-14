package com.lts360.compose.ui.usedproducts.manage

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSavedStateNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.ui.rememberSceneSetupNavEntryDecorator
import com.lts360.compose.ui.main.navhosts.routes.AccountAndProfileSettingsRoutes
import com.lts360.compose.ui.profile.EditProfileSettingsScreen
import com.lts360.compose.ui.profile.viewmodels.ProfileSettingsViewModel
import com.lts360.compose.ui.theme.AppTheme
import com.lts360.compose.ui.usedproducts.manage.navhost.ManageUsedProductListingRoutes
import com.lts360.compose.ui.usedproducts.manage.viewmodels.PublishedUsedProductsListingViewModel
import com.lts360.compose.ui.usedproducts.manage.viewmodels.UsedProductsListingWorkflowViewModel
import com.lts360.compose.utils.SafeDrawingBox
import org.koin.androidx.compose.koinViewModel

class UsedProductListingActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {

                Surface {
                    SafeDrawingBox {
                        ManageUsedProductListingNavHost {
                            this@UsedProductListingActivity.finish()
                        }
                    }
                }

            }
        }
    }
}


@Composable
private fun ManageUsedProductListingNavHost(
    defaultValue: ManageUsedProductListingRoutes = ManageUsedProductListingRoutes.ManageUsedProductListing,
    onFinishActivity: () -> Unit
) {
    val usedProductsListingWorkflowViewModel: UsedProductsListingWorkflowViewModel = koinViewModel()
    val publishedUsedProductsListingViewModel: PublishedUsedProductsListingViewModel = koinViewModel()
    val profileSettingViewModel: ProfileSettingsViewModel = koinViewModel()

    val backStack = rememberNavBackStack(defaultValue)

    NavDisplay(
        backStack = backStack,
        entryDecorators = listOf(
            rememberSceneSetupNavEntryDecorator(),
            rememberSavedStateNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        entryProvider = entryProvider {
            entry<ManageUsedProductListingRoutes.ManageUsedProductListing> {
                ManageUsedProductListingScreen(
                    onAddNewUsedProductListingClick = {
                        usedProductsListingWorkflowViewModel.clearSelectedDraft()
                        backStack.add(ManageUsedProductListingRoutes.CreateUsedProductListing)
                    },
                    onNavigateUpManagePublishedUsedProductListing = {
                        backStack.add(ManageUsedProductListingRoutes.ManagePublishedUsedProductListing)
                    },
                    onNavigateProfileSettings = {
                        backStack.add(AccountAndProfileSettingsRoutes.PersonalSettings)
                    },
                    onPopBackStack = onFinishActivity,
                    viewModel = publishedUsedProductsListingViewModel
                )
            }

            entry<ManageUsedProductListingRoutes.CreateUsedProductListing> {
                CreateUsedProductListingScreen(
                    onUsedProductListingCreated = {
                        backStack.removeLastOrNull()
                    },
                    onPopBackStack = {
                        backStack.removeLastOrNull()
                    },
                    viewModel = usedProductsListingWorkflowViewModel
                )
            }

            entry<ManageUsedProductListingRoutes.ManagePublishedUsedProductListing> {
                val viewModel: PublishedUsedProductsListingViewModel = publishedUsedProductsListingViewModel
                val editableSelectedUsedProductListing by viewModel.selectedUsedProductListing.collectAsState()

                editableSelectedUsedProductListing?.let {
                    ManagePublishedUsedProductListingScreen(
                        onPopBackStack = {
                            backStack.removeLastOrNull()
                        },
                        viewModel = viewModel
                    )
                }
            }

            entry<AccountAndProfileSettingsRoutes.PersonalSettings> {
                EditProfileSettingsScreen(
                    onEditFirstNameNavigateUp = {
                        backStack.add(AccountAndProfileSettingsRoutes.EditProfileFirstName)
                    },
                    onEditLastNameNavigateUp = {
                        backStack.add(AccountAndProfileSettingsRoutes.EditProfileLastName)
                    },
                    onEditAboutNavigateUp = {
                        backStack.add(AccountAndProfileSettingsRoutes.EditProfileAbout("complete_about"))
                    },
                    onEditEmailNavigateUp = {
                        backStack.add(AccountAndProfileSettingsRoutes.EditProfileEmail)
                    },
                    onPopBakStack = {
                        backStack.removeLastOrNull()
                    },
                    viewModel = profileSettingViewModel
                )
            }
        }
    )
}



