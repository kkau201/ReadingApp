package com.example.readingapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.readingapp.R
import com.example.readingapp.ui.theme.AppTheme

@Composable
fun LoadingLottie() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentWidth(Alignment.CenterHorizontally)
            .wrapContentHeight(Alignment.CenterVertically)
    ) {
        val composition by rememberLottieComposition(spec = LottieCompositionSpec.RawRes(R.raw.loading_book))
        LottieAnimation(
            composition = composition,
            iterations = LottieConstants.IterateForever
        )
    }
}

@Composable
fun FullScreenLoadingLottie() {
    Box(
        modifier = Modifier
            .background(color = AppTheme.colors.background)
            .fillMaxSize()
            .wrapContentWidth(Alignment.CenterHorizontally)
            .wrapContentHeight(Alignment.CenterVertically)
    ) {
        val composition by rememberLottieComposition(spec = LottieCompositionSpec.RawRes(R.raw.reading))
        LottieAnimation(
            composition = composition,
            iterations = LottieConstants.IterateForever
        )
    }
}


@Composable
fun NoResultsLottie(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .wrapContentWidth(Alignment.CenterHorizontally)
            .wrapContentHeight(Alignment.CenterVertically)
    ) {
        val composition by rememberLottieComposition(spec = LottieCompositionSpec.RawRes(R.raw.no_result))
        LottieAnimation(
            composition = composition,
            iterations = LottieConstants.IterateForever
        )
    }
}