package com.lts360.compose.ui.main.navhosts

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSavedStateNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.ui.rememberSceneSetupNavEntryDecorator
import com.lts360.compose.dropUnlessResumedV2
import com.lts360.compose.ui.account.AccountAndProfileSettingsScreen
import com.lts360.compose.ui.auth.ForgotPasswordEmailOtpVerificationProtected
import com.lts360.compose.ui.auth.ForgotPasswordScreenProtected
import com.lts360.compose.ui.auth.ResetPasswordScreenProtected
import com.lts360.compose.ui.auth.SwitchAccountTypeScreen
import com.lts360.compose.ui.auth.navhost.AuthScreen
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
import org.koin.androidx.compose.koinViewModel


fun NavBackStack.removeUpTo(
    navKey: Any,
    inclusive: Boolean = true
): Boolean {
    if(navKey !is NavKey) false
    val index = this.indexOfFirst { it == navKey }
    if (index == -1) return false

    val removeIndex = if (inclusive) index else index + 1
    if (removeIndex >= this.size) return false

    this.removeAll { this.indexOf(it) >= removeIndex }
    return true
}

@Composable
fun MainNavHost() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val homeActivityViewModel: HomeActivityViewModel = koinViewModel()
    val homeViewModel: HomeViewModel = koinViewModel()

    val backStack = rememberNavBackStack<MainRoutes>(MainRoutes.Main)

    val chatListViewModel: ChatListViewModel = koinViewModel()
    val notificationViewModel: NotificationViewModel = koinViewModel()
    val moreViewModel: MoreViewModel = koinViewModel()

    val profileSettingViewModel: ProfileSettingsViewModel = koinViewModel()

    val servicesViewModel: ServicesViewModel = koinViewModel()
    val secondsViewModel: SecondsViewmodel = koinViewModel()

    val localJobsViewModel: LocalJobsViewmodel = koinViewModel()

    NavDisplay(
        backStack = backStack,
        entryDecorators = listOf(
            rememberSceneSetupNavEntryDecorator(),
            rememberSavedStateNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        entryProvider = entryProvider {

            entry<BottomNavRoutes.DetailedService> { navEntry ->

                val key = navEntry.key
                val selectedItem by servicesViewModel.getServiceRepository(key).selectedItem.collectAsState()

                selectedItem?.let {
                    DetailedServiceScreen(
                        key,
                        onNavigateUpSlider = {
                            backStack.add(BottomNavRoutes.DetailedServiceImagesSlider(key, it))
                        },
                        navigateUpChat = { chatUser, chatId, recipientId ->
                            chatListViewModel.updateSelectedChatUser(chatUser)
                            backStack.add(MainRoutes.ChatWindow(chatId, recipientId))
                        },
                        {
                            backStack.removeLastOrNull()
                        },
                        servicesViewModel,
                    )
                }
            }

            entry<BottomNavRoutes.DetailedServiceImagesSlider> { navEntry ->

                val selectedImagePosition = navEntry.selectedImagePosition
                val key = navEntry.key

                val selectedItem by servicesViewModel.getServiceRepository(key).selectedItem.collectAsState()

                selectedItem?.let {
                    ImagesSliderScreen(
                        key,
                        selectedImagePosition,
                        servicesViewModel
                    ) { backStack.removeLastOrNull() }
                }

            }

            entry<BottomNavRoutes.ServiceOwnerProfile> { navEntry ->

                val key = navEntry.key

                val selectedItem by servicesViewModel.getServiceRepository(key).selectedItem.collectAsState()

                val viewModel: ServiceOwnerProfileViewModel = koinViewModel()

                selectedItem?.let {
                    ServiceOwnerProfileScreen(
                        key,
                        onNavigateUpChat = { chatUser, chatId, recipientId ->
                            chatListViewModel.updateSelectedChatUser(chatUser)
                            backStack.add(MainRoutes.ChatWindow(chatId, recipientId))
                        },
                        {
                            backStack.add(BottomNavRoutes.DetailedServiceFeedUser(it))
                        },
                        { backStack.removeLastOrNull() },
                        servicesViewModel,
                        viewModel
                    )
                }

            }

            entry<BottomNavRoutes.DetailedServiceFeedUser> { navEntry ->

                val key = navEntry.key

                val selectedItem by servicesViewModel.getServiceRepository(key).selectedItem.collectAsState()

                selectedItem?.let {
                    FeedUserDetailedServiceInfoScreen(
                        key,
                        {
                            backStack.add(
                                BottomNavRoutes.DetailedServiceFeedUserImagesSlider(
                                    it
                                )
                            )
                        },
                        { chatUser, chatId, recipientId ->
                            chatListViewModel.updateSelectedChatUser(chatUser)
                            backStack.add(MainRoutes.ChatWindow(chatId, recipientId))

                        },
                        {
                            backStack.removeLastOrNull()
                        },
                        servicesViewModel
                    )
                }


            }

            entry<BottomNavRoutes.DetailedServiceFeedUserImagesSlider> { navEntry ->

                val selectedImagePosition = navEntry.selectedImagePosition
                val serviceOwnerProfileViewModel: ServiceOwnerProfileViewModel = koinViewModel()

                FeedUserImagesSliderScreen(
                    selectedImagePosition,
                    serviceOwnerProfileViewModel
                ) { backStack.removeLastOrNull() }
            }

            entry<BottomNavRoutes.DetailedSeconds> { navEntry ->

                val key = navEntry.key

                val selectedItem by secondsViewModel.getSecondsRepository(key).selectedItem.collectAsState()

                selectedItem?.let {
                    DetailedUsedProductListingScreen(
                        key,
                        onNavigateUpSlider = {
                            backStack.add(
                                BottomNavRoutes.DetailedSecondsImagesSlider(
                                    key,
                                    it
                                )
                            )
                        }, navigateUpChat = { chatUser, chatId, recipientId ->
                            chatListViewModel.updateSelectedChatUser(chatUser)
                            backStack.add(MainRoutes.ChatWindow(chatId, recipientId))
                        },

                        onUsedProductListingOwnerProfileClicked = { serviceOwnerId ->
                            backStack.add(
                                BottomNavRoutes.SecondsOwnerProfile(serviceOwnerId, key)
                            )
                        },
                        { backStack.removeLastOrNull() },
                        viewModel = secondsViewModel
                    )
                }
            }

            entry<BottomNavRoutes.DetailedSecondsImagesSlider> { navEntry ->

                val selectedImagePosition = navEntry.selectedImagePosition
                val key = navEntry.key

                val selectedItem by secondsViewModel.getSecondsRepository(key).selectedItem.collectAsState()

                selectedItem?.let {
                    SecondsImagesSliderScreen(
                        key,
                        selectedImagePosition,
                        secondsViewModel
                    ) { backStack.removeLastOrNull() }
                }

            }

            entry<BottomNavRoutes.SecondsOwnerProfile> { navEntry ->
                val key = navEntry.key

                val viewmodel: SecondsOwnerProfileViewModel = koinViewModel()

                val selectedItem by secondsViewModel.getSecondsRepository(key).selectedItem.collectAsState()

                selectedItem?.let {
                    SecondsServiceOwnerProfileScreen(
                        key,
                        onNavigateUpChat = { chatUser, chatId, recipientId ->
                            chatListViewModel.updateSelectedChatUser(chatUser)
                            backStack.add(MainRoutes.ChatWindow(chatId, recipientId))
                        },
                        { key, usedProductListing ->
                            viewmodel.setSelectedItem(usedProductListing)
                            homeViewModel.setSelectedSecondsOwnerUsedProductListingIItem(
                                usedProductListing
                            )
                            backStack.add(BottomNavRoutes.DetailedSecondsFeedUser(key))
                        },
                        { backStack.removeLastOrNull() },
                        secondsViewModel,
                        viewmodel
                    )
                }

            }

            entry<BottomNavRoutes.DetailedSecondsFeedUser> { navEntry ->

                val key = navEntry.key

                val selectedItem by secondsViewModel.getSecondsRepository(key).selectedItem.collectAsState()

                selectedItem?.let {
                    FeedUserDetailedSecondsInfoScreen(
                        {
                            backStack.add(
                                BottomNavRoutes.DetailedSecondsFeedUserImagesSlider(
                                    it
                                )
                            )
                        },
                        { chatUser, chatId, recipientId ->

                            chatListViewModel.updateSelectedChatUser(chatUser)
                            backStack.add(MainRoutes.ChatWindow(chatId, recipientId))
                        }, {
                            backStack.removeLastOrNull()
                        }
                    )
                }


            }

            entry<BottomNavRoutes.DetailedSecondsFeedUserImagesSlider> { navEntry ->
                val selectedImagePosition = navEntry.selectedImagePosition
                val viewmodel: SecondsOwnerProfileViewModel = koinViewModel()

                FeedUserSecondsImagesSliderScreen(
                    selectedImagePosition,
                    viewmodel
                ) { backStack.removeLastOrNull() }
            }

            entry<BottomNavRoutes.DetailedLocalJob> { navEntry ->

                val key = navEntry.key

                val selectedItem by localJobsViewModel.getLocalJobsRepository(key).selectedItem.collectAsState()

                selectedItem?.let {
                    DetailedLocalJobScreen(
                        key,
                        onNavigateUpSlider = {
                            backStack.add(
                                BottomNavRoutes.DetailedLocalJobsImagesSlider(
                                    key,
                                    it
                                )
                            )
                        }, navigateUpChat = { chatUser, chatId, recipientId ->
                            chatListViewModel.updateSelectedChatUser(chatUser)
                            backStack.add(MainRoutes.ChatWindow(chatId, recipientId))
                        },
                        {
                            backStack.removeLastOrNull()
                        },
                        localJobsViewModel
                    )
                }
            }

            entry<BottomNavRoutes.DetailedLocalJobsImagesSlider> { navEntry ->

                val selectedImagePosition = navEntry.selectedImagePosition
                val key = navEntry.key

                val selectedItem by localJobsViewModel.getLocalJobsRepository(key).selectedItem.collectAsState()

                selectedItem?.let {
                    LocalJobsImagesSliderScreen(
                        key,
                        selectedImagePosition,
                        localJobsViewModel
                    ) { backStack.removeLastOrNull() }
                }

            }

            entry<MainRoutes.Main> {

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
                        backStack.add(AccountAndProfileSettingsRoutes.PersonalSettings)
                    }, onAccountAndProfileSettingsNavigateUp = { accountType ->
                        backStack.add(
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
                        backStack.add(MainRoutes.ChooseIndustries)
                    },
                    onNavigateUpGuestManageIndustriesAndInterests = {
                        backStack.add(MainRoutes.GuestChooseIndustries)
                    },
                    onNavigateUpChatWindow = { chatUser, chatId, recipientId ->

                        chatListViewModel.updateSelectedChatUser(chatUser)

                        backStack.add(
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

                    }, {
                        backStack.add(BottomNavRoutes.DetailedService(it))
                    }, { key, serviceOwnerId ->
                        backStack.add(
                            BottomNavRoutes.ServiceOwnerProfile(serviceOwnerId, key),
                        )
                    }, {
                        backStack.add(BottomNavRoutes.DetailedSeconds(it))
                    }, {
                        backStack.add(BottomNavRoutes.DetailedLocalJob(it))
                    })
            }

            entry<MainRoutes.ChatWindow> {
                val chatViewModel: ChatViewModel = koinViewModel()
                ChatScreen(
                    { uri, videoWidth, videoHeight, totalDuration ->
                        openPlayerActivity(context, uri, videoWidth, videoHeight, totalDuration)
                    },
                    chatListViewModel,
                    chatViewModel,

                    { uri, imageWidth, imageHeight ->
                        openImageSliderActivity(context, uri, imageWidth, imageHeight)
                    },
                    { backStack.removeLastOrNull() }
                )
            }

            entry<MainRoutes.GuestChooseIndustries> {
                GuestChooseIndustryScreen { backStack.removeLastOrNull() }
            }

            entry<MainRoutes.ChooseIndustries> {
                ChooseIndustryScreen { backStack.removeLastOrNull() }
            }

            entry<AccountAndProfileSettingsRoutes.AccountAndProfileSettings> {
                AccountAndProfileSettingsScreen({
                    backStack.add(AccountAndProfileSettingsRoutes.ChangeAccountPassword)
                }, { accountType ->
                    backStack.add(
                        AccountAndProfileSettingsRoutes.SwitchAccountType(
                            accountType
                        )
                    )
                }, {
                    backStack.removeLastOrNull()
                })
            }

            entry<AccountAndProfileSettingsRoutes.PersonalSettings> {
                EditProfileSettingsScreen({
                    backStack.add(AccountAndProfileSettingsRoutes.EditProfileFirstName)
                }, {
                    backStack.add(AccountAndProfileSettingsRoutes.EditProfileLastName)
                }, {
                    backStack.add(AccountAndProfileSettingsRoutes.EditProfileAbout("complete_about"))
                }, {
                    backStack.add(AccountAndProfileSettingsRoutes.EditProfileEmail)
                }, {
                    backStack.removeLastOrNull()
                }, profileSettingViewModel)
            }

            entry<AccountAndProfileSettingsRoutes.EditProfileFirstName> {
                EditProfileFirstNameScreen({
                    backStack.removeUpTo(
                        AccountAndProfileSettingsRoutes.PersonalSettings,
                        false
                    )
                }, { backStack.removeLastOrNull() })
            }

            entry<AccountAndProfileSettingsRoutes.EditProfileLastName> {
                EditProfileLastNameScreen({
                    backStack.removeUpTo(
                        AccountAndProfileSettingsRoutes.PersonalSettings,
                        false
                    )
                }, { backStack.removeLastOrNull() })
            }

            entry<AccountAndProfileSettingsRoutes.EditProfileAbout> {
                EditProfileAboutScreen({ backStack.removeLastOrNull() }, {
                    backStack.removeLastOrNull()
                })
            }

            entry<AccountAndProfileSettingsRoutes.EditProfileEmail> {
                EditProfileEmailScreen({
                    backStack.add(
                        AccountAndProfileSettingsRoutes.EditEmailOtpVerification(
                            it
                        )
                    )
                }, { backStack.removeLastOrNull() })
            }

            entry<AccountAndProfileSettingsRoutes.EditEmailOtpVerification> {
                EditEmailEmailOtpVerificationScreen({
                    backStack.removeUpTo(
                        AccountAndProfileSettingsRoutes.PersonalSettings,
                        false
                    )
                }, { backStack.removeLastOrNull() })
            }

            entry<AccountAndProfileSettingsRoutes.ChangeAccountPassword> {
                ChangePasswordScreen(onForgotPasswordNavigateUp = {
                    backStack.add(AuthScreen.ForgotPassword)
                }, {
                    backStack.removeUpTo(
                        AccountAndProfileSettingsRoutes.AccountAndProfileSettings,
                        false
                    )
                }, { backStack.removeLastOrNull() })
            }

            entry<AuthScreen.ForgotPassword> {
                ForgotPasswordScreenProtected({ email ->
                    dropUnlessResumedV2(lifecycleOwner) {
                        backStack.add(AuthScreen.ForgotPasswordEmailOtpVerification(email))
                    }
                }, { backStack.removeLastOrNull() })
            }

            entry<AuthScreen.ForgotPasswordEmailOtpVerification> {
                ForgotPasswordEmailOtpVerificationProtected({ email, accessToken ->
                    backStack.add(AuthScreen.ResetPassword(accessToken, email))
                }, { backStack.removeLastOrNull() })
            }

            entry<AuthScreen.ResetPassword> {
                ResetPasswordScreenProtected {
                    backStack.removeLastOrNull()
                }
            }

            entry<AccountAndProfileSettingsRoutes.SwitchAccountType> {
                SwitchAccountTypeScreen({ backStack.removeLastOrNull() }, {
                    backStack.removeLastOrNull()
                })
            }

        })
}

