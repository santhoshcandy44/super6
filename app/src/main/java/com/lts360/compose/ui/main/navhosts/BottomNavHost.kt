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
import com.lts360.compose.ui.main.navhosts.routes.DetailedSeconds
import com.lts360.compose.ui.main.navhosts.routes.DetailedSecondsFeedUser
import com.lts360.compose.ui.main.navhosts.routes.DetailedSecondsFeedUserImagesSlider
import com.lts360.compose.ui.main.navhosts.routes.DetailedService
import com.lts360.compose.ui.main.navhosts.routes.DetailedServiceFeedUser
import com.lts360.compose.ui.main.navhosts.routes.DetailedServiceFeedUserImagesSlider
import com.lts360.compose.ui.main.navhosts.routes.SecondsDetailedImagesSlider
import com.lts360.compose.ui.main.navhosts.routes.SecondsOwnerProfile
import com.lts360.compose.ui.main.navhosts.routes.ServiceDetailedImagesSlider
import com.lts360.compose.ui.main.navhosts.routes.ServiceOwnerProfile
import com.lts360.compose.ui.main.navhosts.routes.UserProfileSerializer
import com.lts360.compose.ui.main.profile.ServiceOwnerProfileScreen
import com.lts360.compose.ui.main.viewmodels.HomeViewModel
import com.lts360.compose.ui.main.viewmodels.SecondsViewmodel
import com.lts360.compose.ui.services.DetailedServiceScreen
import com.lts360.compose.ui.services.FeedUserDetailedServiceInfoScreen
import com.lts360.compose.ui.services.FeedUserImagesSliderScreen
import com.lts360.compose.ui.services.ImagesSliderScreen
import com.lts360.compose.ui.services.ServiceOwnerProfileViewModel
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
    onDockedFabAddNewSecondsChanged:(Boolean)-> Unit

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

            LaunchedEffect(backstackEntry){
                homeViewModel.setSearchQuery(args.submittedQuery ?: "")
            }


            HomeScreen(
                navController,
                {
                    navController.navigate(DetailedService(args.key))
                },
                {
                    navController.navigate(DetailedSeconds(args.key))
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
                servicesViewModel,
                secondsViewmodel,
                onDockedFabAddNewSecondsChanged

            )
        }


        noTransitionComposable<BottomBar.NestedServices> { backstackEntry ->




            val args = backstackEntry.toRoute<BottomBar.Home>()

            LaunchedEffect(backstackEntry){
                homeViewModel.setSearchQuery(args.submittedQuery ?: "")
            }


            val servicesViewModel: ServicesViewModel = hiltViewModel(key = args.key.toString())
            val secondsViewmodel: SecondsViewmodel = hiltViewModel(key = "seconds_${args.key}")


            HomeScreen(
                navController,
                {
                    navController.navigate(DetailedService(args.key))
                },
                {
                    navController.navigate(DetailedSeconds(args.key))
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
                servicesViewModel,
                secondsViewmodel,
                onDockedFabAddNewSecondsChanged,
                args.onlySearchBar,
                "Services",

            )
        }


        slideComposable<DetailedService> { backStackEntry ->


            val args = backStackEntry.toRoute<DetailedService>()
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
                        navController.navigate(ServiceDetailedImagesSlider(key, it))
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

        noTransitionComposable<BottomBar.NestedSeconds> { backstackEntry ->

            val args = backstackEntry.toRoute<BottomBar.NestedSeconds>()

            val servicesViewModel: ServicesViewModel = hiltViewModel(key = args.key.toString())
            val secondsViewmodel: SecondsViewmodel = hiltViewModel(key = "seconds_${args.key}")

            LaunchedEffect(backstackEntry){
                homeViewModel.setSearchQuery(args.submittedQuery ?: "")
            }


            HomeScreen(
                navController,
                {
                    navController.navigate(DetailedService(args.key))
                },
                {
                    navController.navigate(DetailedSeconds(args.key))
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
                servicesViewModel,
                secondsViewmodel,
                onDockedFabAddNewSecondsChanged,
                args.onlySearchBar,
                "Second Hands"
            )
        }



        slideComposable<DetailedSeconds> { backStackEntry ->


            val args = backStackEntry.toRoute<DetailedSeconds>()
            val key = args.key

            val parentBackStackEntry =  remember {
                if (key == 0) {
                    navController.getBackStackEntry<BottomBar.Home>()
                } else navController.getBackStackEntry<BottomBar.NestedServices>()
            }


            val viewModel: SecondsViewmodel =
                hiltViewModel(parentBackStackEntry, key = "seconds_${key}")
            val selectedItem by viewModel.selectedItem.collectAsState()

            selectedItem?.let {
                DetailedUsedProductListingScreen(
                    key,
                    navController,
                    onNavigateUpSlider = {
                        navController.navigate(SecondsDetailedImagesSlider(key, it))
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
                    {
                            serviceOwnerId ->
                        navController.navigate(
                            SecondsOwnerProfile(serviceOwnerId, args.key),
                            NavOptions.Builder().setLaunchSingleTop(true).build()
                        )
                    },
                    viewModel
                )
            }
        }


        slideComposable<ServiceDetailedImagesSlider> {


            val args = it.toRoute<ServiceDetailedImagesSlider>()
            val selectedImagePosition = args.selectedImagePosition
            val key = args.key

            val parentBackStackEntry = remember { if (key == 0) {
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


        slideComposable<SecondsDetailedImagesSlider> {


            val args = it.toRoute<SecondsDetailedImagesSlider>()
            val selectedImagePosition = args.selectedImagePosition
            val key = args.key

            val parentBackStackEntry = remember { if (key == 0) {
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

        slideComposable<ServiceOwnerProfile> { backStackEntry ->
            val key = backStackEntry.toRoute<ServiceOwnerProfile>().key

            val parentBackStackEntry = remember{
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
                        navController.navigate(DetailedServiceFeedUser(it))
                    }, servicesViewModel
                )
            }

        }

        slideComposable<SecondsOwnerProfile> { backStackEntry ->
            val key = backStackEntry.toRoute<SecondsOwnerProfile>().key

            val parentBackStackEntry = remember {  if (key == 0) {
                navController.getBackStackEntry<BottomBar.Home>()
            } else navController.getBackStackEntry<BottomBar.NestedSeconds>()
            }
            val viewModel: SecondsViewmodel = hiltViewModel(
                parentBackStackEntry,
                key = "seconds_${key}"
            )

            val selectedItem by viewModel.selectedItem.collectAsState()

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
                    {
                        navController.navigate(DetailedSecondsFeedUser(it))
                    }, viewModel
                )
            }

        }

        slideComposable<DetailedSecondsFeedUser> { backStackEntry ->
            val args = backStackEntry.toRoute<DetailedSecondsFeedUser>()

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
                        navController.navigate(DetailedSecondsFeedUserImagesSlider(it))
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

        slideComposable<DetailedSecondsFeedUserImagesSlider> {

            val selectedImagePosition =
                it.toRoute<DetailedSecondsFeedUserImagesSlider>().selectedImagePosition
            val viewmodel: SecondsOwnerProfileViewModel =
                hiltViewModel(remember { navController.getBackStackEntry<SecondsOwnerProfile>() })

            FeedUserSecondsImagesSliderScreen(
                navController,
                selectedImagePosition,
                viewmodel
            ) { navController.popBackStack() }
        }


        slideComposable<DetailedServiceFeedUser> { backStackEntry ->
            val args = backStackEntry.toRoute<DetailedServiceFeedUser>()

            val key = args.key

            val parentBackStackEntry = remember{ if (key == 0) {
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
                        navController.navigate(DetailedServiceFeedUserImagesSlider(it))
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

        slideComposable<DetailedServiceFeedUserImagesSlider> {

            val selectedImagePosition =
                it.toRoute<DetailedServiceFeedUserImagesSlider>().selectedImagePosition
            val serviceOwnerProfileViewModel: ServiceOwnerProfileViewModel =
                hiltViewModel(remember { navController.getBackStackEntry<ServiceOwnerProfile>() })

            FeedUserImagesSliderScreen(
                navController,
                selectedImagePosition,
                serviceOwnerProfileViewModel
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
                context.startActivity(Intent(context, UsedProductListingActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                })
            },

                onNavigateUpBookmarks = {
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