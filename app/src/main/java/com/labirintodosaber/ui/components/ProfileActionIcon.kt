package com.labirintodosaber.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.labirintodosaber.R
import com.labirintodosaber.ui.theme.TextPrimary

/** Foto do educador logado, provida no nível do grafo de navegação. */
val LocalProfilePhotoUrl = compositionLocalOf<String?> { null }

/** Ícone de perfil do header: mostra a foto do usuário logado, ou um ícone genérico. */
@Composable
fun ProfileActionIcon(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val photoUrl = LocalProfilePhotoUrl.current
    IconButton(onClick = onClick, modifier = modifier) {
        if (photoUrl != null) {
            AsyncImage(
                model = photoUrl,
                contentDescription = stringResource(R.string.dashboard_profile_desc),
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(30.dp).clip(CircleShape),
            )
        } else {
            Icon(
                imageVector = Icons.Outlined.AccountCircle,
                contentDescription = stringResource(R.string.dashboard_profile_desc),
                tint = TextPrimary,
                modifier = Modifier.size(28.dp),
            )
        }
    }
}
