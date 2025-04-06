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
import com.lts360.compose.ui.theme.AppTheme
import com.lts360.compose.ui.usedproducts.manage.navhost.ManageUsedProductListingRoutes
import com.lts360.compose.ui.usedproducts.manage.navhost.rememberManageUsedProductListingCustomBottomNavController
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
fun ManageUsedProductListingNavHost(
    defaultValue: ManageUsedProductListingRoutes = ManageUsedProductListingRoutes.ManageUsedProductListing,
    onFinishActivity: () -> Unit
) {


    val usedProductsListingWorkflowViewModel: UsedProductsListingWorkflowViewModel = hiltViewModel()
    val publishedUsedProductsListingViewModel: PublishedUsedProductsListingViewModel =
        hiltViewModel()

    var lastEntry by rememberSaveable { mutableStateOf<String?>(null) }
    val navController = rememberManageUsedProductListingCustomBottomNavController(
        lastEntry,
        publishedUsedProductsListingViewModel.isSelectedUsedProductListingNull()
    )

    val currentBackStackEntryAsState by navController.currentBackStackEntryAsState()

    LaunchedEffect(currentBackStackEntryAsState) {
        lastEntry = navController.currentBackStackEntry?.destination?.route
    }


    // Define the AnimatedNavHost
    NavHost(
        navController = navController,
        startDestination = defaultValue
    ) {
        // Entry Screen

        slideComposable<ManageUsedProductListingRoutes.ManageUsedProductListing> {
            ManageUsedProductListingScreen(
                navController,
                publishedUsedProductsListingViewModel,
                {
                    usedProductsListingWorkflowViewModel.clearSelectedDraft()
                    navController.navigate(ManageUsedProductListingRoutes.CreateUsedProductListing)
                }, {
                    navController.navigate(ManageUsedProductListingRoutes.ManagePublishedUsedProductListing)
                }, onFinishActivity
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
    }
}



