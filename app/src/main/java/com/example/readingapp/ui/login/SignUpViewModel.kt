package com.example.readingapp.ui.login

import com.example.readingapp.common.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(): BaseViewModel() {
    override val isNavigationDestination: Boolean = true
}