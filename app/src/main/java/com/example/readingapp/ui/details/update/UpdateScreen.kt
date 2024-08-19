package com.example.readingapp.ui.details.update

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.readingapp.R
import com.example.readingapp.common.ViewModelBinding
import com.example.readingapp.ui.components.ReadingAppBarNav
import com.example.readingapp.ui.theme.AppTheme
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@Destination
@Composable
fun UpdateScreen(
    bookId: String,
    viewModel: UpdateViewModel = hiltViewModel(),
    navigator: DestinationsNavigator
) {
    ViewModelBinding(viewModel = viewModel, navigator = navigator)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    androidx.compose.material.Scaffold(
        backgroundColor = AppTheme.colors.background,
        modifier = Modifier.fillMaxSize(),
        topBar = {
            ReadingAppBarNav(
                title = stringResource(R.string.update_book_title),
                navIconTint = Color.Black,
                onNavIconClick = viewModel::navigateBack
            )
        }
    ) { padding ->
        uiState.book?.let {
            UpdateScreenContent(
                modifier = Modifier.padding(padding)
            )
        }
    }


}

@Composable
fun UpdateScreenContent(modifier: Modifier = Modifier) {

}