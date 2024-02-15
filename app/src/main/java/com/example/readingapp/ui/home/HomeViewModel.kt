package com.example.readingapp.ui.home

import com.example.readingapp.common.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(): BaseViewModel() {
    override val isNavigationDestination: Boolean = true
}