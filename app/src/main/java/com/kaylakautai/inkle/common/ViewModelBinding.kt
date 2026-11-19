package com.kaylakautai.inkle.common

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.kaylakautai.inkle.MainViewModel
import com.kaylakautai.inkle.nav.NavigateBack
import com.kaylakautai.inkle.nav.NavigateTo
import com.kaylakautai.inkle.nav.PopTo
import com.kaylakautai.inkle.ui.NavGraphs
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.spec.Direction

@Composable
fun ViewModelBinding(
    viewModel: BaseViewModel,
    mainViewModel: MainViewModel = hiltViewModel(LocalActivity.current as ComponentActivity),
    navigator: DestinationsNavigator? = null,
) {
    viewModel.setMainViewModel(mainViewModel)

    if (viewModel.isNavigationDestination) {
        if (navigator != null) {
            mainViewModel.navigationFlow.collectWithLifecycle(launchEffectKey = mainViewModel) { navEvent ->
                if (navEvent != null) {
                    when (navEvent) {
                        is NavigateTo -> {
                            if (navEvent.clearBackStack) {
                                navigator.navigate(navEvent.to) {
                                    popUpTo(NavGraphs.root) { inclusive = true }
                                    launchSingleTop = true
                                }
                            } else {
                                if (navEvent.popCurrent) navigator.popBackStack()
                                navigator.navigate(navEvent.to)
                            }
                        }

                        is NavigateBack -> {
                            navigator.navigateUp()
                        }

                        is PopTo -> {
                            navigator.popBackStack(
                                route = Direction(navEvent.route),
                                inclusive = navEvent.inclusive,
                                saveState = false
                            )
                        }
                    }
                }
            }
        }
        else {
            throw IllegalArgumentException(
                "Navigator instance must be provided for the screen owning ${viewModel.javaClass.name}"
            )
        }
    }
}