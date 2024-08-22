package com.example.readingapp.ui.dashboard

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Search
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.readingapp.ui.theme.Purple

data class DashboardUiState(
    val currentTab: DashboardTab = DashboardTab.Home,
    val avatarColor: Color = Purple
)

sealed class DashboardTab(
    val label: String,
    val icon: ImageVector
) {
    data object Home : DashboardTab("Home", Icons.Rounded.Home)
    data object Session : DashboardTab("Session", Icons.Rounded.Add)
    data object Search : DashboardTab("Search", Icons.Rounded.Search)
    data object Profile : DashboardTab("Profile", Icons.Rounded.Person)

    companion object {
        fun values(): List<DashboardTab> = listOf(Home, Session, Search, Profile)
    }
}