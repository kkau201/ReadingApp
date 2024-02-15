package com.example.readingapp.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector

@Composable
fun <LO : LifecycleObserver> LO.observeLifecycle(owner: LifecycleOwner) {
    DisposableEffect(owner.lifecycle) {
        owner.lifecycle.addObserver(this@observeLifecycle)
        onDispose {
            owner.lifecycle.removeObserver(this@observeLifecycle)
        }
    }
}

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