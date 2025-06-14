package com.lts360.compose.ui.auth.navhost

import androidx.navigation3.runtime.NavKey
import com.lts360.compose.ui.auth.AccountType
import kotlinx.serialization.Serializable


sealed class AuthScreen: NavKey{
    @Serializable
    data object Welcome : AuthScreen()

    @Serializable
    data object Verified : AuthScreen()

    @Serializable
    data object SelectAccountType : AuthScreen()

    @Serializable
    data class Register(val accountType: AccountType) : AuthScreen()

    @Serializable
    data class RegisterEmailOtpVerification(val firstName: String, val lastName: String, val email: String, val password: String, val accountType: AccountType) : AuthScreen()

    @Serializable
    data object Login : AuthScreen()

    @Serializable
    data object ForgotPassword: AuthScreen()

    @Serializable
    data class ForgotPasswordEmailOtpVerification(val email: String) : AuthScreen()


    @Serializable
    data class ResetPassword(val accessToken: String,val email: String ): AuthScreen()


}

