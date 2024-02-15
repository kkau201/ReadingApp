package com.example.readingapp.common

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.readingapp.MainViewModel
import com.example.readingapp.mainActivity
import com.example.readingapp.nav.NavigateBack
import com.example.readingapp.nav.NavigateTo
import com.example.readingapp.nav.PopTo
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@Composable
fun ViewModelBinding(
    viewModel: BaseViewModel,
    mainViewModel: MainViewModel = hiltViewModel(mainActivity()),
    navigator: DestinationsNavigator? = null,
) {
    viewModel.setMainViewModel(mainViewModel)

    if (viewModel.isNavigationDestination) {
        if (navigator != null) {
            mainViewModel.navigationFlow.collectWithLifecycle(launchEffectKey = mainViewModel) { navEvent ->
                if (navEvent != null) {
                    when (navEvent) {
                        is NavigateTo -> {
                            if (navEvent.popCurrent) navigator.popBackStack()
                            navigator.navigate(navEvent.to)
                        }

                        is NavigateBack -> {
                            navigator.navigateUp()
                        }

                        is PopTo -> {
                            navigator.popBackStack(
                                route = navEvent.route,
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