package com.lts360.compose.ui.services.manage.navhost

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
import com.lts360.compose.ui.services.manage.CreateServiceScreen
import com.lts360.compose.ui.services.manage.EditServiceImagesScreen
import com.lts360.compose.ui.services.manage.EditServiceInfoScreen
import com.lts360.compose.ui.services.manage.EditServiceLocationScreen
import com.lts360.compose.ui.services.manage.EditServicePlanScreen
import com.lts360.compose.ui.services.manage.EditServiceThumbnailScreen
import com.lts360.compose.ui.services.manage.ManagePublishedServicesScreen
import com.lts360.compose.ui.services.manage.ManageServicesScreen
import com.lts360.compose.ui.services.manage.rememberManageServicesCustomBottomNavController
import com.lts360.compose.ui.services.manage.viewmodels.PublishedServicesViewModel
import com.lts360.compose.ui.services.manage.viewmodels.ServicesWorkflowViewModel


@Composable
fun ManageServicesNavHost(defaultValue: ManageServicesRoutes = ManageServicesRoutes.ManageServices, onFinishActivity:()->Unit) {




    val draftServicesViewModel: ServicesWorkflowViewModel =  hiltViewModel()
    val publishedServicesViewModel: PublishedServicesViewModel =  hiltViewModel()



    val lastEntry by draftServicesViewModel.lastEntry.collectAsState()
    val navController = rememberManageServicesCustomBottomNavController(lastEntry, publishedServicesViewModel.isSelectedServiceNull())

    val currentBackStackEntryAsState by navController.currentBackStackEntryAsState()


    LaunchedEffect(currentBackStackEntryAsState){
        draftServicesViewModel.updateLastEntry(navController.currentBackStackEntry?.destination?.route)
    }


    NavHost(
        navController = navController,
        startDestination = defaultValue
    ) {

        slideComposable<ManageServicesRoutes.ManageServices> {
            ManageServicesScreen(
                navController,
                {
                    draftServicesViewModel.clearSelectedDraft()
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

