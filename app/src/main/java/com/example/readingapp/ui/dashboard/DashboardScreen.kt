package com.example.readingapp.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.readingapp.common.ViewModelBinding
import com.example.readingapp.common.observeLifecycle
import com.example.readingapp.ui.home.HomeScreen
import com.example.readingapp.ui.search.SearchScreen
import com.example.readingapp.ui.theme.AppTheme.spacing
import com.example.readingapp.ui.theme.Beige
import com.example.readingapp.ui.theme.LightPink
import com.example.readingapp.ui.user.UserScreen
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

const val BOTTOM_NAV_HEIGHT = 60

@Destination
@Composable
fun DashboardScreen(
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    viewModel: DashboardViewModel = hiltViewModel(),
    navigator: DestinationsNavigator
) {
    ViewModelBinding(viewModel = viewModel, navigator = navigator)
    ObserveDashboardLifecycle(viewModel, lifecycleOwner)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        bottomBar = { DashboardBottomNav(uiState.avatarColor, uiState.currentTab, viewModel::onTabClick) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            when (uiState.currentTab) {
                DashboardTab.Home -> HomeScreen()
                DashboardTab.Session -> HomeScreen()
                DashboardTab.Search -> SearchScreen()
                DashboardTab.Profile -> UserScreen()
            }
            Spacer(modifier = Modifier.height(BOTTOM_NAV_HEIGHT.dp))
        }
    }
}

@Composable
fun DashboardBottomNav(
    backgroundColor: Color,
    currentTab: DashboardTab,
    onTabClick: (DashboardTab) -> Unit
) {
    BottomNavigation(
        modifier = Modifier
            .height(BOTTOM_NAV_HEIGHT.dp)
            .background(Beige)
            .clip(RoundedCornerShape(topStart = spacing.mdSpacing, topEnd = spacing.mdSpacing)),
        backgroundColor = backgroundColor
    ) {
        DashboardTab.values().forEach { tab ->
            BottomNavigationItem(
                selected = tab == currentTab,
                icon = {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.label,
                        tint = LightPink,
                        modifier = Modifier.size(30.dp)
                    )
                },
                onClick = { onTabClick(tab) },
                modifier = Modifier.fillMaxHeight()
            )
        }
    }
}

@Composable
fun ObserveDashboardLifecycle(
    viewModel: DashboardViewModel,
    lifecycleOwner: LifecycleOwner
) {
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.onLoad()
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    viewModel.observeLifecycle(lifecycleOwner)
}