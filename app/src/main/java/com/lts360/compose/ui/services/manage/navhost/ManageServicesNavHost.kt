package com.lts360.compose.ui.services.manage.navhost

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.lts360.compose.ui.services.manage.CreateServiceScreen
import com.lts360.compose.ui.services.manage.EditServiceImagesScreen
import com.lts360.compose.ui.services.manage.EditServiceInfoScreen
import com.lts360.compose.ui.services.manage.EditServiceLocationScreen
import com.lts360.compose.ui.services.manage.EditServicePlanScreen
import com.lts360.compose.ui.services.manage.EditServiceThumbnailScreen
import com.lts360.compose.ui.services.manage.ManagePublishedServicesScreen
import com.lts360.compose.ui.services.manage.ManageServicesScreen
import com.lts360.compose.ui.services.manage.viewmodels.PublishedServicesViewModel
import com.lts360.compose.ui.services.manage.viewmodels.ServicesWorkflowViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun ManageServicesNavHost(
    defaultValue: ManageServicesRoutes = ManageServicesRoutes.ManageServices,
    onFinishActivity: () -> Unit
) {
    val draftServicesViewModel: ServicesWorkflowViewModel = koinViewModel()
    val publishedServicesViewModel: PublishedServicesViewModel = koinViewModel()
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
            entry<ManageServicesRoutes.ManageServices> {
                ManageServicesScreen(
                    onCreatedNewService = {
                        draftServicesViewModel.clearSelectedDraft()
                        backStack.add(ManageServicesRoutes.CreateService)
                    },
                    onNavigateUpCreateService = { type, draftId ->
                        draftServicesViewModel.updateDraftInfoAndLoadDraftDetails(type, draftId)
                        backStack.add(ManageServicesRoutes.CreateService)
                    },
                    onManagePublishedService = {
                        backStack.add(ManageServicesRoutes.ManagePublishedService)
                    },
                    onProfileSettings = {
                        backStack.add(AccountAndProfileSettingsRoutes.PersonalSettings)
                    },
                    onBack = onFinishActivity,
                    draftServicesViewModel = draftServicesViewModel,
                    publishedServicesViewModel = publishedServicesViewModel
                )
            }

            entry<ManageServicesRoutes.CreateService> {
                CreateServiceScreen(
                    onServiceCreated = {
                        backStack.removeLastOrNull()
                    },
                    onServiceDeleted = {
                        backStack.removeLastOrNull()
                    },
                    onBack = {
                        backStack.removeLastOrNull()
                    },
                    viewModel = draftServicesViewModel
                )
            }

            entry<ManageServicesRoutes.ManagePublishedService> {
                val viewModel = publishedServicesViewModel
                val editableService by viewModel.selectedService.collectAsState()

                if (editableService != null) {
                    ManagePublishedServicesScreen(
                        onNavigateUpManageServiceInfo = {
                            backStack.add(ManageServicesRoutes.ManagePublishedServiceInfo)
                        },
                        onNavigateUpManageServiceThumbnail = {
                            backStack.add(ManageServicesRoutes.ManagePublishedServiceThumbnail)
                        },
                        onNavigateUpManageServiceImages = {
                            backStack.add(ManageServicesRoutes.ManagePublishedServiceImages)
                        },
                        onNavigateUpManageServicePlans = {
                            backStack.add(ManageServicesRoutes.ManagePublishedServicePlans)
                        },
                        onNavigateUpManageServiceLocation = {
                            backStack.add(ManageServicesRoutes.ManagePublishedServiceLocation)
                        },
                        onPopBackStack = {
                            backStack.removeLastOrNull()
                        },
                        viewModel = viewModel
                    )
                }
            }

            entry<ManageServicesRoutes.ManagePublishedServiceInfo> {
                val viewModel = publishedServicesViewModel
                val editableService by viewModel.selectedService.collectAsState()
                if (editableService != null) {
                    EditServiceInfoScreen(
                        onPopBackStack = { backStack.removeLastOrNull() },
                        viewModel = viewModel
                    )
                }
            }

            entry<ManageServicesRoutes.ManagePublishedServiceThumbnail> {
                val viewModel = publishedServicesViewModel
                val editableService by viewModel.selectedService.collectAsState()
                if (editableService != null) {
                    EditServiceThumbnailScreen(
                        onPopBackStack = { backStack.removeLastOrNull() },
                        viewModel = viewModel
                    )
                }
            }

            entry<ManageServicesRoutes.ManagePublishedServicePlans> {
                val viewModel = publishedServicesViewModel
                val editableService by viewModel.selectedService.collectAsState()
                if (editableService != null) {
                    EditServicePlanScreen(
                        onPopBackStack = { backStack.removeLastOrNull() },
                        viewModel = viewModel
                    )
                }
            }

            entry<ManageServicesRoutes.ManagePublishedServiceImages> {
                val viewModel = publishedServicesViewModel
                val editableService by viewModel.selectedService.collectAsState()
                if (editableService != null) {
                    EditServiceImagesScreen(
                        onPopBackStack = { backStack.removeLastOrNull() },
                        viewModel = viewModel
                    )
                }
            }

            entry<ManageServicesRoutes.ManagePublishedServiceLocation> {
                val viewModel = publishedServicesViewModel
                val editableService by viewModel.selectedService.collectAsState()
                if (editableService != null) {
                    EditServiceLocationScreen(
                        onPopBackStack = { backStack.removeLastOrNull() },
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

