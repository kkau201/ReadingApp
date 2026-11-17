package com.example.readingapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.readingapp.common.LoadingState
import com.example.readingapp.nav.ReadingAppNavHost
import com.example.readingapp.ui.components.FullScreenLoadingLottie
import com.example.readingapp.ui.components.ReadingAppDialog
import com.example.readingapp.ui.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    MainContent()
                }
            }
        }
    }
}

@Composable
fun MainContent(
    mainViewModel: MainViewModel = hiltViewModel(LocalActivity.current as ComponentActivity)
) {
    ReadingAppNavHost()

    val loadingState by mainViewModel.loadingFlow.collectAsStateWithLifecycle()
    if (loadingState is LoadingState.Loading &&
        (loadingState as LoadingState.Loading).loadingType == LoadingState.LoadingType.FULL_SCREEN
    ) {
        FullScreenLoadingLottie()
    }

    val dialogState by mainViewModel.dialogFlow.collectAsStateWithLifecycle()
    dialogState?.let { ReadingAppDialog(state = it) }

    val context = LocalContext.current
    val toastFlow by mainViewModel.toastFlow.collectAsStateWithLifecycle()
    toastFlow?.let {
        Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        mainViewModel.resetToast()
    }
}