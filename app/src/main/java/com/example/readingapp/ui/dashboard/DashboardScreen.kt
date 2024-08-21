package com.example.readingapp.ui.dashboard

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.readingapp.common.ViewModelBinding
import com.example.readingapp.ui.home.HomeScreen
import com.example.readingapp.ui.search.SearchScreen
import com.example.readingapp.ui.theme.Pink
import com.example.readingapp.ui.theme.Purple
import com.example.readingapp.ui.user.UserScreen
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

const val BOTTOM_NAV_HEIGHT = 60

@Destination
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    navigator: DestinationsNavigator
) {
    ViewModelBinding(viewModel = viewModel, navigator = navigator)
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    Scaffold(
        bottomBar = { DashboardBottomNav(uiState.value.currentTab, viewModel::onTabClick) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            when (uiState.value.currentTab) {
                DashboardTab.Home -> HomeScreen()
                DashboardTab.Search -> SearchScreen()
                DashboardTab.Profile -> UserScreen()
            }
            Spacer(modifier = Modifier.height(BOTTOM_NAV_HEIGHT.dp))
        }
    }
}

@Composable
fun DashboardBottomNav(
    currentTab: DashboardTab,
    onTabClick: (DashboardTab) -> Unit
) {
    BottomNavigation(
        modifier = Modifier.height(BOTTOM_NAV_HEIGHT.dp),
        backgroundColor = Purple
    ) {
        DashboardTab.values().forEach { tab ->
            BottomNavigationItem(
                selected = tab == currentTab,
                icon = {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.label,
                        tint = Pink,
                        modifier = Modifier.size(30.dp)
                    )
                },
                onClick = { onTabClick(tab) },
                modifier = Modifier.fillMaxHeight()
            )
        }
    }
}