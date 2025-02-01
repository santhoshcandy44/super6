package com.super6.pot.ui.manage.services.navhost

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
import com.super6.pot.ui.manage.services.CreateServiceScreen
import com.super6.pot.ui.auth.navhost.slideComposable
import com.super6.pot.ui.manage.services.EditServiceThumbnailScreen
import com.super6.pot.ui.manage.services.EditServiceImagesScreen
import com.super6.pot.ui.manage.services.EditServiceInfoScreen
import com.super6.pot.ui.manage.services.EditServiceLocationScreen
import com.super6.pot.ui.manage.services.EditServicePlanScreen
import com.super6.pot.ui.manage.services.ManagePublishedServicesScreen
import com.super6.pot.ui.manage.services.ManageServicesScreen
import com.super6.pot.ui.manage.services.rememberManageServicesCustomBottomNavController
import com.super6.pot.ui.manage.services.viewmodels.ServicesWorkflowViewModel
import com.super6.pot.ui.manage.services.viewmodels.PublishedServicesViewModel


@Composable
fun ManageServicesNavHost(defaultValue: ManageServicesRoutes = ManageServicesRoutes.ManageServices, onFinishActivity:()->Unit) {

    var lastEntry by rememberSaveable { mutableStateOf<String?>(null) }
    val navController = rememberManageServicesCustomBottomNavController(lastEntry)

    val currentBackStackEntryAsState by navController.currentBackStackEntryAsState()

    LaunchedEffect(currentBackStackEntryAsState){
        lastEntry = navController.currentBackStackEntry?.destination?.route
    }

    val draftServicesViewModel: ServicesWorkflowViewModel =  hiltViewModel()
    val publishedServicesViewModel: PublishedServicesViewModel =  hiltViewModel()


    // Define the AnimatedNavHost
    NavHost(
        navController = navController,
        startDestination = defaultValue
    ) {
        // Entry Screen


        slideComposable<ManageServicesRoutes.ManageServices> {
            ManageServicesScreen(
                navController,
                {
                    navController.navigate(ManageServicesRoutes.CreateService)
                }, { type, draftId ->
                    draftServicesViewModel.updateDraftInfoAndLoadDraftDetails(type, draftId)
                    navController.navigate(ManageServicesRoutes.CreateService)
                }, {
                    navController.navigate(ManageServicesRoutes.ManagePublishedService)
                },onFinishActivity,
                draftServicesViewModel,
                publishedServicesViewModel)

        }

        slideComposable<ManageServicesRoutes.CreateService> {
            CreateServiceScreen(
                {
                    navController.previousBackStackEntry?.savedStateHandle?.set("is_service_created", true)
                    navController.popBackStack()
                },
                {
                    navController.popBackStack()
                },
                {
                    navController.popBackStack()
                },
                draftServicesViewModel)
        }

        slideComposable<ManageServicesRoutes.ManagePublishedService> {

            val viewModel: PublishedServicesViewModel = publishedServicesViewModel
            val editableService by viewModel.selectedService.collectAsState()

            editableService?.let {
                ManagePublishedServicesScreen(
                    navController,
                    {

                        navController.navigate(ManageServicesRoutes.ManagePublishedServiceInfo)
                    },
                    {
                        navController.navigate(ManageServicesRoutes.ManagePublishedServiceThumbnail)
                    },
                    {
                        navController.navigate(ManageServicesRoutes.ManagePublishedServiceImages)
                    },
                    {
                        navController.navigate(ManageServicesRoutes.ManagePublishedServicePlans)
                    },
                    {
                        navController.navigate(ManageServicesRoutes.ManagePublishedServiceLocation)

                    },
                    {
                        navController.popBackStack()
                    }, {
                        navController.popBackStack()
                    },viewModel)
            }

        }

        slideComposable<ManageServicesRoutes.ManagePublishedServiceInfo> {

            val viewModel: PublishedServicesViewModel = publishedServicesViewModel
            val editableService by viewModel.selectedService.collectAsState()
            editableService?.let {
                EditServiceInfoScreen(navController,{navController.popBackStack()}, viewModel)
            }
        }

        slideComposable<ManageServicesRoutes.ManagePublishedServiceThumbnail> {
            val viewModel: PublishedServicesViewModel = publishedServicesViewModel
            val editableService by viewModel.selectedService.collectAsState()
            editableService?.let {
                EditServiceThumbnailScreen(navController, {navController.popBackStack()}, viewModel)
            }

        }

        slideComposable<ManageServicesRoutes.ManagePublishedServicePlans> {
            val viewModel: PublishedServicesViewModel = publishedServicesViewModel
            val editableService by viewModel.selectedService.collectAsState()

            editableService?.let {
                EditServicePlanScreen(navController, {navController.popBackStack()}, viewModel)
            }
        }

        slideComposable<ManageServicesRoutes.ManagePublishedServiceImages> {
            val viewModel: PublishedServicesViewModel = publishedServicesViewModel
            val editableService by viewModel.selectedService.collectAsState()
            editableService?.let {
                EditServiceImagesScreen(navController, {navController.popBackStack()}, viewModel)
            }
        }

        slideComposable<ManageServicesRoutes.ManagePublishedServiceLocation> {
            val viewModel: PublishedServicesViewModel = publishedServicesViewModel
            val editableService by viewModel.selectedService.collectAsState()
            editableService?.let {
                EditServiceLocationScreen(navController, {navController.popBackStack()}, viewModel)
            }
        }

    }
}

