package com.lts360.compose.ui.auth.navhost

import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.lts360.R
import com.lts360.compose.ui.auth.AccountType
import com.lts360.compose.ui.auth.ForgotPasswordEmailOtpVerification
import com.lts360.compose.ui.auth.ForgotPasswordScreen
import com.lts360.compose.ui.auth.LoginScreen
import com.lts360.compose.ui.auth.RegisterEmailOtpVerificationScreen
import com.lts360.compose.ui.auth.RegisterScreen
import com.lts360.compose.ui.auth.ResetPasswordScreen
import com.lts360.compose.ui.auth.SelectAccountTypeScreen
import com.lts360.compose.ui.auth.VerifiedScreen
import com.lts360.compose.ui.auth.WelcomeScreen
import com.lts360.compose.ui.onboarding.OnBoardingActivity
import kotlin.reflect.typeOf


@Composable
fun AuthNavHost(defaultEntry: AuthScreen = AuthScreen.Welcome) {

    val navController = rememberNavController()
    val context = LocalContext.current

    // Define the AnimatedNavHost
    NavHost(
        navController = navController,
        startDestination = defaultEntry
    ) {
        // Entry Screen
        slideComposableRoot<AuthScreen.Welcome> {
            WelcomeScreen(onLogInNavigate = {
                navController.navigate(AuthScreen.Login)
            }, onSelectAccountNavigate = {

                navController.navigate(AuthScreen.SelectAccountType)
            }) {
                context.startActivity(
                    Intent(context, OnBoardingActivity::class.java).apply {
                        flags=Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                        putExtra("type","guest")
                    },
                    ActivityOptions.makeCustomAnimation(
                        context,
                        R.anim.slide_in_right,
                        R.anim.slide_out_left
                    ).toBundle()
                )
            }
        }


        // Choose Account Type Screen
        slideComposable<AuthScreen.SelectAccountType> {
            SelectAccountTypeScreen {
                navController.navigate(AuthScreen.Register(it))
            }
        }

        // Register Screen
        slideComposable<AuthScreen.Register>(
            typeMap = mapOf(typeOf<AccountType>() to NavType.EnumType(AccountType::class.java))
        ) {
            RegisterScreen(onNavigateUpRegisterEmailOtpVerification = { firstName, lastName, email, password, accountType ->
                navController.navigate(AuthScreen.RegisterEmailOtpVerification(
                       firstName, lastName, email, password, accountType
                    ))
            }) {
                context.startActivity(
                    Intent(context, OnBoardingActivity::class.java).apply {
                        flags=Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    },
                    ActivityOptions.makeCustomAnimation(
                        context,
                        R.anim.slide_in_right,
                        R.anim.slide_out_left
                    ).toBundle()
                )
                (context as Activity).finishAffinity()

            }
        }

        // Reset Password Screen
        slideComposable<AuthScreen.Verified> {
            VerifiedScreen {
                context.startActivity(
                    Intent(context, OnBoardingActivity::class.java).apply {
                        flags=Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    },
                    ActivityOptions.makeCustomAnimation(
                        context,
                        R.anim.slide_in_right,
                        R.anim.slide_out_left
                    ).toBundle()
                )
                (context as Activity).finishAffinity()
            }
        }

        // Login Screen
        slideComposable<AuthScreen.Login> {
            LoginScreen(onNavigateUpForgotPassword = {
                navController.navigate(AuthScreen.ForgotPassword)
            }) {
                navController.navigate(AuthScreen.SelectAccountType)

            }
        }

        //  Email OTP Verification Screen
        slideComposable<AuthScreen.RegisterEmailOtpVerification> {

            RegisterEmailOtpVerificationScreen({
                navController.popBackStack()
            })
        }

        // Forgot Password Screen
        slideComposable<AuthScreen.ForgotPassword> {
            ForgotPasswordScreen { email ->
                navController.navigate(
                    AuthScreen.ForgotPasswordEmailOtpVerification(
                        email
                    )
                )
            }
        }

        // Forgot Password Validate Email Screen
        slideComposable<AuthScreen.ForgotPasswordEmailOtpVerification> {
            ForgotPasswordEmailOtpVerification(
                { email,accessToken ->
                    navController.navigate(AuthScreen.ResetPassword(accessToken,email)){
                        popUpTo(AuthScreen.Login) { inclusive = false } // Pops up to 'Login' screen and removes it from the backstack
                        launchSingleTop = true // Ensures only one instance of the destination is in the backstack
                    }
                }, {
                    navController.popBackStack()
                }
            )
        }


        // Reset Password Screen
        slideComposable<AuthScreen.ResetPassword> {
            ResetPasswordScreen {
                navController.navigate(AuthScreen.Login) {
                    // Clear the back stack
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                }
            }
        }
    }
}

