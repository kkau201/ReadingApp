package com.example.readingapp.ui.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.readingapp.R
import com.example.readingapp.common.ViewModelBinding
import com.example.readingapp.common.collectWithLifecycle
import com.example.readingapp.ui.theme.AppTheme
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@Destination
@RootNavGraph(start = true)
@Composable
fun SplashScreen(
    viewModel: SplashViewModel = hiltViewModel(),
    navigator: DestinationsNavigator
) {
    ViewModelBinding(viewModel = viewModel, navigator = navigator)

    viewModel.userData.collectWithLifecycle {
        if (it.loading == false && it.data != null) viewModel.navigateToHomeScreen()
        if (it.e != null) viewModel.showErrorLoadingUser()
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(color = AppTheme.colors.background)
    ) {
        SplashLottie()
    }
}

@Composable
fun SplashLottie() {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.reading))
    LottieAnimation(composition = composition, iterations = LottieConstants.IterateForever)
}