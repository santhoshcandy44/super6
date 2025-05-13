package com.lts360.compose.ui.localjobs.manage

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
import com.lts360.compose.ui.localjobs.manage.navhost.ManageLocalJobRoutes
import com.lts360.compose.ui.localjobs.manage.navhost.rememberManageLocalJobsCustomNavController
import com.lts360.compose.ui.localjobs.manage.viewmodels.LocalJobWorkFlowViewModel
import com.lts360.compose.ui.localjobs.manage.viewmodels.PublishedLocalJobViewModel
import com.lts360.compose.ui.theme.AppTheme
import com.lts360.compose.utils.SafeDrawingBox
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class LocalJobsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {

                Surface {
                    SafeDrawingBox {
                        ManageLocalJobsNavHost {
                            this@LocalJobsActivity.finish()
                        }
                    }
                }

            }
        }
    }
}


@Composable
fun ManageLocalJobsNavHost(
    defaultValue: ManageLocalJobRoutes = ManageLocalJobRoutes.ManageLocalJob,
    onFinishActivity: () -> Unit
) {

    val localJobsWorkflowViewModel: LocalJobWorkFlowViewModel = hiltViewModel()
    val publishedLocalJobViewModel: PublishedLocalJobViewModel = hiltViewModel()

    var lastEntry by rememberSaveable { mutableStateOf<String?>(null) }
    val navController = rememberManageLocalJobsCustomNavController(
        lastEntry,
        publishedLocalJobViewModel.isSelectedLocalJobNull()
    )

    val currentBackStackEntryAsState by navController.currentBackStackEntryAsState()

    LaunchedEffect(currentBackStackEntryAsState) {
        lastEntry = navController.currentBackStackEntry?.destination?.route
    }


    NavHost(
        navController = navController,
        startDestination = defaultValue
    ) {

        slideComposable<ManageLocalJobRoutes.ManageLocalJob> {
            ManageLocalJobScreen(
                navController,
                publishedLocalJobViewModel,
                {
                    localJobsWorkflowViewModel.clearSelectedDraft()
                    navController.navigate(ManageLocalJobRoutes.CreateLocalJob)
                }, {
                    navController.navigate(ManageLocalJobRoutes.ManagePublishedLocalJob)
                },
                {
                    navController.navigate(ManageLocalJobRoutes.ViewApplicantsPublishedLocalJob)
                },
                onFinishActivity
            )
        }

        slideComposable<ManageLocalJobRoutes.CreateLocalJob> {
            CreateLocalJobScreen({
                navController.previousBackStackEntry?.savedStateHandle?.set(
                    "is_local_job_created",
                    true
                )
                navController.popBackStack()
            }, {
                navController.popBackStack()
            }, localJobsWorkflowViewModel)
        }


        slideComposable<ManageLocalJobRoutes.ManagePublishedLocalJob> {

            val editableSelectedUsedProductListing by publishedLocalJobViewModel.selectedItem.collectAsState()

            editableSelectedUsedProductListing?.let {

                ManagePublishedLocalJobScreen(
                    {
                        navController.popBackStack()
                    },
                    publishedLocalJobViewModel
                )

            }

        }

        slideComposable<ManageLocalJobRoutes.ViewApplicantsPublishedLocalJob> {

            val editableSelectedUsedProductListing by publishedLocalJobViewModel.selectedItem.collectAsState()

            if (editableSelectedUsedProductListing == null) return@slideComposable

            ManagePublishedLocalJobApplicantsScreen(
                {
                    navController.popBackStack()
                },
                publishedLocalJobViewModel
            )

        }

    }
}



