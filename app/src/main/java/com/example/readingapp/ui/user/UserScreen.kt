package com.example.readingapp.ui.user

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.readingapp.common.ViewModelBinding
import com.example.readingapp.common.observeLifecycle
import com.example.readingapp.model.DEFAULT_READING_GOAL
import java.util.Calendar

@Composable
fun UserScreen(
    viewModel: UserViewModel = hiltViewModel(),
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
) {
    ViewModelBinding(viewModel)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ObserveUserLifecycleEvents(lifecycleOwner, viewModel)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
    ) { padding ->
        UserContent(
            modifier = Modifier.padding(padding),
            userDisplayName = uiState.user?.displayName.orEmpty(),
            userBio = uiState.user?.quote.orEmpty(),
            userAvatar = uiState.user?.avatar,
            books = uiState.books,
            booksFailed = uiState.booksFailed,
            booksLoaded = uiState.booksLoaded,
            onRetryBooks = viewModel::onRetryBooksClick,
            readingGoal = uiState.user?.readingGoal ?: DEFAULT_READING_GOAL,
            reward = uiState.user?.rewardFor(Calendar.getInstance().get(Calendar.YEAR)),
            onUpdateClick = viewModel::navigateToUpdateUser,
            onLogoutClick = viewModel::onLogoutClick,
            onDeleteAccountClick = viewModel::onDeleteAccountClick,
            unverifiedEmail = uiState.unverifiedEmail,
            verificationResent = uiState.verificationResent,
            onResendVerification = viewModel::onResendVerificationClick,
            onDismissVerification = viewModel::onDismissVerificationClick,
            emailJustVerified = uiState.emailJustVerified,
            onEmailVerifiedShown = viewModel::onEmailVerifiedShown
        )
    }
}

@Composable
fun ObserveUserLifecycleEvents(
    lifecycleOwner: LifecycleOwner,
    viewModel: UserViewModel
) {
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.loadUser()
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    viewModel.observeLifecycle(lifecycleOwner)
}
