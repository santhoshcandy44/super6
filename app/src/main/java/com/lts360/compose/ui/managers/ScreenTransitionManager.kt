package com.lts360.compose.ui.managers

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.navigation.NavBackStackEntry


// Advanced reusable transition manager
class ScreenTransitionManager(
    private val slideDirection: AnimatedContentTransitionScope.SlideDirection = AnimatedContentTransitionScope.SlideDirection.Left,
    private val reverseSlideDirection: AnimatedContentTransitionScope.SlideDirection = AnimatedContentTransitionScope.SlideDirection.Right,
    private val durationMillis: Int = 500
) {
    // Create the enter transition
    val enterTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?) = {
        slideIntoContainer(
            slideDirection,
            animationSpec = tween(durationMillis)
        )
    }

    // Create the exit transition
    val exitTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?) = {
        slideOutOfContainer(
            slideDirection,
            animationSpec = tween(durationMillis)
        )
    }

    // Create the pop enter transition (reverse direction)
    val popEnterTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?) = {
        slideIntoContainer(
            reverseSlideDirection,
            animationSpec = tween(durationMillis)
        )
    }

    // Create the pop exit transition (reverse direction)
    val popExitTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?) = {
        slideOutOfContainer(
            reverseSlideDirection,
            animationSpec = tween(durationMillis)
        )
    }
}
