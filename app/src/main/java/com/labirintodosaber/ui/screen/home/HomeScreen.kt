package com.labirintodosaber.ui.screen.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.labirintodosaber.R
import com.labirintodosaber.ui.components.LoadingOverlay
import com.labirintodosaber.ui.components.PrimaryButton
import com.labirintodosaber.ui.navigation.AppDestination

// Stateful: conecta ao ViewModel e delega a UI para HomeContent (state hoisting)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onNavigate: (AppDestination) -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeContent(
        modifier = modifier,
        uiState = uiState,
        onAction = viewModel::onAction,
    )
}

// Stateless: recebe estado e callbacks — fácil de testar e reutilizar
@Composable
private fun HomeContent(
    uiState: HomeUiState,
    onAction: (HomeAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(modifier = modifier.fillMaxSize()) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(text = stringResource(R.string.home_title))

            PrimaryButton(
                label = stringResource(R.string.home_refresh_button),
                onClick = { onAction(HomeAction.OnRefresh) },
            )
        }

        if (uiState.isLoading) {
            LoadingOverlay()
        }
    }
}
