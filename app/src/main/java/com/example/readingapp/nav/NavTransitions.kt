package com.example.readingapp.nav

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import com.ramcosta.composedestinations.animations.defaults.RootNavGraphDefaultAnimations

val slideTransitions = RootNavGraphDefaultAnimations(
    enterTransition = {
        slideIntoContainer(
            AnimatedContentTransitionScope.SlideDirection.Start,
            tween(700)
        )
    },
    exitTransition = {
        slideOutOfContainer(
            AnimatedContentTransitionScope.SlideDirection.Start,
            tween(700)
        )
    },
    popEnterTransition = {
        slideIntoContainer(
            AnimatedContentTransitionScope.SlideDirection.End,
            tween(700)
        )
    },
    popExitTransition = {
        slideOutOfContainer(
            AnimatedContentTransitionScope.SlideDirection.End,
            tween(700)
        )
    }
)