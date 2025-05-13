package com.lts360.compose.ui.main.navhosts

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavOptions
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.toRoute
import com.lts360.compose.dropUnlessResumedV2
import com.lts360.compose.ui.account.AccountAndProfileSettingsScreen
import com.lts360.compose.ui.auth.ForgotPasswordEmailOtpVerificationProtected
import com.lts360.compose.ui.auth.ForgotPasswordScreenProtected
import com.lts360.compose.ui.auth.ResetPasswordScreenProtected
import com.lts360.compose.ui.auth.SwitchAccountTypeScreen
import com.lts360.compose.ui.auth.navhost.AuthScreen
import com.lts360.compose.ui.auth.navhost.noTransitionComposable
import com.lts360.compose.ui.auth.navhost.slideComposable
import com.lts360.compose.ui.auth.navhost.slideComposableRoot
import com.lts360.compose.ui.bookmarks.BookmarksActivity
import com.lts360.compose.ui.chat.ChatScreen
import com.lts360.compose.ui.chat.openImageSliderActivity
import com.lts360.compose.ui.chat.openPlayerActivity
import com.lts360.compose.ui.chat.viewmodels.ChatListViewModel
import com.lts360.compose.ui.chat.viewmodels.ChatViewModel
import com.lts360.compose.ui.localjobs.DetailedLocalJobScreen
import com.lts360.compose.ui.localjobs.LocalJobsViewmodel
import com.lts360.compose.ui.localjobs.manage.LocalJobsImagesSliderScreen
import com.lts360.compose.ui.main.MainScreen
import com.lts360.compose.ui.main.navhosts.routes.AccountAndProfileSettingsRoutes
import com.lts360.compose.ui.main.navhosts.routes.BottomNavRoutes
import com.lts360.compose.ui.main.navhosts.routes.MainRoutes
import com.lts360.compose.ui.main.profile.ServiceOwnerProfileScreen
import com.lts360.compose.ui.main.rememberCustomMainNavController
import com.lts360.compose.ui.main.viewmodels.HomeViewModel
import com.lts360.compose.ui.main.viewmodels.SecondsViewmodel
import com.lts360.compose.ui.onboarding.ChooseIndustryScreen
import com.lts360.compose.ui.onboarding.EditProfileAboutScreen
import com.lts360.compose.ui.onboarding.GuestChooseIndustryScreen
import com.lts360.compose.ui.profile.ChangePasswordScreen
import com.lts360.compose.ui.profile.EditEmailEmailOtpVerificationScreen
import com.lts360.compose.ui.profile.EditProfileEmailScreen
import com.lts360.compose.ui.profile.EditProfileFirstNameScreen
import com.lts360.compose.ui.profile.EditProfileLastNameScreen
import com.lts360.compose.ui.profile.EditProfileSettingsScreen
import com.lts360.compose.ui.profile.viewmodels.ProfileSettingsViewModel
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
import com.lts360.compose.ui.viewmodels.HomeActivityViewModel
import com.lts360.compose.ui.viewmodels.MoreViewModel
import com.lts360.compose.ui.viewmodels.NotificationViewModel
import com.lts360.compose.ui.viewmodels.ServicesViewModel


