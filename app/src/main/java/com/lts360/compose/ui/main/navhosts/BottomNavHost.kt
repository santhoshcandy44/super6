package com.lts360.compose.ui.main.navhosts

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSavedStateNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.ui.rememberSceneSetupNavEntryDecorator
import com.lts360.app.database.models.app.Board
import com.lts360.app.database.models.chat.ChatUser
import com.lts360.compose.ui.chat.ChatUsersScreen
import com.lts360.compose.ui.chat.viewmodels.ChatListViewModel
import com.lts360.compose.ui.localjobs.LocalJobsViewmodel
import com.lts360.compose.ui.localjobs.manage.LocalJobsActivity
import com.lts360.compose.ui.main.HomeScreen
import com.lts360.compose.ui.main.MoreScreen
import com.lts360.compose.ui.main.NestedLocalJobsScreen
import com.lts360.compose.ui.main.NestedSecondsScreen
import com.lts360.compose.ui.main.NestedServicesScreen
import com.lts360.compose.ui.main.NotificationScreen
import com.lts360.compose.ui.main.navhosts.routes.BottomBar
import com.lts360.compose.ui.main.prefs.BoardsSetupActivity
import com.lts360.compose.ui.main.viewmodels.HomeViewModel
import com.lts360.compose.ui.main.viewmodels.SecondsViewmodel
import com.lts360.compose.ui.settings.SettingsActivity
import com.lts360.compose.ui.usedproducts.manage.UsedProductListingActivity
import com.lts360.compose.ui.viewmodels.MoreViewModel
import com.lts360.compose.ui.viewmodels.NotificationViewModel
import com.lts360.compose.ui.viewmodels.ServicesViewModel


