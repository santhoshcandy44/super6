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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import com.lts360.compose.ui.auth.navhost.slideComposable
import com.lts360.compose.ui.main.navhosts.routes.AccountAndProfileSettingsRoutes
import com.lts360.compose.ui.profile.EditProfileSettingsScreen
import com.lts360.compose.ui.profile.viewmodels.ProfileSettingsViewModel
import com.lts360.compose.ui.theme.AppTheme
import com.lts360.compose.ui.usedproducts.manage.navhost.ManageUsedProductListingRoutes
import com.lts360.compose.ui.usedproducts.manage.navhost.rememberManageUsedProductListingCustomNavController
import com.lts360.compose.ui.usedproducts.manage.viewmodels.PublishedUsedProductsListingViewModel
import com.lts360.compose.ui.usedproducts.manage.viewmodels.UsedProductsListingWorkflowViewModel
import com.lts360.compose.utils.SafeDrawingBox
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
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


    val usedProductsListingWorkflowViewModel: UsedProductsListingWorkflowViewModel = hiltViewModel()
    val publishedUsedProductsListingViewModel: PublishedUsedProductsListingViewModel =
        hiltViewModel()

    var lastEntry by rememberSaveable { mutableStateOf<String?>(null) }
    val navController = rememberManageUsedProductListingCustomNavController(
        lastEntry,
        publishedUsedProductsListingViewModel.isSelectedUsedProductListingNull()
    )

    val currentBackStackEntryAsState by navController.currentBackStackEntryAsState()

    LaunchedEffect(currentBackStackEntryAsState) {
        lastEntry = navController.currentBackStackEntry?.destination?.route
    }

    val profileSettingViewModel: ProfileSettingsViewModel = hiltViewModel()


    NavHost(
        navController = navController,
        startDestination = defaultValue
    ) {

        slideComposable<ManageUsedProductListingRoutes.ManageUsedProductListing> {
            ManageUsedProductListingScreen(
                navController,
                publishedUsedProductsListingViewModel,
                {
                    usedProductsListingWorkflowViewModel.clearSelectedDraft()
                    navController.navigate(ManageUsedProductListingRoutes.CreateUsedProductListing)
                }, {
                    navController.navigate(ManageUsedProductListingRoutes.ManagePublishedUsedProductListing)
                },
                {
                    navController.navigate(AccountAndProfileSettingsRoutes.PersonalSettings)
                },
                onFinishActivity
            )

        }

        slideComposable<ManageUsedProductListingRoutes.CreateUsedProductListing> {
            CreateUsedProductListingScreen({
                navController.previousBackStackEntry?.savedStateHandle?.set(
                    "is_used_product_listing_created",
                    true
                )
                navController.popBackStack()
            }, {
                navController.popBackStack()
            }, usedProductsListingWorkflowViewModel)
        }


        slideComposable<ManageUsedProductListingRoutes.ManagePublishedUsedProductListing> {

            val viewModel: PublishedUsedProductsListingViewModel =
                publishedUsedProductsListingViewModel
            val editableSelectedUsedProductListing by viewModel.selectedUsedProductListing.collectAsState()

            editableSelectedUsedProductListing?.let {

                ManagePublishedUsedProductListingScreen(
                    {
                        navController.popBackStack()
                    },
                    viewModel
                )

            }

        }

        slideComposable<AccountAndProfileSettingsRoutes.PersonalSettings> {
            EditProfileSettingsScreen({
                navController.navigate(AccountAndProfileSettingsRoutes.EditProfileFirstName)
            }, {
                navController.navigate(AccountAndProfileSettingsRoutes.EditProfileLastName)
            }, {
                navController.navigate(AccountAndProfileSettingsRoutes.EditProfileAbout("complete_about"))
            }, {
                navController.navigate(AccountAndProfileSettingsRoutes.EditProfileEmail)
            }, {
                navController.popBackStack()
            }, profileSettingViewModel)
        }
    }
}



