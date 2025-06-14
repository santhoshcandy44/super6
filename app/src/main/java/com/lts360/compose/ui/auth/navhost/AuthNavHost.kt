package com.lts360.compose.ui.auth.navhost

import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSavedStateNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.ui.rememberSceneSetupNavEntryDecorator
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
    val backStacks = rememberNavBackStack(defaultEntry)
    val context = LocalContext.current

    NavDisplay(
        backStack = backStacks,
        entryDecorators = listOf(
            rememberSceneSetupNavEntryDecorator(),
            rememberSavedStateNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        entryProvider = entryProvider {
            entry<AuthScreen.Welcome> {
                WelcomeScreen(
                    onLogInNavigate = { backStacks.add(AuthScreen.Login) },
                    onSelectAccountNavigate = { backStacks.add(AuthScreen.SelectAccountType) },
                    onGuestLogin = {
                        context.startActivity(
                            Intent(context, OnBoardingActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                                putExtra("type", "guest")
                            },
                            ActivityOptions.makeCustomAnimation(
                                context,
                                R.anim.slide_in_right,
                                R.anim.slide_out_left
                            ).toBundle()
                        )
                    }
                )
            }

            entry<AuthScreen.SelectAccountType> {
                SelectAccountTypeScreen { accountType ->
                    backStacks.add(AuthScreen.Register(accountType))
                }
            }

            entry<AuthScreen.Register> { entry ->
                RegisterScreen(
                    onNavigateUpRegisterEmailOtpVerification = { firstName, lastName, email, password, accountType ->
                        backStacks.add(
                            AuthScreen.RegisterEmailOtpVerification(
                                firstName,
                                lastName,
                                email,
                                password,
                                accountType
                            )
                        )
                    },
                    onNavigateUpOnBoarding = {
                        context.startActivity(
                            Intent(context, OnBoardingActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                            },
                            ActivityOptions.makeCustomAnimation(
                                context,
                                R.anim.slide_in_right,
                                R.anim.slide_out_left
                            ).toBundle()
                        )
                        (context as Activity).finishAffinity()
                    }
                )
            }

            entry<AuthScreen.Verified> {
                VerifiedScreen {
                    context.startActivity(
                        Intent(context, OnBoardingActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
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

            entry<AuthScreen.Login> {
                LoginScreen(
                    onNavigateUpForgotPassword = { backStacks.add(AuthScreen.ForgotPassword) },
                    onNavigateUpCreateAccount = { backStacks.add(AuthScreen.SelectAccountType) }
                )
            }

            entry<AuthScreen.RegisterEmailOtpVerification> {
                RegisterEmailOtpVerificationScreen(
                    onPopBackStack = { backStacks.removeLastOrNull() }
                )
            }

            entry<AuthScreen.ForgotPassword> {
                ForgotPasswordScreen { email ->
                    backStacks.add(AuthScreen.ForgotPasswordEmailOtpVerification(email))
                }
            }

            entry<AuthScreen.ForgotPasswordEmailOtpVerification> { entry ->
                ForgotPasswordEmailOtpVerification(
                    onNavigateUp = { email, accessToken ->
                        backStacks.removeAll { it != AuthScreen.Welcome }
                        backStacks.add(AuthScreen.ResetPassword(accessToken, email))
                    },
                    onPopBackStack = { backStacks.removeLastOrNull() }
                )
            }

            entry<AuthScreen.ResetPassword> {
                ResetPasswordScreen {
                    backStacks.removeAll { true }
                    backStacks.add(AuthScreen.Login)
                }
            }
        }
    )
}