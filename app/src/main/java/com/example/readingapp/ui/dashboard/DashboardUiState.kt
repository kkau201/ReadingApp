package com.example.readingapp.ui.dashboard

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Search
import androidx.compose.ui.graphics.vector.ImageVector

data class DashboardUiState(
    val currentTab: DashboardTab = DashboardTab.Home
)

sealed class DashboardTab(
    val label: String,
    val icon: ImageVector
) {
    data object Home : DashboardTab("Home", Icons.Rounded.Home)
    data object Search : DashboardTab("Search", Icons.Rounded.Search)
    data object Profile : DashboardTab("Profile", Icons.Rounded.Person)

    companion object {
        fun values(): List<DashboardTab> = listOf(Home, Search, Profile)
    }
}