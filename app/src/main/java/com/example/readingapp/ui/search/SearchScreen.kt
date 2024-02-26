package com.example.readingapp.ui.search

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.readingapp.R
import com.example.readingapp.common.ViewModelBinding
import com.example.readingapp.ui.components.ReadingAppBarNav
import com.example.readingapp.ui.theme.AppTheme
import com.example.readingapp.utils.keyboardAsState
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@Destination
@Composable
fun SearchScreen(
    viewModel: SearchViewModel = hiltViewModel(),
    navigator: DestinationsNavigator
) {
    ViewModelBinding(viewModel = viewModel, navigator = navigator)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val keyboardController = LocalSoftwareKeyboardController.current

    Box(Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.search_img),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(450.dp)
        )
        Scaffold(
            backgroundColor = Color.Transparent,
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        keyboardController?.hide()
                    })
                },
            topBar = {
                ReadingAppBarNav(title = stringResource(id = R.string.search_title)) { viewModel.navigateBack() }
            }
        ) { padding ->
            SearchContent(
                currentInput = uiState.searchInput,
                keyboardController = keyboardController,
                onInputChange = viewModel::onInputChange,
                onDoneClick = viewModel::onDoneClick,
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@Composable
fun SearchContent(
    currentInput: String,
    keyboardController: SoftwareKeyboardController?,
    onInputChange: (String) -> Unit,
    onDoneClick: () -> Unit,
    modifier: Modifier,
) {
    // Clear focus if keyboard was hidden by user
    val isKeyboardOpen by keyboardAsState()
    val focusManager = LocalFocusManager.current
    if (!isKeyboardOpen) focusManager.clearFocus()

    Column(
        modifier = modifier.fillMaxSize()

    ) {
        Spacer(modifier = Modifier.height(220.dp))
        TextField(
            value = currentInput,
            onValueChange = onInputChange,
            leadingIcon = { Icon(Icons.Rounded.Search, stringResource(id = R.string.cont_desc_search_icon)) },
            shape = RoundedCornerShape(AppTheme.spacing.lgSpacing),
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = Color.White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = {
                    keyboardController?.hide()
                    onDoneClick()
                }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppTheme.spacing.mdSpacing)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .background(color = Color.White, shape = RoundedCornerShape(AppTheme.spacing.lgSpacing))
                .padding(AppTheme.spacing.mdSpacing)
        ) {
            //TODO
            Text(text = "Search Results", modifier = Modifier.align(Alignment.TopCenter))
        }
    }
}