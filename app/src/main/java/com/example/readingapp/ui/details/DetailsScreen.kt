package com.example.readingapp.ui.details

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.readingapp.common.ViewModelBinding
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@Destination
@Composable
fun DetailsScreen(
    bookId: String,
    viewModel: DetailsViewModel = hiltViewModel(),
    navigator: DestinationsNavigator
) {
    ViewModelBinding(viewModel = viewModel, navigator = navigator)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    DetailsScreenContent(uiState)
    Text(text = bookId)
}

@Composable
fun DetailsScreenContent(uiState: DetailsUiState) {}