package com.kaylakautai.inkle.nav

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.navigation.NavBackStackEntry
import com.kaylakautai.inkle.ui.NavGraphs
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.animations.NavHostAnimatedDestinationStyle
import com.ramcosta.composedestinations.rememberNavHostEngine
import com.ramcosta.composedestinations.spec.Direction

@Composable
fun InkleNavHost() {
    val navHostEngine = rememberNavHostEngine(
        navHostContentAlignment = Alignment.TopCenter,
    )

    DestinationsNavHost(
        navGraph = NavGraphs.root,
        engine = navHostEngine,
        defaultTransitions = object : NavHostAnimatedDestinationStyle() {
            override val enterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Start,
                    tween(700),
                )
            }
            override val exitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Start,
                    tween(700),
                )
            }
            override val popEnterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.End,
                    tween(700),
                )
            }
            override val popExitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.End,
                    tween(700),
                )
            }
        },
    )
}

sealed interface NavigationEvent

data object NavigateBack : NavigationEvent

/**
 * @param popCurrent remove the current screen before navigating.
 * @param clearBackStack remove every screen before navigating, so [to] becomes the only entry
 *   and Back exits the app (e.g. after logout).
 */
data class NavigateTo(
    val to: Direction,
    val popCurrent: Boolean = false,
    val clearBackStack: Boolean = false
) : NavigationEvent

data class PopTo(
    val route: String,
    val inclusive: Boolean = false
) : NavigationEvent