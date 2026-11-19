package com.kaylakautai.inkle.ui.dashboard

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.SpaceDashboard
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SpaceDashboard
import androidx.compose.ui.graphics.vector.ImageVector
import com.kaylakautai.inkle.R

data class DashboardUiState(
    val currentTab: DashboardTab = DashboardTab.Home
)

/**
 * A bottom-nav tab. [selectedIcon] is filled and [icon] is outlined, so the current tab stands
 * out by shape as well as colour.
 */
sealed class DashboardTab(
    @StringRes val label: Int,
    val icon: ImageVector,
    val selectedIcon: ImageVector
) {
    data object Home :
        DashboardTab(R.string.nav_home, Icons.Outlined.SpaceDashboard, Icons.Rounded.SpaceDashboard)

    data object Session :
        DashboardTab(R.string.nav_session, Icons.Outlined.Book, Icons.Rounded.Book)

    // Search has no outlined variant; size and colour still distinguish it.
    data object Search :
        DashboardTab(R.string.nav_search, Icons.Rounded.Search, Icons.Rounded.Search)

    data object Profile :
        DashboardTab(R.string.nav_profile, Icons.Outlined.Person, Icons.Rounded.Person)

    companion object {
        fun values(): List<DashboardTab> = listOf(Home, Session, Search, Profile)
    }
}