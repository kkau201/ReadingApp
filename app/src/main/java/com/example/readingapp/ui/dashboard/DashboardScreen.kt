package com.example.readingapp.ui.dashboard

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
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
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@Destination<RootGraph>
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
        containerColor = Beige,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = { DashboardBottomNav(uiState.avatarColor, uiState.currentTab, viewModel::onTabClick) }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .consumeWindowInsets(padding)
        ) {
            when (uiState.currentTab) {
                DashboardTab.Home -> HomeScreen()
                DashboardTab.Session -> HomeScreen()
                DashboardTab.Search -> SearchScreen()
                DashboardTab.Profile -> UserScreen()
            }
        }
    }
}

@Composable
fun DashboardBottomNav(
    backgroundColor: Color,
    currentTab: DashboardTab,
    onTabClick: (DashboardTab) -> Unit
) {
    NavigationBar(
        modifier = Modifier.clip(
            RoundedCornerShape(topStart = spacing.mdSpacing, topEnd = spacing.mdSpacing)
        ),
        containerColor = backgroundColor,
    ) {
        DashboardTab.values().forEach { tab ->
            NavigationBarItem(
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
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = LightPink,
                    unselectedIconColor = LightPink,
                    indicatorColor = Color.Transparent,
                ),
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