package com.super6.pot.compose.ui.main.navhosts

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavOptions
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.super6.pot.compose.dropUnlessResumedV2
import com.super6.pot.compose.ui.account.AccountAndProfileSettingsScreen
import com.super6.pot.compose.ui.auth.ForgotPasswordEmailOtpVerificationProtected
import com.super6.pot.compose.ui.auth.ForgotPasswordScreenProtected
import com.super6.pot.compose.ui.auth.ResetPasswordScreenProtected
import com.super6.pot.compose.ui.auth.SwitchAccountTypeScreen
import com.super6.pot.compose.ui.auth.navhost.AuthScreen
import com.super6.pot.compose.ui.auth.navhost.slideComposable
import com.super6.pot.compose.ui.auth.navhost.slideComposableRoot
import com.super6.pot.compose.ui.chat.ChatScreen
import com.super6.pot.compose.ui.chat.openImageSliderActivity
import com.super6.pot.compose.ui.chat.openPlayerActivity
import com.super6.pot.compose.ui.chat.viewmodels.ChatListViewModel
import com.super6.pot.compose.ui.chat.viewmodels.ChatViewModel
import com.super6.pot.compose.ui.main.MainScreen
import com.super6.pot.compose.ui.main.navhosts.routes.AccountAndProfileSettings
import com.super6.pot.compose.ui.main.navhosts.routes.ChangeProfilePassword
import com.super6.pot.compose.ui.main.navhosts.routes.ChatWindow
import com.super6.pot.compose.ui.main.navhosts.routes.EditEmailOtpVerification
import com.super6.pot.compose.ui.main.navhosts.routes.EditProfileAbout
import com.super6.pot.compose.ui.main.navhosts.routes.EditProfileEmail
import com.super6.pot.compose.ui.main.navhosts.routes.EditProfileFirstName
import com.super6.pot.compose.ui.main.navhosts.routes.EditProfileLastName
import com.super6.pot.compose.ui.main.navhosts.routes.MainScreen
import com.super6.pot.compose.ui.main.navhosts.routes.PersonalSettings
import com.super6.pot.compose.ui.main.navhosts.routes.Profile
import com.super6.pot.compose.ui.main.navhosts.routes.SwitchAccountType
import com.super6.pot.compose.ui.main.profile.ProfileScreen
import com.super6.pot.compose.ui.main.viewmodels.HomeViewModel
import com.super6.pot.compose.ui.onboarding.ChooseIndustryScreen
import com.super6.pot.compose.ui.onboarding.EditProfileAboutScreen
import com.super6.pot.compose.ui.onboarding.GuestChooseIndustryScreen
import com.super6.pot.compose.ui.onboarding.navhost.OnBoardingScreen
import com.super6.pot.compose.ui.profile.ChangePasswordScreen
import com.super6.pot.compose.ui.profile.EditEmailEmailOtpVerificationScreen
import com.super6.pot.compose.ui.profile.EditProfileEmailScreen
import com.super6.pot.compose.ui.profile.EditProfileFirstNameScreen
import com.super6.pot.compose.ui.profile.EditProfileLastNameScreen
import com.super6.pot.compose.ui.profile.EditProfileSettingsScreen
import com.super6.pot.compose.ui.profile.viewmodels.ProfileSettingsViewModel
import com.super6.pot.compose.ui.services.manage.BookmarkedServicesActivity
import com.super6.pot.compose.ui.viewmodels.MoreViewModel
import com.super6.pot.compose.ui.viewmodels.NotificationViewModel


