package com.example.readingapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.readingapp.common.LoadingState
import com.example.readingapp.nav.ReadingAppNavHost
import com.example.readingapp.ui.components.FullScreenLoadingLottie
import com.example.readingapp.ui.components.LoadingLottie
import com.example.readingapp.ui.components.ReadingAppDialog
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                MainContent()
            }
        }
    }
}

@Composable
fun MainContent(mainViewModel: MainViewModel = hiltViewModel(mainActivity())) {
    ReadingAppNavHost()

    val loadingState by mainViewModel.loadingFlow.collectAsStateWithLifecycle()
    if (loadingState is LoadingState.Loading) {
        when ((loadingState as LoadingState.Loading).loadingType) {
            LoadingState.LoadingType.FULL_SCREEN -> FullScreenLoadingLottie()
            LoadingState.LoadingType.MINI -> LoadingLottie()
        }
    }

    val dialogState by mainViewModel.dialogFlow.collectAsStateWithLifecycle()
    dialogState?.let { ReadingAppDialog(state = it) }

    val context = mainActivity()
    val toastFlow by mainViewModel.toastFlow.collectAsStateWithLifecycle()
    toastFlow?.let {
        Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        mainViewModel.resetToast()
    }

}