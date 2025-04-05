package com.lts360.compose.ui.main.navhosts

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavOptions
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.lts360.compose.dropUnlessResumedV2
import com.lts360.compose.ui.account.AccountAndProfileSettingsScreen
import com.lts360.compose.ui.auth.ForgotPasswordEmailOtpVerificationProtected
import com.lts360.compose.ui.auth.ForgotPasswordScreenProtected
import com.lts360.compose.ui.auth.ResetPasswordScreenProtected
import com.lts360.compose.ui.auth.SwitchAccountTypeScreen
import com.lts360.compose.ui.auth.navhost.AuthScreen
import com.lts360.compose.ui.auth.navhost.slideComposable
import com.lts360.compose.ui.auth.navhost.slideComposableRoot
import com.lts360.compose.ui.bookmarks.BookmarksActivity
import com.lts360.compose.ui.chat.ChatScreen
import com.lts360.compose.ui.chat.openImageSliderActivity
import com.lts360.compose.ui.chat.openPlayerActivity
import com.lts360.compose.ui.chat.viewmodels.ChatListViewModel
import com.lts360.compose.ui.chat.viewmodels.ChatViewModel
import com.lts360.compose.ui.main.MainScreen
import com.lts360.compose.ui.main.navhosts.routes.AccountAndProfileSettingsRoutes
import com.lts360.compose.ui.main.navhosts.routes.MainRoutes
import com.lts360.compose.ui.main.profile.ProfileScreen
import com.lts360.compose.ui.main.viewmodels.HomeViewModel
import com.lts360.compose.ui.onboarding.ChooseIndustryScreen
import com.lts360.compose.ui.onboarding.EditProfileAboutScreen
import com.lts360.compose.ui.onboarding.GuestChooseIndustryScreen
import com.lts360.compose.ui.onboarding.navhost.OnBoardingScreen
import com.lts360.compose.ui.profile.ChangePasswordScreen
import com.lts360.compose.ui.profile.EditEmailEmailOtpVerificationScreen
import com.lts360.compose.ui.profile.EditProfileEmailScreen
import com.lts360.compose.ui.profile.EditProfileFirstNameScreen
import com.lts360.compose.ui.profile.EditProfileLastNameScreen
import com.lts360.compose.ui.profile.EditProfileSettingsScreen
import com.lts360.compose.ui.profile.viewmodels.ProfileSettingsViewModel
import com.lts360.compose.ui.usedproducts.manage.UsedProductListingActivity
import com.lts360.compose.ui.viewmodels.HomeActivityViewModel
import com.lts360.compose.ui.viewmodels.MoreViewModel
import com.lts360.compose.ui.viewmodels.NotificationViewModel


@Composable
fun MainNavHost() {


    val context = LocalContext.current

    val navController = rememberNavController()

    val homeActivityViewModel:HomeActivityViewModel = hiltViewModel()

    val homeViewModel: HomeViewModel = hiltViewModel()
    val chatListViewModel: ChatListViewModel = hiltViewModel()
    val notificationViewModel: NotificationViewModel = hiltViewModel()
    val moreViewModel: MoreViewModel = hiltViewModel()

    val profileSettingViewModel: ProfileSettingsViewModel = hiltViewModel()


    NavHost(
        navController = navController,
        startDestination = MainRoutes.Main
    ) {
        slideComposableRoot<MainRoutes.Main> {

            MainScreen(
                homeViewModel,
                chatListViewModel,
                notificationViewModel,
                moreViewModel,
                homeActivityViewModel,
                onProfileNavigateUp = {
                    navController.navigate(AccountAndProfileSettingsRoutes.Profile)
                }, onAccountAndProfileSettingsNavigateUp = { accountType ->
                    navController.navigate(AccountAndProfileSettingsRoutes.AccountAndProfileSettings(accountType))
                }, onManageIndustriesAndInterestsNavigateUp = { userId, type ->
                    navController.navigate(OnBoardingScreen.ChooseIndustries(userId, type))
                },
                onNavigateUpBookmarkedServices = {

                    context.startActivity(Intent(context, BookmarksActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                    })

                },
                onNavigateUpGuestManageIndustriesAndInterests = {
                    navController.navigate(OnBoardingScreen.GuestChooseIndustries)
                }, onNavigateUpChatWindow = { chatUser, chatId, recipientId, feedUserProfile ->

                    chatListViewModel.updateSelectedChatUser(chatUser)

                    navController.navigate(
                        MainRoutes.ChatWindow(
                            chatId,
                            recipientId
                        )
                    )
                }, onNavigateUpUsedProductListing = {

                    context.startActivity(Intent(context, UsedProductListingActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                    })

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
                } ,
                { navController.popBackStack()}
            )
        }

        slideComposable<OnBoardingScreen.GuestChooseIndustries> {
            GuestChooseIndustryScreen({},{ navController.popBackStack() })
        }


        slideComposable<OnBoardingScreen.ChooseIndustries> {
            ChooseIndustryScreen({ navController.popBackStack() })
        }

        slideComposable<AccountAndProfileSettingsRoutes.Profile> {

            ProfileScreen({ navController.popBackStack() })
        }

        slideComposable<AccountAndProfileSettingsRoutes.AccountAndProfileSettings> {
            AccountAndProfileSettingsScreen({
                navController.navigate(AccountAndProfileSettingsRoutes.PersonalSettings)
            }, {
                navController.navigate(AccountAndProfileSettingsRoutes.ChangeProfilePassword)
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
            EditProfileAboutScreen(
                { _, _ -> },
                {},
                {
                    navController.popBackStack(AccountAndProfileSettingsRoutes.PersonalSettings, false)
                }, { navController.popBackStack() })
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

        slideComposable<AccountAndProfileSettingsRoutes.ChangeProfilePassword> {
            ChangePasswordScreen(onForgotPasswordNavigateUp = {
                navController.navigate(AuthScreen.ForgotPassword)
            }, {
                navController.popBackStack<AccountAndProfileSettingsRoutes.AccountAndProfileSettings>(false)
            }, { navController.popBackStack() })
        }

        slideComposable<AuthScreen.ForgotPassword> {
            ForgotPasswordScreenProtected({ email ->
                dropUnlessResumedV2(it){
                    navController.navigate(
                        AuthScreen.ForgotPasswordEmailOtpVerification(email), NavOptions.Builder()
                        .setLaunchSingleTop(true)
                        .build())
                }
            }, { navController.popBackStack() })
        }

        slideComposable<AuthScreen.ForgotPasswordEmailOtpVerification> {
            ForgotPasswordEmailOtpVerificationProtected({ email, accessToken ->
                navController.popBackStack<AccountAndProfileSettingsRoutes.AccountAndProfileSettings>(inclusive = false)
                navController.navigate(AuthScreen.ResetPassword(accessToken, email))
            }, { navController.popBackStack() })
        }

        slideComposable<AuthScreen.ResetPassword> {
            ResetPasswordScreenProtected {
                navController.popBackStack()
            }
        }

        slideComposable<AccountAndProfileSettingsRoutes.SwitchAccountType> {
            SwitchAccountTypeScreen(navController,{navController.popBackStack()}, {
                navController.popBackStack()
            })
        }

    }
}

