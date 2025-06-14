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
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSavedStateNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.ui.rememberSceneSetupNavEntryDecorator
import com.lts360.compose.ui.localjobs.manage.navhost.ManageLocalJobRoutes
import com.lts360.compose.ui.localjobs.manage.viewmodels.LocalJobWorkFlowViewModel
import com.lts360.compose.ui.localjobs.manage.viewmodels.PublishedLocalJobViewModel
import com.lts360.compose.ui.main.navhosts.routes.AccountAndProfileSettingsRoutes
import com.lts360.compose.ui.profile.EditProfileSettingsScreen
import com.lts360.compose.ui.profile.viewmodels.ProfileSettingsViewModel
import com.lts360.compose.ui.theme.AppTheme
import com.lts360.compose.utils.SafeDrawingBox
import org.koin.androidx.compose.koinViewModel


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
    val localJobsWorkflowViewModel: LocalJobWorkFlowViewModel = koinViewModel()
    val publishedLocalJobViewModel: PublishedLocalJobViewModel = koinViewModel()
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
            entry<ManageLocalJobRoutes.ManageLocalJob> {
                ManageLocalJobScreen(
                    onAddNewLocalJobClick = {
                        localJobsWorkflowViewModel.clearSelectedDraft()
                        backStack.add(ManageLocalJobRoutes.CreateLocalJob)
                    },
                    onNavigateManagePublishedLocalJob = {
                        backStack.add(ManageLocalJobRoutes.ManagePublishedLocalJob)
                    },
                    onNavigatePublishedLocalJobViewApplicants = {
                        backStack.add(ManageLocalJobRoutes.ViewApplicantsPublishedLocalJob)
                    },
                    onNavigateProfileSettings = {
                        backStack.add(AccountAndProfileSettingsRoutes.PersonalSettings)
                    },
                    onPopBackStack = onFinishActivity,
                    viewModel = publishedLocalJobViewModel
                )
            }

            entry<ManageLocalJobRoutes.CreateLocalJob> {
                CreateLocalJobScreen(
                    onLocalJobCreated = {
                        backStack.removeLastOrNull()
                    },
                    onPopBackStack = {
                        backStack.removeLastOrNull()
                    },
                    viewModel = localJobsWorkflowViewModel
                )
            }

            entry<ManageLocalJobRoutes.ManagePublishedLocalJob> {
                val selectedItem by publishedLocalJobViewModel.selectedItem.collectAsState()
                if (selectedItem != null) {
                    ManagePublishedLocalJobScreen(
                        onPopBackStack = { backStack.removeLastOrNull() },
                        viewModel = publishedLocalJobViewModel
                    )
                }
            }

            entry<ManageLocalJobRoutes.ViewApplicantsPublishedLocalJob> {
                val selectedItem by publishedLocalJobViewModel.selectedItem.collectAsState()
                if (selectedItem != null) {
                    ManagePublishedLocalJobApplicantsScreen(
                        onPopBackStack = { backStack.removeLastOrNull() },
                        viewModel = publishedLocalJobViewModel
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
                    onPopBakStack = { backStack.removeLastOrNull() },
                    viewModel = profileSettingViewModel
                )
            }
        }
    )
}
