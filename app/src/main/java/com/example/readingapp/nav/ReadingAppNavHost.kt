package com.example.readingapp.nav

import androidx.compose.runtime.Composable
import com.example.readingapp.ui.home.NavGraphs
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.spec.Direction

@Composable
fun ReadingAppNavHost() {
    DestinationsNavHost(navGraph = NavGraphs.root)
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