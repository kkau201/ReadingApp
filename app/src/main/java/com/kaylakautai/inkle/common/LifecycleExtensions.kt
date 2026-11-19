package com.kaylakautai.inkle.common

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector

@SuppressLint("ComposableNaming")
@Composable
fun <LO : LifecycleObserver> LO.observeLifecycle(owner: LifecycleOwner) {
    DisposableEffect(owner.lifecycle) {
        owner.lifecycle.addObserver(this@observeLifecycle)
        onDispose {
            owner.lifecycle.removeObserver(this@observeLifecycle)
        }
    }
}

@SuppressLint("ComposableNaming")
@Composable
fun <T> Flow<T>.collectWithLifecycle(
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    launchEffectKey: Any? = Unit,
    collector: FlowCollector<T>
) {
    LaunchedEffect(launchEffectKey) {
        flowWithLifecycle(lifecycleOwner.lifecycle, minActiveState).collect(collector)
    }
}