@Composable
fun MainNavHost() {


    val context = LocalContext.current

    val navController = rememberNavController()

    val homeViewModel: HomeViewModel = hiltViewModel()
    val chatListViewModel: ChatListViewModel = hiltViewModel()
    val notificationViewModel: NotificationViewModel = hiltViewModel()
    val moreViewModel: MoreViewModel = hiltViewModel()

    val profileSettingViewModel: ProfileSettingsViewModel = hiltViewModel()


    NavHost(
        navController = navController,
        startDestination = MainScreen.Main
    ) {
        slideComposableRoot<MainScreen.Main> {

            MainScreen(
                homeViewModel,
                chatListViewModel,
                notificationViewModel,
                moreViewModel,
                onProfileNavigateUp = {
                    navController.navigate(Profile)
                }, onAccountAndProfileSettingsNavigateUp = { accountType ->
                    navController.navigate(AccountAndProfileSettings(accountType))
                }, onManageIndustriesAndInterestsNavigateUp = { userId, type ->
                    navController.navigate(OnBoardingScreen.ChooseIndustries(userId, type))
                },
                onNavigateUpBookmarkedServices = {

                    context.startActivity(Intent(context, BookmarkedServicesActivity::class.java).apply {
                        flags =
                            Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    })

                },
                onNavigateUpGuestManageIndustriesAndInterests = {
                    navController.navigate(OnBoardingScreen.GuestChooseIndustries)
                }, onNavigateUpChatWindow = { chatUser, chatId, recipientId, feedUserProfile ->

                    chatListViewModel.updateSelectedChatUser(chatUser)

                    navController.navigate(
                        ChatWindow(
                            chatId,
                            recipientId
                        )
                    )
                })
        }

        slideComposable<ChatWindow> {

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

        slideComposable<Profile> {

            ProfileScreen({ navController.popBackStack() })
        }

        slideComposable<AccountAndProfileSettings> {
            AccountAndProfileSettingsScreen({
                navController.navigate(PersonalSettings)
            }, {
                navController.navigate(ChangeProfilePassword)
            }, { accountType ->
                navController.navigate(SwitchAccountType(accountType))
            }, {
                navController.popBackStack()
            })
        }

        slideComposable<PersonalSettings> {
            EditProfileSettingsScreen({
                navController.navigate(EditProfileFirstName)
            }, {
                navController.navigate(EditProfileLastName)
            }, {
                navController.navigate(EditProfileAbout("complete_about"))
            }, {
                navController.navigate(EditProfileEmail)
            }, {
                navController.popBackStack()
            }, profileSettingViewModel)
        }

        slideComposable<EditProfileFirstName> {
            EditProfileFirstNameScreen({
                navController.popBackStack(PersonalSettings, false)
            }, { navController.popBackStack() })
        }

        slideComposable<EditProfileLastName> {
            EditProfileLastNameScreen({
                navController.popBackStack(PersonalSettings, false)
            }, { navController.popBackStack() })
        }

        slideComposable<EditProfileAbout> {
            EditProfileAboutScreen(
                { _, _ -> },
                {},
                {
                    navController.popBackStack(PersonalSettings, false)
                }, { navController.popBackStack() })
        }

        slideComposable<EditProfileEmail> {
            EditProfileEmailScreen({
                navController.navigate(EditEmailOtpVerification(it))
            }, { navController.popBackStack() })
        }

        slideComposable<EditEmailOtpVerification> {
            EditEmailEmailOtpVerificationScreen({
                navController.popBackStack(PersonalSettings, false)
            }, { navController.popBackStack() })
        }

        slideComposable<ChangeProfilePassword> {
            ChangePasswordScreen(onForgotPasswordNavigateUp = {
                navController.navigate(AuthScreen.ForgotPassword)
            }, {
                navController.popBackStack<AccountAndProfileSettings>(false)
            }, { navController.popBackStack() })
        }

        slideComposable<AuthScreen.ForgotPassword> {
            ForgotPasswordScreenProtected({ email ->
                dropUnlessResumedV2(it){
                    navController.navigate(AuthScreen.ForgotPasswordEmailOtpVerification(email), NavOptions.Builder()
                        .setLaunchSingleTop(true)
                        .build())
                }
            }, { navController.popBackStack() })
        }

        slideComposable<AuthScreen.ForgotPasswordEmailOtpVerification> {
            ForgotPasswordEmailOtpVerificationProtected({ email, accessToken ->
                navController.popBackStack<AccountAndProfileSettings>(inclusive = false)
                navController.navigate(AuthScreen.ResetPassword(accessToken, email))
            }, { navController.popBackStack() })
        }

        slideComposable<AuthScreen.ResetPassword> {
            ResetPasswordScreenProtected {
                navController.popBackStack()
            }
        }

        slideComposable<SwitchAccountType> {
            SwitchAccountTypeScreen(navController,{navController.popBackStack()}, {
                navController.popBackStack()
            })
        }

        slideComposable<OnBoardingScreen.ChooseIndustries> {
            ChooseIndustryScreen({}, { navController.popBackStack() })
        }
    }
}

