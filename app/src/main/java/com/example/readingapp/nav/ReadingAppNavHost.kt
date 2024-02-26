package com.example.readingapp.nav

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import com.example.readingapp.ui.NavGraphs
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.animations.defaults.NestedNavGraphDefaultAnimations
import com.ramcosta.composedestinations.animations.rememberAnimatedNavHostEngine
import com.ramcosta.composedestinations.spec.Direction

@OptIn(ExperimentalMaterialNavigationApi::class, ExperimentalAnimationApi::class)
@Composable
fun ReadingAppNavHost() {
    val navHostEngine = rememberAnimatedNavHostEngine(
        navHostContentAlignment = Alignment.TopCenter,
        rootDefaultAnimations = slideTransitions,
        defaultAnimationsForNestedNavGraph = mapOf(
            NavGraphs.root to NestedNavGraphDefaultAnimations(
                enterTransition = {
                    slideIntoContainer(
                        AnimatedContentTransitionScope.SlideDirection.Start,
                        tween(700)
                    )
                },
                exitTransition = {
                    slideOutOfContainer(
                        AnimatedContentTransitionScope.SlideDirection.End,
                        tween(700)
                    )
                }
            )
        )
    )

    DestinationsNavHost(navGraph = NavGraphs.root, engine = navHostEngine)
}

sealed interface NavigationEvent

data object NavigateBack : NavigationEvent

data class NavigateTo(
    val to: Direction,
    val popCurrent: Boolean = false
) : NavigationEvent

data class PopTo(
    val route: String,
    val inclusive: Boolean = false
) : NavigationEvent