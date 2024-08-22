package com.example.readingapp.common

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import com.example.readingapp.MainViewModel
import com.example.readingapp.nav.NavigateBack
import com.example.readingapp.nav.NavigationEvent
import com.example.readingapp.ui.components.DialogState

abstract class BaseViewModel(
    private val dependencyContextWrapper: DependencyContextWrapper
) : ViewModel() {
    private lateinit var mainViewModel: MainViewModel

    /**
     * If this model is owned by a navigation destination
     * (a composable with `@Destination` annotation)
     */
    open val isNavigationDestination: Boolean = false

    fun setMainViewModel(main: MainViewModel) {
        mainViewModel = main
    }

    fun navigate(navigationEvent: NavigationEvent) {
        mainViewModel.navigate(navigationEvent)
    }

    fun navigateBack() {
        mainViewModel.navigate(NavigateBack)
    }

    fun getLoadingState() = mainViewModel.loadingFlow

    fun updateLoadingState(state: LoadingState) {
        if (state == mainViewModel.loadingFlow.value) return
        if (state is LoadingState.Loading && mainViewModel.loadingFlow.value is LoadingState.Loading) return

        mainViewModel.updateLoadingState(state)
    }

    fun showDialog(state: DialogState) {
        mainViewModel.showDialog(state)
    }

    fun dismissDialog() {
        mainViewModel.dismissDialog()
    }

    fun showErrorDialog(e: Throwable?) {
        val error = if (e is ErrorType) e else ErrorType.UnknownNetworkException(e)

        showDialog(
            DialogState(
                title = getString(error.title),
                message = getString(error.body),
                primaryButtonText = getString(error.primaryBtn),
                onPrimaryClick = {
                    dismissDialog()
                    navigateBack()
                }
            )
        )
    }

    fun showToast(text: String) {
        mainViewModel.showToast(text)
    }

    fun getString(@StringRes resId: Int): String {
        return dependencyContextWrapper.context.getString(resId)
    }

    fun getString(@StringRes resId: Int, vararg args: String): String {
        return dependencyContextWrapper.context.getString(resId, *args)
    }
}