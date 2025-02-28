package com.lts360.compose.ui.main.navhosts

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.compose.NavHost
import androidx.navigation.toRoute
import com.lts360.app.database.models.chat.ChatUser
import com.lts360.compose.dropUnlessResumedV2
import com.lts360.compose.ui.auth.navhost.noTransitionComposable
import com.lts360.compose.ui.auth.navhost.slideComposable
import com.lts360.compose.ui.chat.ChatUsersScreen
import com.lts360.compose.ui.chat.viewmodels.ChatListViewModel
import com.lts360.compose.ui.main.HomeScreen
import com.lts360.compose.ui.main.MoreScreen
import com.lts360.compose.ui.main.NotificationScreen
import com.lts360.compose.ui.main.navhosts.routes.BottomBar
import com.lts360.compose.ui.main.navhosts.routes.BottomNavRoutes
import com.lts360.compose.ui.main.navhosts.routes.UserProfileSerializer
import com.lts360.compose.ui.main.profile.ServiceOwnerProfileScreen
import com.lts360.compose.ui.main.viewmodels.HomeViewModel
import com.lts360.compose.ui.main.viewmodels.SecondsViewmodel
import com.lts360.compose.ui.services.DetailedServiceScreen
import com.lts360.compose.ui.services.FeedUserDetailedServiceInfoScreen
import com.lts360.compose.ui.services.FeedUserImagesSliderScreen
import com.lts360.compose.ui.services.ImagesSliderScreen
import com.lts360.compose.ui.services.ServiceOwnerProfileViewModel
import com.lts360.compose.ui.theme.ThemeModeSettingsActivity
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
    navController: NavHostController,
    modifier: Modifier = Modifier,
    onProfileNavigateUp: () -> Unit,
    onAccountAndProfileSettingsNavigateUp: (String) -> Unit,
    onManageIndustriesAndInterestsNavigateUp: () -> Unit,
    onManageServiceNavigateUp: () -> Unit,
    onNavigateUpBookmarkedServices: () -> Unit,
    onNavigateUpWelcomeScreenSheet: () -> Unit,
    onNavigateUpLogInSheet: () -> Unit,
    isSheetExpanded: Boolean,
    collapseSheet: () -> Unit,
    onNavigateUpGuestManageIndustriesAndInterests: () -> Unit = {},
    onNavigateUpChatScreen: (ChatUser, Int, Long, String) -> Unit,
    showChooseIndustriesSheet: () -> Unit,
    onDockedFabAddNewSecondsChanged: (Boolean) -> Unit

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

            LaunchedEffect(backstackEntry) {
                homeViewModel.setSearchQuery(args.submittedQuery ?: "")
                homeViewModel.collapseSearchAction()
            }


            HomeScreen(
                navController,
                {
                    navController.navigate(BottomNavRoutes.DetailedService(args.key))
                },
                {
                    navController.navigate(BottomNavRoutes.DetailedSeconds(args.key))
                },
                { serviceOwnerId ->
                    navController.navigate(
                        BottomNavRoutes.ServiceOwnerProfile(serviceOwnerId, args.key),
                        NavOptions.Builder().setLaunchSingleTop(true).build()
                    )
                },
                { showChooseIndustriesSheet() },
                { dropUnlessResumedV2(backstackEntry) { navController.popBackStack() } },
                homeViewModel,
                servicesViewModel,
                secondsViewmodel,
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
            val secondsViewmodel: SecondsViewmodel = hiltViewModel(key = "seconds_${args.key}")
            Column {

                Text("Service: ${args.key}")
                HomeScreen(
                    navController,
                    {
                        navController.navigate(BottomNavRoutes.DetailedService(args.key))
                    },
                    {
                        navController.navigate(BottomNavRoutes.DetailedSeconds(args.key))
                    },
                    { serviceOwnerId ->
                        navController.navigate(
                            BottomNavRoutes.ServiceOwnerProfile(serviceOwnerId, args.key),
                            NavOptions.Builder().setLaunchSingleTop(true).build()
                        )
                    },
                    { showChooseIndustriesSheet() },
                    { dropUnlessResumedV2(backstackEntry) { navController.popBackStack() } },
                    homeViewModel,
                    servicesViewModel,
                    secondsViewmodel,
                    onDockedFabAddNewSecondsChanged,
                    args.onlySearchBar,
                    "Services",

                    )

            }

        }

        slideComposable<BottomNavRoutes.DetailedService> { backStackEntry ->


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
                    key,
                    navController,
                    onNavigateUpSlider = {
                        navController.navigate(BottomNavRoutes.DetailedServiceImagesSlider(key, it))
                    }, navigateUpChat = { chatUser, chatId, recipientId, feedUserProfile ->
                        onNavigateUpChatScreen(
                            chatUser,
                            chatId,
                            recipientId,
                            UserProfileSerializer.serializeFeedUserProfileInfo(feedUserProfile)
                        )
                    },
                    {
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
                    key,
                    navController,
                    selectedImagePosition,
                    viewModel
                ) { navController.popBackStack() }
            }

        }

        slideComposable<BottomNavRoutes.ServiceOwnerProfile> { backStackEntry ->

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
                    onNavigateUpChat = { chatUser, chatId, recipientId, feedUserProfile ->
                        onNavigateUpChatScreen(
                            chatUser,
                            chatId,
                            recipientId,
                            UserProfileSerializer.serializeFeedUserProfileInfo(feedUserProfile)
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
                    key,
                    {
                        navController.navigate(
                            BottomNavRoutes.DetailedServiceFeedUserImagesSlider(
                                it
                            )
                        )
                    },
                    { chatUser, chatId, recipientId, feedUserProfile ->

                        onNavigateUpChatScreen(
                            chatUser,
                            chatId,
                            recipientId,
                            UserProfileSerializer.serializeFeedUserProfileInfo(feedUserProfile)
                        )

                    }, {}, servicesViewModel
                )
            }


        }

        slideComposable<BottomNavRoutes.DetailedServiceFeedUserImagesSlider> {

            val selectedImagePosition =
                it.toRoute<BottomNavRoutes.DetailedServiceFeedUserImagesSlider>().selectedImagePosition
            val serviceOwnerProfileViewModel: ServiceOwnerProfileViewModel =
                hiltViewModel(remember { navController.getBackStackEntry<BottomNavRoutes.ServiceOwnerProfile>() })

            FeedUserImagesSliderScreen(
                navController,
                selectedImagePosition,
                serviceOwnerProfileViewModel
            ) { navController.popBackStack() }
        }


        noTransitionComposable<BottomBar.NestedSeconds> { backstackEntry ->

            val args = backstackEntry.toRoute<BottomBar.NestedSeconds>()

            val servicesViewModel: ServicesViewModel = hiltViewModel(key = args.key.toString())
            val secondsViewmodel: SecondsViewmodel = hiltViewModel(key = "seconds_${args.key}")

            LaunchedEffect(backstackEntry) {
                homeViewModel.setSearchQuery(args.submittedQuery ?: "")
                homeViewModel.collapseSearchAction()
            }

            HomeScreen(
                navController,
                {
                    navController.navigate(BottomNavRoutes.DetailedService(args.key))
                },
                {
                    navController.navigate(BottomNavRoutes.DetailedSeconds(args.key))
                },
                { serviceOwnerId ->
                    navController.navigate(
                        BottomNavRoutes.ServiceOwnerProfile(serviceOwnerId, args.key),
                        NavOptions.Builder().setLaunchSingleTop(true).build()
                    )
                },
                { showChooseIndustriesSheet() },
                { dropUnlessResumedV2(backstackEntry) { navController.popBackStack() } },
                homeViewModel,
                servicesViewModel,
                secondsViewmodel,
                onDockedFabAddNewSecondsChanged,
                args.onlySearchBar,
                "Second Hands"
            )

        }

        slideComposable<BottomNavRoutes.DetailedSeconds> { backStackEntry ->


            val args = backStackEntry.toRoute<BottomNavRoutes.DetailedSeconds>()
            val key = args.key

            val parentBackStackEntry = remember {
                if (key == 0) {
                    navController.getBackStackEntry<BottomBar.Home>()
                } else navController.getBackStackEntry<BottomBar.NestedSeconds>()
            }


            val viewModel: SecondsViewmodel = hiltViewModel(parentBackStackEntry, key = "seconds_${key}")

            val selectedItem by viewModel.selectedItem.collectAsState()

            selectedItem?.let {
                DetailedUsedProductListingScreen(
                    key,
                    navController,
                    onNavigateUpSlider = {
                        navController.navigate(BottomNavRoutes.DetailedSecondsImagesSlider(key, it))
                    }, navigateUpChat = { chatUser, chatId, recipientId, feedUserProfile ->
                        onNavigateUpChatScreen(
                            chatUser,
                            chatId,
                            recipientId,
                            UserProfileSerializer.serializeFeedUserProfileInfo(feedUserProfile)
                        )
                    },
                    {

                    },
                    { serviceOwnerId ->
                        navController.navigate(
                            BottomNavRoutes.SecondsOwnerProfile(serviceOwnerId, args.key),
                            NavOptions.Builder().setLaunchSingleTop(true).build()
                        )
                    },
                    viewModel
                )
            } ?: run {
                Box(modifier=Modifier
                    .fillMaxSize()
                    .background(Color.Yellow)){

                    Text("${key}")
                }
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
                    key,
                    navController,
                    selectedImagePosition,
                    viewModel
                ) { navController.popBackStack() }
            }

        }

        slideComposable<BottomNavRoutes.SecondsOwnerProfile> { backStackEntry ->
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
                    onNavigateUpChat = { chatUser, chatId, recipientId, feedUserProfile ->
                        onNavigateUpChatScreen(
                            chatUser,
                            chatId,
                            recipientId,
                            UserProfileSerializer.serializeFeedUserProfileInfo(feedUserProfile)
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
                    key,
                    {
                        navController.navigate(
                            BottomNavRoutes.DetailedSecondsFeedUserImagesSlider(
                                it
                            )
                        )
                    },
                    { chatUser, chatId, recipientId, feedUserProfile ->

                        onNavigateUpChatScreen(
                            chatUser,
                            chatId,
                            recipientId,
                            UserProfileSerializer.serializeFeedUserProfileInfo(feedUserProfile)
                        )

                    }, {}, viewmodel
                )
            }


        }

        slideComposable<BottomNavRoutes.DetailedSecondsFeedUserImagesSlider> {

            val selectedImagePosition =
                it.toRoute<BottomNavRoutes.DetailedSecondsFeedUserImagesSlider>().selectedImagePosition
            val viewmodel: SecondsOwnerProfileViewModel =
                hiltViewModel(remember { navController.getBackStackEntry<BottomNavRoutes.SecondsOwnerProfile>() })

            FeedUserSecondsImagesSliderScreen(
                navController,
                selectedImagePosition,
                viewmodel
            ) { navController.popBackStack() }
        }



        noTransitionComposable<BottomBar.Chats> {
            ChatUsersScreen(
                navController, { chatUser, chatId, recipientId, feedUserProfile ->
                    onNavigateUpChatScreen(
                        chatUser,
                        chatId,
                        recipientId,
                        UserProfileSerializer.serializeFeedUserProfileInfo(feedUserProfile)
                    )
                }, isSheetExpanded,
                collapseSheet,
                chatListViewModel
            )
        }



        noTransitionComposable<BottomBar.Notifications> {
            NotificationScreen(
                navController,
                isSheetExpanded,
                collapseSheet,
                notificationViewModel
            )
        }

        noTransitionComposable<BottomBar.More> {

            MoreScreen(navController, onProfileNavigateUp = {
                onProfileNavigateUp()
            }, onAccountAndProfileSettingsNavigateUp = { accountType ->
                onAccountAndProfileSettingsNavigateUp(accountType)
            }, onManageIndustriesAndInterestsNavigateUp = {
                onManageIndustriesAndInterestsNavigateUp()
            }, onManageServiceNavigateUp = {
                onManageServiceNavigateUp()
            }, onManageSecondsNavigateUp = {
                context.startActivity(
                    Intent(
                        context,
                        UsedProductListingActivity::class.java
                    ).apply {
                        flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                    })
            },
                onNavigateUpBookmarks = {
                    onNavigateUpBookmarkedServices()
                },
                onNavigateUpThemeModeSettings = {
                    context.startActivity(
                        Intent(
                            context,
                            ThemeModeSettingsActivity::class.java
                        ).apply {
                            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                        })
                },
                moreViewModel,
                onNavigateUpWelcomeScreenSheet,
                onNavigateUpLogInSheet,
                onNavigateUpGuestManageIndustriesAndInterests = {
                    onNavigateUpGuestManageIndustriesAndInterests()
                }, isSheetExpanded,
                collapseSheet
            )


        }

    }


}