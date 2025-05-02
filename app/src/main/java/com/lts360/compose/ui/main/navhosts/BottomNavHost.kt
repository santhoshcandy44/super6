package com.lts360.compose.ui.main.navhosts

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.compose.NavHost
import androidx.navigation.toRoute
import com.lts360.app.database.models.app.Board
import com.lts360.app.database.models.chat.ChatUser
import com.lts360.compose.dropUnlessResumedV2
import com.lts360.compose.ui.auth.navhost.noTransitionComposable
import com.lts360.compose.ui.auth.navhost.slideComposable
import com.lts360.compose.ui.chat.ChatUsersScreen
import com.lts360.compose.ui.chat.viewmodels.ChatListViewModel
import com.lts360.compose.ui.localjobs.DetailedLocalJobScreen
import com.lts360.compose.ui.localjobs.LocalJobsViewmodel
import com.lts360.compose.ui.localjobs.manage.LocalJobsActivity
import com.lts360.compose.ui.localjobs.manage.LocalJobsImagesSliderScreen
import com.lts360.compose.ui.main.HomeScreen
import com.lts360.compose.ui.main.MoreScreen
import com.lts360.compose.ui.main.NestedLocalJobsScreen
import com.lts360.compose.ui.main.NestedSecondsScreen
import com.lts360.compose.ui.main.NestedServicesScreen
import com.lts360.compose.ui.main.NotificationScreen
import com.lts360.compose.ui.main.navhosts.routes.BottomBar
import com.lts360.compose.ui.main.navhosts.routes.BottomNavRoutes
import com.lts360.compose.ui.main.prefs.BoardsSetupActivity
import com.lts360.compose.ui.main.profile.ServiceOwnerProfileScreen
import com.lts360.compose.ui.main.viewmodels.HomeViewModel
import com.lts360.compose.ui.main.viewmodels.SecondsViewmodel
import com.lts360.compose.ui.services.DetailedServiceScreen
import com.lts360.compose.ui.services.FeedUserDetailedServiceInfoScreen
import com.lts360.compose.ui.services.FeedUserImagesSliderScreen
import com.lts360.compose.ui.services.ImagesSliderScreen
import com.lts360.compose.ui.services.ServiceOwnerProfileViewModel
import com.lts360.compose.ui.settings.SettingsActivity
import com.lts360.compose.ui.usedproducts.DetailedUsedProductListingScreen
import com.lts360.compose.ui.usedproducts.FeedUserDetailedSecondsInfoScreen
import com.lts360.compose.ui.usedproducts.SecondsOwnerProfileViewModel
import com.lts360.compose.ui.usedproducts.manage.FeedUserSecondsImagesSliderScreen
import com.lts360.compose.ui.usedproducts.manage.SecondsImagesSliderScreen
import com.lts360.compose.ui.usedproducts.manage.SecondsServiceOwnerProfileScreen
import com.lts360.compose.ui.usedproducts.manage.UsedProductListingActivity
import com.lts360.compose.ui.viewmodels.MoreViewModel
import com.lts360.compose.ui.viewmodels.NotificationViewModel
import com.lts360.compose.ui.viewmodels.ServicesViewModel