@Composable
fun MainNavHost() {


    val context = LocalContext.current
    val homeActivityViewModel: HomeActivityViewModel = hiltViewModel()
    val homeViewModel: HomeViewModel = hiltViewModel()

    var lastEntry by rememberSaveable { mutableStateOf<String?>(null) }

    val navController = rememberCustomMainNavController(
        lastEntry,
        homeViewModel.isSelectedServiceItemNull(),
        homeViewModel.isSelectedServiceOwnerServiceItemNull(),
        homeViewModel.isSelectedUsedProductListingItemNull(),
        homeViewModel.isSelectedServiceOwnerUsedProductListingItemNull(),
        homeViewModel.isSelectedLocalJobItemNull()

    )


    val currentBackStackEntry by navController.currentBackStackEntryAsState()

    LaunchedEffect(currentBackStackEntry) {
        lastEntry = navController.currentBackStackEntry?.destination?.route
    }



    val chatListViewModel: ChatListViewModel = hiltViewModel()
    val notificationViewModel: NotificationViewModel = hiltViewModel()
    val moreViewModel: MoreViewModel = hiltViewModel()

    val profileSettingViewModel: ProfileSettingsViewModel = hiltViewModel()

    val servicesViewModel: ServicesViewModel = hiltViewModel()
    val secondsViewModel: SecondsViewmodel = hiltViewModel()

    val localJobsViewModel: LocalJobsViewmodel = hiltViewModel()

    NavHost(
        navController = navController,
        startDestination = MainRoutes.Main
    ) {

        noTransitionComposable<BottomNavRoutes.DetailedService> { backStackEntry ->

            val args = backStackEntry.toRoute<BottomNavRoutes.DetailedService>()
            val key = args.key

            val selectedItem by servicesViewModel.getServiceRepository(key).selectedItem.collectAsState()

            selectedItem?.let {
                DetailedServiceScreen(
                    navController,
                    key,
                    onNavigateUpSlider = {
                        navController.navigate(BottomNavRoutes.DetailedServiceImagesSlider(key, it))
                    }, navigateUpChat = { chatUser, chatId, recipientId ->
                        chatListViewModel.updateSelectedChatUser(chatUser)
                        navController.navigate(MainRoutes.ChatWindow(chatId, recipientId))
                    },
                    servicesViewModel,
                )
            }
        }

        slideComposable<BottomNavRoutes.DetailedServiceImagesSlider> {


            val args = it.toRoute<BottomNavRoutes.DetailedServiceImagesSlider>()
            val selectedImagePosition = args.selectedImagePosition
            val key = args.key

            val selectedItem by servicesViewModel.getServiceRepository(key).selectedItem.collectAsState()

            selectedItem?.let {
                ImagesSliderScreen(
                    key,
                    selectedImagePosition,
                    servicesViewModel
                ) { navController.popBackStack() }
            }

        }

        noTransitionComposable<BottomNavRoutes.ServiceOwnerProfile> { backStackEntry ->

            val key = backStackEntry.toRoute<BottomNavRoutes.ServiceOwnerProfile>().key


            val selectedItem by servicesViewModel.getServiceRepository(key).selectedItem.collectAsState()

            val viewModel: ServiceOwnerProfileViewModel = hiltViewModel(remember { navController.getBackStackEntry<BottomNavRoutes.ServiceOwnerProfile>() })


            selectedItem?.let {
                ServiceOwnerProfileScreen(
                    navController,
                    key,
                    onNavigateUpChat = { chatUser, chatId, recipientId ->
                        chatListViewModel.updateSelectedChatUser(chatUser)
                        navController.navigate(MainRoutes.ChatWindow(chatId, recipientId))
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

            val selectedItem by servicesViewModel.getServiceRepository(key).selectedItem.collectAsState()

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
                    { chatUser, chatId, recipientId ->
                        chatListViewModel.updateSelectedChatUser(chatUser)
                        navController.navigate(MainRoutes.ChatWindow(chatId, recipientId))

                    }, servicesViewModel
                )
            }


        }

        slideComposable<BottomNavRoutes.DetailedServiceFeedUserImagesSlider> {

            val selectedImagePosition = it.toRoute<BottomNavRoutes.DetailedServiceFeedUserImagesSlider>().selectedImagePosition
            val serviceOwnerProfileViewModel: ServiceOwnerProfileViewModel = hiltViewModel(remember { navController.getBackStackEntry<BottomNavRoutes.ServiceOwnerProfile>() })

            FeedUserImagesSliderScreen(selectedImagePosition, serviceOwnerProfileViewModel) { navController.popBackStack() }
        }

        noTransitionComposable<BottomNavRoutes.DetailedSeconds> { backStackEntry ->


            val args = backStackEntry.toRoute<BottomNavRoutes.DetailedSeconds>()
            val key = args.key


            val selectedItem by secondsViewModel.getSecondsRepository(key).selectedItem.collectAsState()

            selectedItem?.let {
                DetailedUsedProductListingScreen(
                    navController,
                    key,
                    onNavigateUpSlider = {
                        navController.navigate(BottomNavRoutes.DetailedSecondsImagesSlider(key, it))
                    }, navigateUpChat = { chatUser, chatId, recipientId ->
                        chatListViewModel.updateSelectedChatUser(chatUser)
                        navController.navigate(MainRoutes.ChatWindow(chatId, recipientId))
                    },

                    onUsedProductListingOwnerProfileClicked = { serviceOwnerId ->
                        navController.navigate(
                            BottomNavRoutes.SecondsOwnerProfile(serviceOwnerId, args.key),
                            NavOptions.Builder().setLaunchSingleTop(true).build()
                        )
                    },
                    viewModel =  secondsViewModel
                )
            }
        }

        slideComposable<BottomNavRoutes.DetailedSecondsImagesSlider> {


            val args = it.toRoute<BottomNavRoutes.DetailedSecondsImagesSlider>()
            val selectedImagePosition = args.selectedImagePosition
            val key = args.key


            val selectedItem by secondsViewModel.getSecondsRepository(key).selectedItem.collectAsState()

            selectedItem?.let {
                SecondsImagesSliderScreen(
                    key,
                    selectedImagePosition,
                    secondsViewModel
                ) { navController.popBackStack() }
            }

        }

        noTransitionComposable<BottomNavRoutes.SecondsOwnerProfile> { backStackEntry ->
            val key = backStackEntry.toRoute<BottomNavRoutes.SecondsOwnerProfile>().key


            val viewmodel: SecondsOwnerProfileViewModel =
                hiltViewModel(remember { navController.getBackStackEntry<BottomNavRoutes.SecondsOwnerProfile>() })

            val selectedItem by secondsViewModel.getSecondsRepository(key).selectedItem.collectAsState()

            selectedItem?.let {
                SecondsServiceOwnerProfileScreen(
                    navController,
                    key,
                    onNavigateUpChat = { chatUser, chatId, recipientId ->
                        chatListViewModel.updateSelectedChatUser(chatUser)
                        navController.navigate(MainRoutes.ChatWindow(chatId, recipientId))
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

            val selectedItem by secondsViewModel.getSecondsRepository(key).selectedItem.collectAsState()

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

                        chatListViewModel.updateSelectedChatUser(chatUser)
                        navController.navigate(MainRoutes.ChatWindow(chatId, recipientId))
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


        noTransitionComposable<BottomNavRoutes.DetailedLocalJob> { backStackEntry ->


            val args = backStackEntry.toRoute<BottomNavRoutes.DetailedLocalJob>()
            val key = args.key


            val selectedItem by localJobsViewModel.getLocalJobsRepository(key).selectedItem.collectAsState()

            selectedItem?.let {
                DetailedLocalJobScreen(
                    navController,
                    key,
                    onNavigateUpSlider = {
                        navController.navigate(
                            BottomNavRoutes.DetailedLocalJobsImagesSlider(
                                key,
                                it
                            )
                        )
                    }, navigateUpChat = { chatUser, chatId, recipientId ->
                        chatListViewModel.updateSelectedChatUser(chatUser)
                        navController.navigate(MainRoutes.ChatWindow(chatId, recipientId))
                    },

                    localJobsViewModel
                )
            }
        }

        slideComposable<BottomNavRoutes.DetailedLocalJobsImagesSlider> {

            val args = it.toRoute<BottomNavRoutes.DetailedLocalJobsImagesSlider>()
            val selectedImagePosition = args.selectedImagePosition
            val key = args.key


            val selectedItem by localJobsViewModel.getLocalJobsRepository(key).selectedItem.collectAsState()

            selectedItem?.let {
                LocalJobsImagesSliderScreen(
                    key,
                    selectedImagePosition,
                    localJobsViewModel
                ) { navController.popBackStack() }
            }

        }


        slideComposableRoot<MainRoutes.Main> {

            MainScreen(
                homeActivityViewModel,
                homeViewModel,
                chatListViewModel,
                notificationViewModel,
                moreViewModel,
                servicesViewModel,
                secondsViewModel,
                localJobsViewModel,
                onProfileNavigateUp = {
                    navController.navigate(AccountAndProfileSettingsRoutes.PersonalSettings)
                }, onAccountAndProfileSettingsNavigateUp = { accountType ->
                    navController.navigate(
                        AccountAndProfileSettingsRoutes.AccountAndProfileSettings(
                            accountType
                        )
                    )
                },
                onNavigateUpBookmarkedServices = {

                    context.startActivity(Intent(context, BookmarksActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                    })

                },
                onManageIndustriesAndInterestsNavigateUp = {
                    navController.navigate(MainRoutes.ChooseIndustries)
                },

                onNavigateUpGuestManageIndustriesAndInterests = {
                    navController.navigate(MainRoutes.GuestChooseIndustries)
                }, onNavigateUpChatWindow = { chatUser, chatId, recipientId ->

                    chatListViewModel.updateSelectedChatUser(chatUser)

                    navController.navigate(
                        MainRoutes.ChatWindow(
                            chatId,
                            recipientId
                        )
                    )
                }, onNavigateUpUsedProductListing = {

                    context.startActivity(
                        Intent(
                            context,
                            UsedProductListingActivity::class.java
                        ).apply {
                            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                        })

                },{
                    navController.navigate(BottomNavRoutes.DetailedService(it))
                },{ key, serviceOwnerId ->
                    navController.navigate(
                        BottomNavRoutes.ServiceOwnerProfile(serviceOwnerId, key),
                        NavOptions.Builder().setLaunchSingleTop(true).build()
                    )
                }, {
                    navController.navigate(BottomNavRoutes.DetailedSeconds(it))
                },{
                    navController.navigate(BottomNavRoutes.DetailedLocalJob(it))
                })
        }

        slideComposable<MainRoutes.ChatWindow> {

            val chatViewModel: ChatViewModel = hiltViewModel()

            ChatScreen(
                { uri, videoWidth, videoHeight, totalDuration ->
                    openPlayerActivity(context, uri, videoWidth, videoHeight, totalDuration)
                },
                chatListViewModel,
                chatViewModel,

                { uri, imageWidth, imageHeight ->
                    openImageSliderActivity(context, uri, imageWidth, imageHeight)
                },
                { navController.popBackStack() }
            )
        }

        slideComposable<MainRoutes.GuestChooseIndustries> {
            GuestChooseIndustryScreen { navController.popBackStack() }
        }


        slideComposable<MainRoutes.ChooseIndustries> {
            ChooseIndustryScreen { navController.popBackStack() }
        }


        slideComposable<AccountAndProfileSettingsRoutes.AccountAndProfileSettings> {
            AccountAndProfileSettingsScreen({
                navController.navigate(AccountAndProfileSettingsRoutes.ChangeAccountPassword)
            }, { accountType ->
                navController.navigate(AccountAndProfileSettingsRoutes.SwitchAccountType(accountType))
            }, {
                navController.popBackStack()
            })
        }

        slideComposable<AccountAndProfileSettingsRoutes.PersonalSettings> {
            EditProfileSettingsScreen({
                navController.navigate(AccountAndProfileSettingsRoutes.EditProfileFirstName)
            }, {
                navController.navigate(AccountAndProfileSettingsRoutes.EditProfileLastName)
            }, {
                navController.navigate(AccountAndProfileSettingsRoutes.EditProfileAbout("complete_about"))
            }, {
                navController.navigate(AccountAndProfileSettingsRoutes.EditProfileEmail)
            }, {
                navController.popBackStack()
            }, profileSettingViewModel)
        }

        slideComposable<AccountAndProfileSettingsRoutes.EditProfileFirstName> {
            EditProfileFirstNameScreen({
                navController.popBackStack(AccountAndProfileSettingsRoutes.PersonalSettings, false)
            }, { navController.popBackStack() })
        }

        slideComposable<AccountAndProfileSettingsRoutes.EditProfileLastName> {
            EditProfileLastNameScreen({
                navController.popBackStack(AccountAndProfileSettingsRoutes.PersonalSettings, false)
            }, { navController.popBackStack() })
        }

        slideComposable<AccountAndProfileSettingsRoutes.EditProfileAbout> {
            EditProfileAboutScreen({ navController.popBackStack() }, {
                navController.popBackStack()
            })
        }

        slideComposable<AccountAndProfileSettingsRoutes.EditProfileEmail> {
            EditProfileEmailScreen({
                navController.navigate(AccountAndProfileSettingsRoutes.EditEmailOtpVerification(it))
            }, { navController.popBackStack() })
        }

        slideComposable<AccountAndProfileSettingsRoutes.EditEmailOtpVerification> {
            EditEmailEmailOtpVerificationScreen({
                navController.popBackStack(AccountAndProfileSettingsRoutes.PersonalSettings, false)
            }, { navController.popBackStack() })
        }

        slideComposable<AccountAndProfileSettingsRoutes.ChangeAccountPassword> {
            ChangePasswordScreen(onForgotPasswordNavigateUp = {
                navController.navigate(AuthScreen.ForgotPassword)
            }, {
                navController.popBackStack<AccountAndProfileSettingsRoutes.AccountAndProfileSettings>(
                    false
                )
            }, { navController.popBackStack() })
        }

        slideComposable<AuthScreen.ForgotPassword> {
            ForgotPasswordScreenProtected({ email ->
                dropUnlessResumedV2(it) {
                    navController.navigate(
                        AuthScreen.ForgotPasswordEmailOtpVerification(email), NavOptions.Builder()
                            .setLaunchSingleTop(true)
                            .build()
                    )
                }
            }, { navController.popBackStack() })
        }

        slideComposable<AuthScreen.ForgotPasswordEmailOtpVerification> {
            ForgotPasswordEmailOtpVerificationProtected({ email, accessToken ->
                navController.popBackStack<AccountAndProfileSettingsRoutes.AccountAndProfileSettings>(
                    inclusive = false
                )
                navController.navigate(AuthScreen.ResetPassword(accessToken, email))
            }, { navController.popBackStack() })
        }

        slideComposable<AuthScreen.ResetPassword> {
            ResetPasswordScreenProtected {
                navController.popBackStack()
            }
        }

        slideComposable<AccountAndProfileSettingsRoutes.SwitchAccountType> {
            SwitchAccountTypeScreen(navController, { navController.popBackStack() }, {
                navController.popBackStack()
            })
        }

    }
}
