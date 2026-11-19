package com.kaylakautai.inkle.ui.dashboard

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kaylakautai.inkle.common.ViewModelBinding
import com.kaylakautai.inkle.common.observeLifecycle
import com.kaylakautai.inkle.ui.home.HomeScreen
import com.kaylakautai.inkle.ui.search.SearchScreen
import com.kaylakautai.inkle.ui.session.SessionScreen
import com.kaylakautai.inkle.ui.theme.AppTheme.spacing
import com.kaylakautai.inkle.ui.theme.InkBase
import com.kaylakautai.inkle.ui.theme.NavBar
import com.kaylakautai.inkle.ui.theme.PaperBase
import com.kaylakautai.inkle.ui.user.UserScreen
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
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = { DashboardBottomNav(uiState.currentTab, viewModel::onTabClick) }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .consumeWindowInsets(padding)
        ) {
            when (uiState.currentTab) {
                DashboardTab.Home -> HomeScreen()
                DashboardTab.Session -> SessionScreen()
                DashboardTab.Search -> SearchScreen()
                DashboardTab.Profile -> UserScreen()
            }
        }
    }
}

private val NavBarHeight = 80.dp
private val NavBarHorizontalPadding = 12.dp
private val IndicatorHeight = 48.dp

/** Gap between the pill and the edges of its tab slot. */
private val IndicatorInset = 10.dp
private val NavIconSize = 26.dp

/**
 * Dark bottom bar. A large light pill sits behind the current tab and slides to whichever tab
 * is tapped; the current tab's icon is filled and dark, the others outlined and dimmed.
 *
 * Built by hand rather than with Material's NavigationBar, whose indicator is a fixed small
 * size and fades between tabs instead of sliding.
 */
@Composable
fun DashboardBottomNav(
    currentTab: DashboardTab,
    onTabClick: (DashboardTab) -> Unit
) {
    val tabs = DashboardTab.values()
    val selectedIndex = tabs.indexOf(currentTab)

    Surface(
        color = NavBar,
        shape = RoundedCornerShape(topStart = spacing.mdSpacing, topEnd = spacing.mdSpacing)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                // Keep the icons above the gesture bar; the dark background extends behind it.
                .navigationBarsPadding()
                .height(NavBarHeight)
                .padding(horizontal = NavBarHorizontalPadding)
        ) {
            val slotWidth = maxWidth / tabs.size
            val indicatorWidth = slotWidth - IndicatorInset * 2
            val indicatorOffset by animateDpAsState(
                targetValue = slotWidth * selectedIndex + IndicatorInset,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow
                ),
                label = "navIndicatorOffset"
            )

            // The sliding pill. Offset is read in the layout phase, so animating it doesn't
            // recompose the bar on every frame.
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset { IntOffset(indicatorOffset.roundToPx(), 0) }
                    .size(width = indicatorWidth, height = IndicatorHeight)
                    .clip(CircleShape)
                    .background(PaperBase)
            )

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .selectableGroup()
            ) {
                tabs.forEachIndexed { index, tab ->
                    DashboardNavItem(
                        tab = tab,
                        selected = index == selectedIndex,
                        onClick = { onTabClick(tab) },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardNavItem(
    tab: DashboardTab,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val iconColor by animateColorAsState(
        // The bar is dark in both themes, so these colours are fixed.
        targetValue = if (selected) InkBase else PaperBase.copy(alpha = 0.55f),
        label = "navIconColor"
    )
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.selectable(
            selected = selected,
            onClick = onClick,
            role = Role.Tab,
            interactionSource = null,
            indication = ripple(bounded = false, radius = IndicatorHeight / 2, color = PaperBase)
        )
    ) {
        Icon(
            imageVector = if (selected) tab.selectedIcon else tab.icon,
            // No visible label, so the icon names the tab for screen readers.
            contentDescription = stringResource(tab.label),
            tint = iconColor,
            modifier = Modifier.size(NavIconSize)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF1EFEB)
@Composable
private fun DashboardBottomNavPreview() {
    DashboardBottomNav(currentTab = DashboardTab.Session, onTabClick = {})
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