@Composable
fun BottomNavHost(
    homeViewModel: HomeViewModel,
    chatListViewModel: ChatListViewModel,
    notificationViewModel: NotificationViewModel,
    moreViewModel: MoreViewModel,
    boards: List<Board>,
    navController: NavHostController,
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
    onNavigateUpGuestManageIndustriesAndInterests: () -> Unit = {}
) {


    val context = LocalContext.current


    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = BottomBar.Home(),
    ) {


        noTransitionComposable<BottomBar.Home> { backstackEntry ->

            val args = backstackEntry.toRoute<BottomBar.Home>()
            val servicesViewModel: ServicesViewModel = hiltViewModel(key = args.key.toString())
            val secondsViewmodel: SecondsViewmodel = hiltViewModel(key = "seconds_${args.key}")
            val localJobViewmodel: LocalJobsViewmodel = hiltViewModel(key = "local_jobs_${args.key}")

            LaunchedEffect(backstackEntry) {
                homeViewModel.setSearchQuery(args.submittedQuery ?: "")
                homeViewModel.collapseSearchAction()
            }

            HomeScreen(
                navController,
                boards,
                {
                    navController.navigate(BottomNavRoutes.DetailedService(args.key))
                },
                {
                    navController.navigate(BottomNavRoutes.DetailedSeconds(args.key))
                },
                {
                    navController.navigate(BottomNavRoutes.DetailedLocalJob(args.key))
                },
                { serviceOwnerId ->
                    navController.navigate(
                        BottomNavRoutes.ServiceOwnerProfile(serviceOwnerId, args.key),
                        NavOptions.Builder().setLaunchSingleTop(true).build()
                    )
                },
                { dropUnlessResumedV2(backstackEntry) { navController.popBackStack() } },
                homeViewModel,
                servicesViewModel,
                secondsViewmodel,
                localJobViewmodel,
                onDockedFabAddNewSecondsChanged
            )
        }

        noTransitionComposable<BottomBar.NestedServices> { backstackEntry ->

            val args = backstackEntry.toRoute<BottomBar.NestedServices>()

            LaunchedEffect(backstackEntry) {
                homeViewModel.setSearchQuery(args.submittedQuery ?: "")
                homeViewModel.collapseSearchAction()
            }

            val servicesViewModel: ServicesViewModel = hiltViewModel(key = args.key.toString())

            NestedServicesScreen(
                navController,
                {
                    navController.navigate(BottomNavRoutes.DetailedService(args.key))
                },
                { serviceOwnerId ->
                    navController.navigate(
                        BottomNavRoutes.ServiceOwnerProfile(serviceOwnerId, args.key),
                        NavOptions.Builder().setLaunchSingleTop(true).build()
                    )
                },
                { dropUnlessResumedV2(backstackEntry) { navController.popBackStack() } },
                homeViewModel,
                servicesViewModel,
                args.onlySearchBar
            )

        }

        noTransitionComposable<BottomNavRoutes.DetailedService> { backStackEntry ->


            val args = backStackEntry.toRoute<BottomNavRoutes.DetailedService>()
            val key = args.key
            val parentBackStackEntry = remember {
                if (key == 0) {
                    navController.getBackStackEntry<BottomBar.Home>()
                } else navController.getBackStackEntry<BottomBar.NestedServices>()
            }

            val viewModel: ServicesViewModel =
                hiltViewModel(parentBackStackEntry, key = key.toString())
            val selectedItem by viewModel.selectedItem.collectAsState()

            selectedItem?.let {
                DetailedServiceScreen(
                    navController,
                    onNavigateUpSlider = {
                        navController.navigate(BottomNavRoutes.DetailedServiceImagesSlider(key, it))
                    }, navigateUpChat = { chatUser, chatId, recipientId ->
                        onNavigateUpChatScreen(
                            chatUser,
                            chatId,
                            recipientId
                        )
                    },
                    viewModel
                )
            }
        }

        slideComposable<BottomNavRoutes.DetailedServiceImagesSlider> {


            val args = it.toRoute<BottomNavRoutes.DetailedServiceImagesSlider>()
            val selectedImagePosition = args.selectedImagePosition
            val key = args.key

            val parentBackStackEntry = remember {
                if (key == 0) {
                    navController.getBackStackEntry<BottomBar.Home>()
                } else navController.getBackStackEntry<BottomBar.NestedServices>()
            }
            val viewModel: ServicesViewModel = hiltViewModel(
                parentBackStackEntry,
                key = key.toString()
            )
            val selectedItem by viewModel.selectedItem.collectAsState()

            selectedItem?.let {
                ImagesSliderScreen(
                    selectedImagePosition,
                    viewModel
                ) { navController.popBackStack() }
            }

        }

        noTransitionComposable<BottomNavRoutes.ServiceOwnerProfile> { backStackEntry ->

            val key = backStackEntry.toRoute<BottomNavRoutes.ServiceOwnerProfile>().key

            val parentBackStackEntry = remember {
                if (key == 0) {
                    navController.getBackStackEntry<BottomBar.Home>()
                } else navController.getBackStackEntry<BottomBar.NestedServices>()
            }


            val servicesViewModel: ServicesViewModel = hiltViewModel(
                parentBackStackEntry,
                key.toString()
            )
            val selectedItem by servicesViewModel.selectedItem.collectAsState()

            val viewModel: ServiceOwnerProfileViewModel =
                hiltViewModel(remember { navController.getBackStackEntry<BottomNavRoutes.ServiceOwnerProfile>() })


            selectedItem?.let {
                ServiceOwnerProfileScreen(
                    navController,
                    key,
                    onNavigateUpChat = { chatUser, chatId, recipientId ->
                        onNavigateUpChatScreen(
                            chatUser,
                            chatId,
                            recipientId
                        )

                    },
                    {
                        navController.navigate(BottomNavRoutes.DetailedServiceFeedUser(it))
                    }, servicesViewModel,
                    viewModel
                )
            }

        }

        slideComposable<BottomNavRoutes.DetailedServiceFeedUser> { backStackEntry ->
            val args = backStackEntry.toRoute<BottomNavRoutes.DetailedServiceFeedUser>()

            val key = args.key

            val parentBackStackEntry = remember {
                if (key == 0) {
                    navController.getBackStackEntry<BottomBar.Home>()
                } else navController.getBackStackEntry<BottomBar.NestedServices>()
            }

            val servicesViewModel: ServicesViewModel = hiltViewModel(
                parentBackStackEntry,
                key.toString()
            )
            val selectedItem by servicesViewModel.selectedItem.collectAsState()

            selectedItem?.let {
                FeedUserDetailedServiceInfoScreen(
                    navController,
                    {
                        navController.navigate(
                            BottomNavRoutes.DetailedServiceFeedUserImagesSlider(
                                it
                            )
                        )
                    },
                    { chatUser, chatId, recipientId ->

                        onNavigateUpChatScreen(
                            chatUser,
                            chatId,
                            recipientId
                        )

                    }, servicesViewModel
                )
            }


        }

        slideComposable<BottomNavRoutes.DetailedServiceFeedUserImagesSlider> {

            val selectedImagePosition =
                it.toRoute<BottomNavRoutes.DetailedServiceFeedUserImagesSlider>().selectedImagePosition
            val serviceOwnerProfileViewModel: ServiceOwnerProfileViewModel =
                hiltViewModel(remember { navController.getBackStackEntry<BottomNavRoutes.ServiceOwnerProfile>() })

            FeedUserImagesSliderScreen(
                selectedImagePosition,
                serviceOwnerProfileViewModel
            ) { navController.popBackStack() }
        }

        noTransitionComposable<BottomBar.NestedSeconds> { backstackEntry ->

            val args = backstackEntry.toRoute<BottomBar.NestedSeconds>()
            val secondsViewmodel: SecondsViewmodel = hiltViewModel(key = "seconds_${args.key}")

            LaunchedEffect(backstackEntry) {
                homeViewModel.setSearchQuery(args.submittedQuery ?: "")
                homeViewModel.collapseSearchAction()
            }

            NestedSecondsScreen(
                navController,
                {
                    navController.navigate(BottomNavRoutes.DetailedSeconds(args.key))
                },

                { dropUnlessResumedV2(backstackEntry) { navController.popBackStack() } },
                homeViewModel,
                secondsViewmodel,
                args.onlySearchBar,
            )

        }

        noTransitionComposable<BottomNavRoutes.DetailedSeconds> { backStackEntry ->


            val args = backStackEntry.toRoute<BottomNavRoutes.DetailedSeconds>()
            val key = args.key

            val parentBackStackEntry = remember {
                if (key == 0) {
                    navController.getBackStackEntry<BottomBar.Home>()
                } else navController.getBackStackEntry<BottomBar.NestedSeconds>()
            }


            val viewModel: SecondsViewmodel =
                hiltViewModel(parentBackStackEntry, key = "seconds_${key}")

            val selectedItem by viewModel.selectedItem.collectAsState()

            selectedItem?.let {
                DetailedUsedProductListingScreen(
                    navController,
                    onNavigateUpSlider = {
                        navController.navigate(BottomNavRoutes.DetailedSecondsImagesSlider(key, it))
                    }, navigateUpChat = { chatUser, chatId, recipientId ->
                        onNavigateUpChatScreen(
                            chatUser,
                            chatId,
                            recipientId
                        )
                    },

                    { serviceOwnerId ->
                        navController.navigate(
                            BottomNavRoutes.SecondsOwnerProfile(serviceOwnerId, args.key),
                            NavOptions.Builder().setLaunchSingleTop(true).build()
                        )
                    },
                    viewModel
                )
            }
        }

        slideComposable<BottomNavRoutes.DetailedSecondsImagesSlider> {


            val args = it.toRoute<BottomNavRoutes.DetailedSecondsImagesSlider>()
            val selectedImagePosition = args.selectedImagePosition
            val key = args.key

            val parentBackStackEntry = remember {
                if (key == 0) {
                    navController.getBackStackEntry<BottomBar.Home>()
                } else navController.getBackStackEntry<BottomBar.NestedSeconds>()
            }
            val viewModel: SecondsViewmodel = hiltViewModel(
                parentBackStackEntry,
                key = "seconds_${key}"
            )
            val selectedItem by viewModel.selectedItem.collectAsState()

            selectedItem?.let {
                SecondsImagesSliderScreen(
                    selectedImagePosition,
                    viewModel
                ) { navController.popBackStack() }
            }

        }

        noTransitionComposable<BottomNavRoutes.SecondsOwnerProfile> { backStackEntry ->
            val key = backStackEntry.toRoute<BottomNavRoutes.SecondsOwnerProfile>().key

            val parentBackStackEntry = remember {
                if (key == 0) {
                    navController.getBackStackEntry<BottomBar.Home>()
                } else navController.getBackStackEntry<BottomBar.NestedSeconds>()
            }
            val secondsViewModel: SecondsViewmodel = hiltViewModel(
                parentBackStackEntry,
                key = "seconds_${key}"
            )

            val viewmodel: SecondsOwnerProfileViewModel =
                hiltViewModel(remember { navController.getBackStackEntry<BottomNavRoutes.SecondsOwnerProfile>() })

            val selectedItem by secondsViewModel.selectedItem.collectAsState()

            selectedItem?.let {
                SecondsServiceOwnerProfileScreen(
                    navController,
                    key,
                    onNavigateUpChat = { chatUser, chatId, recipientId ->
                        onNavigateUpChatScreen(
                            chatUser,
                            chatId,
                            recipientId
                        )

                    },
                    { key, usedProductListing ->
                        viewmodel.setSelectedItem(usedProductListing)
                        homeViewModel.setSelectedSecondsOwnerUsedProductListingIItem(
                            usedProductListing
                        )
                        navController.navigate(BottomNavRoutes.DetailedSecondsFeedUser(key))
                    }, secondsViewModel,
                    viewmodel
                )
            }

        }

        slideComposable<BottomNavRoutes.DetailedSecondsFeedUser> { backStackEntry ->
            val args = backStackEntry.toRoute<BottomNavRoutes.DetailedSecondsFeedUser>()

            val key = args.key

            val parentBackStackEntry = remember {
                if (key == 0) {
                    navController.getBackStackEntry<BottomBar.Home>()
                } else navController.getBackStackEntry<BottomBar.NestedSeconds>()

            }

            val viewmodel: SecondsViewmodel = hiltViewModel(
                parentBackStackEntry,
                key = "seconds_${key}"
            )
            val selectedItem by viewmodel.selectedItem.collectAsState()

            selectedItem?.let {
                FeedUserDetailedSecondsInfoScreen(
                    navController,
                    {
                        navController.navigate(
                            BottomNavRoutes.DetailedSecondsFeedUserImagesSlider(
                                it
                            )
                        )
                    },
                    { chatUser, chatId, recipientId ->

                        onNavigateUpChatScreen(
                            chatUser,
                            chatId,
                            recipientId
                        )

                    }
                )
            }


        }

        slideComposable<BottomNavRoutes.DetailedSecondsFeedUserImagesSlider> {

            val selectedImagePosition =
                it.toRoute<BottomNavRoutes.DetailedSecondsFeedUserImagesSlider>().selectedImagePosition
            val viewmodel: SecondsOwnerProfileViewModel =
                hiltViewModel(remember { navController.getBackStackEntry<BottomNavRoutes.SecondsOwnerProfile>() })

            FeedUserSecondsImagesSliderScreen(
                selectedImagePosition,
                viewmodel
            ) { navController.popBackStack() }
        }

        noTransitionComposable<BottomBar.NestedLocalJobs> { backstackEntry ->

            val args = backstackEntry.toRoute<BottomBar.NestedLocalJobs>()
            val localJobViewmodel: LocalJobsViewmodel = hiltViewModel(key = "local_jobs_${args.key}")

            LaunchedEffect(backstackEntry) {
                homeViewModel.setSearchQuery(args.submittedQuery ?: "")
                homeViewModel.collapseSearchAction()
            }

            NestedLocalJobsScreen(
                navController,

                {
                    navController.navigate(BottomNavRoutes.DetailedLocalJob(args.key))
                },

                { dropUnlessResumedV2(backstackEntry) { navController.popBackStack() } },
                homeViewModel,
                localJobViewmodel,
                args.onlySearchBar
            )

        }

        noTransitionComposable<BottomNavRoutes.DetailedLocalJob> { backStackEntry ->


            val args = backStackEntry.toRoute<BottomNavRoutes.DetailedLocalJob>()
            val key = args.key

            val parentBackStackEntry = remember {
                if (key == 0) navController.getBackStackEntry<BottomBar.Home>()
                else navController.getBackStackEntry<BottomBar.NestedLocalJobs>()
            }


            val viewModel: LocalJobsViewmodel = hiltViewModel(parentBackStackEntry,
                key = "local_jobs_${key}")

            val selectedItem by viewModel.selectedItem.collectAsState()

            selectedItem?.let {
                DetailedLocalJobScreen(
                    navController,
                    onNavigateUpSlider = {
                        navController.navigate(BottomNavRoutes.DetailedLocalJobsImagesSlider(key, it))
                    }, navigateUpChat = { chatUser, chatId, recipientId ->
                        onNavigateUpChatScreen(
                            chatUser,
                            chatId,
                            recipientId
                        )
                    },

                    viewModel
                )
            }
        }

        slideComposable<BottomNavRoutes.DetailedLocalJobsImagesSlider> {

            val args = it.toRoute<BottomNavRoutes.DetailedLocalJobsImagesSlider>()
            val selectedImagePosition = args.selectedImagePosition
            val key = args.key

            val parentBackStackEntry = remember {
                if (key == 0) {
                    navController.getBackStackEntry<BottomBar.Home>()
                } else navController.getBackStackEntry<BottomBar.NestedLocalJobs>()
            }
            val viewModel: LocalJobsViewmodel = hiltViewModel(
                parentBackStackEntry,
                key = "local_jobs_${key}"
            )
            val selectedItem by viewModel.selectedItem.collectAsState()

            selectedItem?.let {
                LocalJobsImagesSliderScreen (
                    selectedImagePosition,
                    viewModel
                ) { navController.popBackStack() }
            }

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
                onManageLocalJobNavigateUp ={
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