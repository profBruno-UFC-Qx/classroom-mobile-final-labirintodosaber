package com.labirintodosaber.ui.components

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

// Nome: substantivo (PrimaryButton), não verbo (DrawButton) nem adjetivo solto
@Composable
fun PrimaryButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,  // modifier sempre presente com default
    enabled: Boolean = true,
) {
    // Trailing lambda: onClick fica fora dos parênteses
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
    ) {
        Text(text = label)
    }
}