@Composable
fun BottomNavHost(
    backStack: NavBackStack,
    boards: List<Board>,
    modifier: Modifier = Modifier,
    onProfileNavigateUp: () -> Unit,
    onAccountAndProfileSettingsNavigateUp: (String) -> Unit,
    onManageIndustriesAndInterestsNavigateUp: () -> Unit,
    onManageServiceNavigateUp: () -> Unit,
    onNavigateUpBookmarkedServices: () -> Unit,
    onNavigateUpWelcomeScreenSheet: () -> Unit,
    onNavigateUpLogInSheet: () -> Unit,
    onNavigateUpChatScreen: (ChatUser, Int, Long) -> Unit,
    onDockedFabAddNewSecondsChanged: (Boolean) -> Unit,
    onNavigateUpGuestManageIndustriesAndInterests: () -> Unit,
    onNavigateUpDetailedService: (Int) -> Unit,
    onNavigateUpServiceOwnerProfile: (Int, Long) -> Unit,
    onNavigateUpDetailedSeconds: (Int) -> Unit,
    onNavigateUpDetailedLocalJob: (Int) -> Unit,
    homeViewModel: HomeViewModel,
    servicesViewModel: ServicesViewModel,
    secondsViewModel: SecondsViewmodel,
    localJobsViewModel: LocalJobsViewmodel,
    chatListViewModel: ChatListViewModel,
    notificationViewModel: NotificationViewModel,
    moreViewModel: MoreViewModel,
) {
    val context = LocalContext.current

    NavDisplay(
        modifier = modifier,
        backStack = backStack,
        entryDecorators = listOf(
            rememberSceneSetupNavEntryDecorator(),
            rememberSavedStateNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        entryProvider = entryProvider {

            entry<BottomBar.Home> { entry ->
                val key = entry.key
                val submittedQuery = entry.submittedQuery

                LaunchedEffect(entry) {
                    homeViewModel.setSearchQuery(submittedQuery ?: "")
                    homeViewModel.collapseSearchAction()
                }

                HomeScreen(
                    backStack = backStack,
                    boardItems = boards,
                    onNavigateUpServiceDetailedScreen = { onNavigateUpDetailedService(key) },
                    onNavigateUpServiceOwnerProfile = { ownerId ->
                        onNavigateUpServiceOwnerProfile(key, ownerId)
                    },
                    onNavigateUpUsedProductListingDetailedScreen = {  onNavigateUpDetailedSeconds(key) },
                    onNavigateUpLocalJobDetailedScreen = { onNavigateUpDetailedLocalJob(key) },
                    onPopBackStack = { backStack.removeLastOrNull() },
                    viewModel = homeViewModel,
                    servicesViewModel = servicesViewModel,
                    secondsViewModel = secondsViewModel,
                    localJobsViewModel = localJobsViewModel,
                    onDockedFabAddNewSecondsVisibility = onDockedFabAddNewSecondsChanged
                )
            }

            entry<BottomBar.NestedServices> { entry ->

                val key = entry.key
                val onlySearchBar = entry.onlySearchBar
                val submittedQuery = entry.submittedQuery

                LaunchedEffect(entry) {
                    homeViewModel.setSearchQuery(submittedQuery ?: "")
                    homeViewModel.collapseSearchAction()
                }

                NestedServicesScreen(
                    backStack = backStack,
                    key = key,
                    onNavigateUpServiceDetailedScreen = { onNavigateUpDetailedService(key) },
                    onNavigateUpServiceOwnerProfile = { ownerId ->
                        onNavigateUpServiceOwnerProfile(key, ownerId)
                    },
                    onPopBackStack = { backStack.removeLastOrNull() },
                    viewModel = homeViewModel,
                    servicesViewModel = servicesViewModel,
                    onlySearchBar = onlySearchBar
                )
            }

            entry<BottomBar.NestedSeconds> { entry ->

                val key = entry.key
                val onlySearchBar = entry.onlySearchBar
                val submittedQuery = entry.submittedQuery

                LaunchedEffect(entry) {
                    homeViewModel.setSearchQuery(submittedQuery ?: "")
                    homeViewModel.collapseSearchAction()
                }

                NestedSecondsScreen(
                    backStack = backStack,
                    key = key,
                    onNavigateUpUsedProductListingDetailedScreen = { onNavigateUpDetailedSeconds(key) },
                    onPopBackStack = { backStack.removeLastOrNull() },
                    viewModel = homeViewModel,
                    secondsViewModel = secondsViewModel,
                    onlySearchBar = onlySearchBar
                )
            }

            entry<BottomBar.NestedLocalJobs> { entry ->

                val key = entry.key
                val onlySearchBar = entry.onlySearchBar
                val submittedQuery = entry.submittedQuery

                LaunchedEffect(entry) {
                    homeViewModel.setSearchQuery(submittedQuery ?: "")
                    homeViewModel.collapseSearchAction()
                }

                NestedLocalJobsScreen(
                    backStack = backStack,
                    key = key,
                    onNavigateUpLocalJobDetailedScreen = { onNavigateUpDetailedLocalJob(key) },
                    onPopBackStack = { backStack.removeLastOrNull() },
                    viewModel = homeViewModel,
                    localJobsViewModel = localJobsViewModel,
                    onlySearchBar = onlySearchBar
                )
            }

            entry<BottomBar.Chats> {
                ChatUsersScreen(
                    onNavigateUpChat = { chatUser, chatId, recipientId ->
                        onNavigateUpChatScreen(chatUser, chatId, recipientId)
                    },
                    viewModel = chatListViewModel
                )
            }

            entry<BottomBar.Notifications> {
                NotificationScreen(
                    viewModel = notificationViewModel
                )
            }

            entry<BottomBar.More> {
                MoreScreen(
                    boardItems = boards,
                    onProfileNavigateUp = onProfileNavigateUp,
                    onAccountAndProfileSettingsNavigateUp = onAccountAndProfileSettingsNavigateUp,
                    onSetupBoardsSettingsNavigateUp = {
                        context.startActivity(
                            Intent(context, BoardsSetupActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                            })
                    },
                    onManageIndustriesAndInterestsNavigateUp = onManageIndustriesAndInterestsNavigateUp,
                    onManageServiceNavigateUp = onManageServiceNavigateUp,
                    onManageSecondsNavigateUp = {
                        context.startActivity(
                            Intent(context, UsedProductListingActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                            })
                    },
                    onManageLocalJobNavigateUp = {
                        context.startActivity(
                            Intent(context, LocalJobsActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                            })
                    },
                    onNavigateUpBookmarks = onNavigateUpBookmarkedServices,
                    onNavigateUpThemeModeSettings = {
                        context.startActivity(
                            Intent(context, SettingsActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                            })
                    },
                    onNavigateUpGuestManageIndustriesAndInterests = onNavigateUpGuestManageIndustriesAndInterests,
                    onNavigateUpWelcomeScreenSheet = onNavigateUpWelcomeScreenSheet,
                    onNavigateUpLogInSheet = onNavigateUpLogInSheet,
                    viewModel = moreViewModel
                )
            }
        }
    )
}