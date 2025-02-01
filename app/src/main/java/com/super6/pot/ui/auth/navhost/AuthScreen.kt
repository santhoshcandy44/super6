package com.super6.pot.ui.auth.navhost

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import com.super6.pot.ui.auth.AccountType
import com.super6.pot.ui.managers.ScreenTransitionManager
import kotlinx.serialization.Serializable
import kotlin.reflect.KType

@Serializable
sealed class AuthScreen  {
    @Serializable
    data object Welcome : AuthScreen()

    @Serializable
    data object Verified : AuthScreen()

    @Serializable
    data object SelectAccountType : AuthScreen()

    @Serializable
    data class Register(val accountType:AccountType) : AuthScreen()

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


val screenTransitionManager = ScreenTransitionManager()



inline fun <reified T : Any> NavGraphBuilder.noTransitionComposable(
    typeMap: Map<KType, @JvmSuppressWildcards NavType<*>> = emptyMap(),
    noinline content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit)
{
    composable<T>(
        typeMap=typeMap,
        exitTransition = {ExitTransition.None},
        enterTransition = {EnterTransition.None},
        popExitTransition =  {ExitTransition.None},
        popEnterTransition = {EnterTransition.None},
    ){
        content(it)
    }

}


inline fun <reified T : Any> NavGraphBuilder.slideComposable(
    typeMap: Map<KType, @JvmSuppressWildcards NavType<*>> = emptyMap(),
    noinline content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit)
{
    composable<T>(
        typeMap=typeMap,
        exitTransition = screenTransitionManager.exitTransition,
        enterTransition = screenTransitionManager.enterTransition,
        popExitTransition = screenTransitionManager.popExitTransition,
        popEnterTransition = screenTransitionManager.popEnterTransition,
    ){
        content(it)
    }

}


inline fun <reified T : Any> NavGraphBuilder.slideComposableRoot(
    typeMap: Map<KType, @JvmSuppressWildcards NavType<*>> = emptyMap(),
    noinline content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit)
{
    composable<T>(
        typeMap=typeMap,
        exitTransition = screenTransitionManager.exitTransition,
        popEnterTransition = screenTransitionManager.popEnterTransition
    ){
        content(it)
    }
}

