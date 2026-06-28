package com.labirintodosaber.ui.screen.createactivity

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.AudioFile
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.labirintodosaber.R
import com.labirintodosaber.data.remote.FileUpload
import com.labirintodosaber.ui.components.CategorySelector
import com.labirintodosaber.ui.theme.TealDark
import com.labirintodosaber.ui.theme.TealLight
import com.labirintodosaber.ui.theme.TealPrimary

@Composable
fun CreateActivityScreen(
    onBackClick: () -> Unit,
    onSaveSuccess: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CreateActivityViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) onSaveSuccess()
    }

    CreateActivityContent(
        uiState = uiState,
        onAction = viewModel::onAction,
        onCancelClick = onBackClick,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateActivityContent(
    uiState: CreateActivityUiState,
    onAction: (CreateActivityAction) -> Unit,
    onCancelClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { onAction(CreateActivityAction.OnImagePicked(context.toFileUpload(it, "imagem"))) }
    }
    val audioPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { onAction(CreateActivityAction.OnAudioPicked(context.toFileUpload(it, "audio"))) }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.create_activity_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onCancelClick) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
            )
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 4.dp) {
                NavigationBarItem(selected = false, onClick = {}, icon = { Icon(Icons.Outlined.Menu, contentDescription = null) }, label = { Text(stringResource(R.string.dashboard_tab_home), style = MaterialTheme.typography.labelSmall) }, colors = navItemColors())
                NavigationBarItem(selected = true, onClick = {}, icon = { Icon(Icons.AutoMirrored.Outlined.MenuBook, contentDescription = null) }, label = { Text(stringResource(R.string.dashboard_tab_activities), style = MaterialTheme.typography.labelSmall) }, colors = navItemColors())
                NavigationBarItem(selected = false, onClick = {}, icon = { Icon(Icons.Outlined.Folder, contentDescription = null) }, label = { Text(stringResource(R.string.dashboard_tab_students), style = MaterialTheme.typography.labelSmall) }, colors = navItemColors())
                NavigationBarItem(selected = false, onClick = {}, icon = { Icon(Icons.Outlined.AccountCircle, contentDescription = null) }, label = { Text(stringResource(R.string.dashboard_tab_reports), style = MaterialTheme.typography.labelSmall) }, colors = navItemColors())
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Nome
            item {
                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = { onAction(CreateActivityAction.OnNameChange(it)) },
                    label = { Text(stringResource(R.string.create_activity_name_label)) },
                    placeholder = { Text("Ex: Alfabetização Divertida", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = fieldColors(),
                )
            }

            // Categoria
            item {
                Text(
                    text = stringResource(R.string.create_activity_category_label),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(10.dp))
                CategorySelector(
                    selected = uiState.category,
                    onSelect = { onAction(CreateActivityAction.OnCategorySelect(it)) },
                )
            }

            // Imagem
            item {
                MediaUploadField(
                    icon = {
                        Icon(Icons.Outlined.Image, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(20.dp))
                    },
                    label = stringResource(R.string.create_activity_image_label),
                    fileName = uiState.imageFile?.fileName,
                    onClick = {
                        imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                )
            }

            // Áudio
            item {
                MediaUploadField(
                    icon = {
                        Icon(Icons.Outlined.AudioFile, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(20.dp))
                    },
                    label = stringResource(R.string.create_activity_audio_label),
                    fileName = uiState.audioFile?.fileName,
                    onClick = { audioPicker.launch("audio/*") },
                )
            }

            // Enunciado
            item {
                OutlinedTextField(
                    value = uiState.enunciado,
                    onValueChange = { onAction(CreateActivityAction.OnEnunciadoChange(it)) },
                    label = { Text("Enunciado *") },
                    placeholder = { Text("Ex: Identifique a sílaba inicial da palavra mostrada na imagem", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp) },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = fieldColors(),
                )
            }

            // Alternativas
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Alternativas de Resposta *", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    }
                    Text(
                        "Preencha as alternativas e marque qual é a correta clicando no botão ao lado",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                    )
                    uiState.alternatives.forEachIndexed { index, alt ->
                        AlternativeRow(
                            label = alt.label,
                            text = alt.text,
                            isCorrect = alt.isCorrect,
                            onTextChange = { onAction(CreateActivityAction.OnAlternativeTextChange(index, it)) },
                            onMarkCorrect = { onAction(CreateActivityAction.OnMarkCorrect(index)) },
                        )
                    }
                }
            }

            // Erro
            uiState.errorMessage?.let { error ->
                item {
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFC62828),
                    )
                }
            }

            // Botões
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedButton(
                        onClick = onCancelClick,
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
                    ) {
                        Text("Cancelar", fontWeight = FontWeight.SemiBold)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Brush.horizontalGradient(listOf(TealDark, TealLight)))
                            .clickable(enabled = !uiState.isSaving) { onAction(CreateActivityAction.OnSave) },
                        contentAlignment = Alignment.Center,
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.Book, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Criar Atividade", color = Color.White, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun MediaUploadField(
    icon: @Composable () -> Unit,
    label: String,
    fileName: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val hasFile = fileName != null
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, if (hasFile) TealPrimary else MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            icon()
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = fileName ?: label,
                style = MaterialTheme.typography.bodyMedium,
                color = if (hasFile) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
        }
        Icon(
            imageVector = if (hasFile) Icons.Outlined.CheckCircle else Icons.Outlined.Upload,
            contentDescription = null,
            tint = if (hasFile) TealPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp),
        )
    }
}

/** Lê um `Uri` selecionado (Photo Picker / SAF) e o converte em [FileUpload] com bytes resolvidos. */
private fun Context.toFileUpload(uri: Uri, fallbackName: String): FileUpload? = runCatching {
    val mime = contentResolver.getType(uri) ?: "application/octet-stream"
    val displayName = contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) cursor.getString(0) else null
    }
    val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return null
    FileUpload(
        fileName = displayName ?: fallbackName,
        bytes = bytes,
        mimeType = mime,
    )
}.getOrNull()

@Composable
private fun AlternativeRow(
    label: String,
    text: String,
    isCorrect: Boolean,
    onTextChange: (String) -> Unit,
    onMarkCorrect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Letra (A, B, C, D)
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(TealPrimary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = TealPrimary)
        }

        OutlinedTextField(
            value = text,
            onValueChange = onTextChange,
            placeholder = { Text("Alternativa $label", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) },
            singleLine = true,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(8.dp),
            colors = fieldColors(),
        )

        // Botão Marcar
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(if (isCorrect) TealPrimary else MaterialTheme.colorScheme.surface)
                .border(1.dp, if (isCorrect) TealPrimary else MaterialTheme.colorScheme.outline, RoundedCornerShape(6.dp))
                .clickable { onMarkCorrect() }
                .padding(horizontal = 10.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Marcar",
                style = MaterialTheme.typography.labelSmall,
                color = if (isCorrect) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = TealPrimary,
    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
)

@Composable
private fun navItemColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = TealPrimary,
    selectedTextColor = TealPrimary,
    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
    indicatorColor = Color.Transparent,
)
