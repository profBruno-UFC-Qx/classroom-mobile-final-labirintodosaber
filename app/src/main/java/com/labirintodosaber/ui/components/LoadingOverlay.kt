package com.labirintodosaber.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.labirintodosaber.R

// Nome: substantivo (LoadingOverlay), modifier obrigatório para reutilização
@Composable
fun LoadingOverlay(modifier: Modifier = Modifier) {
    val description = stringResource(R.string.loading_description)

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            // contentDescription obrigatório para acessibilidade com leitores de tela
            modifier = Modifier.semantics { contentDescription = description },
        )
    }
}
