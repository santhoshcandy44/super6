package com.lts360.compose.ui.main.navhosts

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.toRoute
import com.lts360.app.database.models.app.Board
import com.lts360.app.database.models.chat.ChatUser
import com.lts360.compose.dropUnlessResumedV2
import com.lts360.compose.ui.auth.navhost.noTransitionComposable
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
    navController: NavHostController,
    homeViewModel: HomeViewModel,
    servicesViewModel: ServicesViewModel,
    secondsViewModel: SecondsViewmodel,
    localJobsViewModel: LocalJobsViewmodel,
    chatListViewModel: ChatListViewModel,
    notificationViewModel: NotificationViewModel,
    moreViewModel: MoreViewModel,
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
    onNavigateUpDetailedLocalJob: (Int) -> Unit

) {


    val context = LocalContext.current




    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = BottomBar.Home()
    ) {


        noTransitionComposable<BottomBar.Home> { backstackEntry ->

            val args = backstackEntry.toRoute<BottomBar.Home>()


            LaunchedEffect(backstackEntry) {
                homeViewModel.setSearchQuery(args.submittedQuery ?: "")
                homeViewModel.collapseSearchAction()
            }

            HomeScreen(
                navController,
                boards,
                {
                    onNavigateUpDetailedService(args.key)
                },
                { serviceOwnerId ->
                    onNavigateUpServiceOwnerProfile(args.key, serviceOwnerId)
                },
                {
                    onNavigateUpDetailedSeconds(args.key)
                },
                {
                    onNavigateUpDetailedLocalJob(args.key)
                },
                { dropUnlessResumedV2(backstackEntry) { navController.popBackStack() } },
                homeViewModel,
                servicesViewModel,
                secondsViewModel,
                localJobsViewModel,
                onDockedFabAddNewSecondsChanged
            )
        }

        noTransitionComposable<BottomBar.NestedServices> { backstackEntry ->

            val args = backstackEntry.toRoute<BottomBar.NestedServices>()
            LaunchedEffect(backstackEntry) {
                homeViewModel.setSearchQuery(args.submittedQuery ?: "")
                homeViewModel.collapseSearchAction()
            }

            NestedServicesScreen(
                navController,
                args.key,
                {
                    onNavigateUpDetailedService(args.key)
                },
                { serviceOwnerId ->
                    onNavigateUpServiceOwnerProfile(args.key, serviceOwnerId)
                },
                { dropUnlessResumedV2(backstackEntry) { navController.popBackStack() } },
                homeViewModel,
                servicesViewModel,
                args.onlySearchBar
            )

        }

        noTransitionComposable<BottomBar.NestedSeconds> { backstackEntry ->

            val args = backstackEntry.toRoute<BottomBar.NestedSeconds>()

            LaunchedEffect(backstackEntry) {
                homeViewModel.setSearchQuery(args.submittedQuery ?: "")
                homeViewModel.collapseSearchAction()
            }

            NestedSecondsScreen(
                navController,
                args.key,
                {
                    onNavigateUpDetailedSeconds(args.key)
                },

                { dropUnlessResumedV2(backstackEntry) { navController.popBackStack() } },
                homeViewModel,
                secondsViewModel,
                args.onlySearchBar,
            )

        }

        noTransitionComposable<BottomBar.NestedLocalJobs> { backstackEntry ->

            val args = backstackEntry.toRoute<BottomBar.NestedLocalJobs>()


            LaunchedEffect(backstackEntry) {
                homeViewModel.setSearchQuery(args.submittedQuery ?: "")
                homeViewModel.collapseSearchAction()
            }

            NestedLocalJobsScreen(
                navController,
                args.key,
                {
                    onNavigateUpDetailedLocalJob(args.key)
                },

                { dropUnlessResumedV2(backstackEntry) { navController.popBackStack() } },
                homeViewModel,
                localJobsViewModel,
                args.onlySearchBar
            )

        }



        noTransitionComposable<BottomBar.Chats> {
            ChatUsersScreen(
                navController, { chatUser, chatId, recipientId ->
                    onNavigateUpChatScreen(
                        chatUser,
                        chatId,
                        recipientId
                    )
                },
                chatListViewModel
            )
        }

        noTransitionComposable<BottomBar.Notifications> {
            NotificationScreen(
                navController,
                notificationViewModel
            )
        }

        noTransitionComposable<BottomBar.More> {

            MoreScreen(
                navController,
                boards,
                onProfileNavigateUp = onProfileNavigateUp,
                onAccountAndProfileSettingsNavigateUp = { accountType ->
                    onAccountAndProfileSettingsNavigateUp(accountType)
                },
                onSetupBoardsSettingsNavigateUp = {
                    context.startActivity(
                        Intent(
                            context,
                            BoardsSetupActivity::class.java
                        ).apply {
                            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                        })
                },
                onManageIndustriesAndInterestsNavigateUp = onManageIndustriesAndInterestsNavigateUp,
                onManageServiceNavigateUp = onManageServiceNavigateUp,
                onManageSecondsNavigateUp = {
                    context.startActivity(
                        Intent(
                            context,
                            UsedProductListingActivity::class.java
                        ).apply {
                            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                        })
                },
                onManageLocalJobNavigateUp = {
                    context.startActivity(
                        Intent(
                            context, LocalJobsActivity::class.java
                        ).apply {
                            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                        })
                },
                onNavigateUpBookmarks = onNavigateUpBookmarkedServices,
                onNavigateUpThemeModeSettings = {
                    context.startActivity(
                        Intent(
                            context,
                            SettingsActivity::class.java
                        ).apply {
                            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                        })
                },
                onNavigateUpGuestManageIndustriesAndInterests =
                    onNavigateUpGuestManageIndustriesAndInterests,
                onNavigateUpWelcomeScreenSheet = onNavigateUpWelcomeScreenSheet,
                onNavigateUpLogInSheet = onNavigateUpLogInSheet,
                viewModel = moreViewModel
            )
        }

    }
}