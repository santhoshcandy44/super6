package com.super6.pot.ui.main.navhosts

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.compose.NavHost
import androidx.navigation.toRoute
import com.super6.pot.app.database.models.chat.ChatUser
import com.super6.pot.ui.auth.navhost.noTransitionComposable
import com.super6.pot.ui.auth.navhost.slideComposable
import com.super6.pot.ui.chat.viewmodels.ChatListViewModel
import com.super6.pot.ui.chat.ChatUsersScreen
import com.super6.pot.ui.dropUnlessResumedV2
import com.super6.pot.ui.main.navhosts.routes.DetailedService
import com.super6.pot.ui.main.navhosts.routes.FeedUserDetailedService
import com.super6.pot.ui.main.navhosts.routes.FeedUserImagesSliderDetailedService
import com.super6.pot.ui.main.HomeScreen
import com.super6.pot.ui.main.navhosts.routes.ImagesSliderDetailedService
import com.super6.pot.ui.main.MoreScreen
import com.super6.pot.ui.main.NotificationScreen
import com.super6.pot.ui.main.navhosts.routes.BottomBarScreen
import com.super6.pot.ui.main.navhosts.routes.ServiceOwnerProfile
import com.super6.pot.ui.main.navhosts.routes.UserProfileSerializer
import com.super6.pot.ui.main.profile.ServiceOwnerProfileScreen
import com.super6.pot.ui.main.viewmodels.HomeViewModel
import com.super6.pot.ui.services.DetailedServiceScreen
import com.super6.pot.ui.services.FeedUserDetailedServiceInfoScreen
import com.super6.pot.ui.services.FeedUserImagesSliderScreen
import com.super6.pot.ui.services.ImagesSliderScreen
import com.super6.pot.ui.services.ServiceOwnerProfileViewModel
import com.super6.pot.ui.viewmodels.MoreViewModel
import com.super6.pot.ui.viewmodels.NotificationViewModel
import com.super6.pot.ui.viewmodels.ServicesViewModel

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
    showChooseIndustriesSheet: () -> Unit
) {


    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = BottomBarScreen.Home(),
    ) {


        noTransitionComposable<BottomBarScreen.Home> { backstackEntry ->

            val args = backstackEntry.toRoute<BottomBarScreen.Home>()

            val servicesViewModel: ServicesViewModel = hiltViewModel(key = args.key.toString())

            HomeScreen(
                navController,
                {
                    navController.navigate(DetailedService(args.key))
                },
                { serviceOwnerId ->
                    navController.navigate(
                        ServiceOwnerProfile(serviceOwnerId, args.key),
                        NavOptions.Builder().setLaunchSingleTop(true).build()
                    )
                },
                { showChooseIndustriesSheet() },
                { dropUnlessResumedV2(backstackEntry){navController.popBackStack()}},
                homeViewModel,
                servicesViewModel
            )
        }



        noTransitionComposable<BottomBarScreen.NestedHome> { backstackEntry ->

            val args = backstackEntry.toRoute<BottomBarScreen.NestedHome>()

            val servicesViewModel: ServicesViewModel = hiltViewModel(key = args.key.toString())

            HomeScreen(
                navController,
                {
                    navController.navigate(DetailedService(args.key))
                },
                { serviceOwnerId ->
                    navController.navigate(
                        ServiceOwnerProfile(serviceOwnerId, args.key),
                        NavOptions.Builder().setLaunchSingleTop(true)
                            .build()
                    )
                },
                { showChooseIndustriesSheet() },
                { dropUnlessResumedV2(backstackEntry){navController.popBackStack()}},
                homeViewModel,
                servicesViewModel
            )
        }


        slideComposable<DetailedService> { backStackEntry ->


            val args = backStackEntry.toRoute<DetailedService>()
            val key = args.key

            val parentBackStackEntry = if (key == 0) {
                navController.getBackStackEntry<BottomBarScreen.Home>()
            } else navController.getBackStackEntry<BottomBarScreen.NestedHome>()

            val viewModel: ServicesViewModel =
                hiltViewModel(parentBackStackEntry, key = key.toString())
            val selectedItem by viewModel.selectedItem.collectAsState()

            selectedItem?.let {
                DetailedServiceScreen(
                    key,
                    navController,
                    onNavigateUpSlider = {
                        navController.navigate(ImagesSliderDetailedService(key, it))
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


        slideComposable<ImagesSliderDetailedService> {


            val args = it.toRoute<ImagesSliderDetailedService>()
            val selectedImagePosition = args.selectedImagePosition
            val key = args.key

            val parentBackStackEntry = if (key == 0) {
                navController.getBackStackEntry<BottomBarScreen.Home>()
            } else navController.getBackStackEntry<BottomBarScreen.NestedHome>()

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


        slideComposable<ServiceOwnerProfile> { backStackEntry ->
            val key = backStackEntry.toRoute<ServiceOwnerProfile>().key

            val parentBackStackEntry = if (key == 0) {
                navController.getBackStackEntry<BottomBarScreen.Home>()
            } else navController.getBackStackEntry<BottomBarScreen.NestedHome>()


            val servicesViewModel: ServicesViewModel = hiltViewModel(
                parentBackStackEntry,
                key.toString()
            )
            val selectedItem by servicesViewModel.selectedItem.collectAsState()

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
                        navController.navigate(FeedUserDetailedService(it))
                    }, servicesViewModel
                )
            }

        }


        slideComposable<FeedUserDetailedService> { backStackEntry ->
            val args = backStackEntry.toRoute<FeedUserDetailedService>()

            val key = args.key

            val parentBackStackEntry = if (key == 0) {
                navController.getBackStackEntry<BottomBarScreen.Home>()
            } else navController.getBackStackEntry<BottomBarScreen.NestedHome>()


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
                        navController.navigate(FeedUserImagesSliderDetailedService(it))
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


        slideComposable<FeedUserImagesSliderDetailedService> {

            val selectedImagePosition =
                it.toRoute<FeedUserImagesSliderDetailedService>().selectedImagePosition
            val serviceOwnerProfileViewModel: ServiceOwnerProfileViewModel =
                hiltViewModel(navController.getBackStackEntry<ServiceOwnerProfile>())

            FeedUserImagesSliderScreen(
                navController,
                selectedImagePosition,
                serviceOwnerProfileViewModel
            ) { navController.popBackStack() }
        }



        noTransitionComposable<BottomBarScreen.Chats> {
            ChatUsersScreen(
                navController, { chatUser, chatId, recipientId, feedUserProfile ->
                    onNavigateUpChatScreen(
                        chatUser,
                        chatId,
                        recipientId,
                        UserProfileSerializer.serializeFeedUserProfileInfo(feedUserProfile)
                    )
                    /*
                                        navController.navigate(ChatWindow(chatId, recipientId, null, UserProfileSerializer.serializeFeedUserProfileInfo(feedUserProfile)))
                    */

                    /*             context.startActivity(
                                     Intent(context, ChatActivity::class.java)
                                         .apply {
                                             putExtra("from", "chat")
                                             putExtra("chat_id", chatId)
                                             putExtra("recipient_id", recipientId)
                                             putExtra("feed_user_profile",UserProfileSerializer.serializeFeedUserProfileInfo(feedUserProfile) )
                                         })*/
                }, isSheetExpanded,
                collapseSheet,
                chatListViewModel
            )
        }



        noTransitionComposable<BottomBarScreen.Notifications> {
            NotificationScreen(
                navController,
                isSheetExpanded,
                collapseSheet,
                notificationViewModel
            )
        }

        noTransitionComposable<BottomBarScreen.More> {
            MoreScreen(navController, onProfileNavigateUp = {
                onProfileNavigateUp()
            }, onAccountAndProfileSettingsNavigateUp = { accountType ->
                onAccountAndProfileSettingsNavigateUp(accountType)
            }, onManageIndustriesAndInterestsNavigateUp = {
                onManageIndustriesAndInterestsNavigateUp()
            }, onManageServiceNavigateUp = {
                onManageServiceNavigateUp()
            }, onNavigateUpBookmarkedServices = {
                onNavigateUpBookmarkedServices()
            }, moreViewModel,
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