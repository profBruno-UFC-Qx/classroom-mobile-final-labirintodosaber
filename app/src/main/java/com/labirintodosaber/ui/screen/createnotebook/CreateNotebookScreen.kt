package com.labirintodosaber.ui.screen.createnotebook

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Menu
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.labirintodosaber.R
import com.labirintodosaber.ui.components.CategorySelector
import com.labirintodosaber.ui.theme.TealDark
import com.labirintodosaber.ui.theme.TealLight
import com.labirintodosaber.ui.theme.TealPrimary

@Composable
fun CreateNotebookScreen(
    onBackClick: () -> Unit,
    onSaveSuccess: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CreateNotebookViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) onSaveSuccess()
    }

    CreateNotebookContent(
        uiState = uiState,
        onAction = viewModel::onAction,
        onCancelClick = onBackClick,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateNotebookContent(
    uiState: CreateNotebookUiState,
    onAction: (CreateNotebookAction) -> Unit,
    onCancelClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.create_notebook_title),
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
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            item {
                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = { onAction(CreateNotebookAction.OnNameChange(it)) },
                    label = { Text(stringResource(R.string.create_notebook_name_label)) },
                    placeholder = { Text("Ex: Alfabetização Divertida", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = fieldColors(),
                )
            }

            item {
                OutlinedTextField(
                    value = uiState.description,
                    onValueChange = { onAction(CreateNotebookAction.OnDescriptionChange(it)) },
                    label = { Text(stringResource(R.string.create_notebook_description_label)) },
                    placeholder = { Text("Descreva o objetivo deste caderno...", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = fieldColors(),
                )
            }

            item {
                Text("Categoria *", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(10.dp))
                CategorySelector(
                    selected = uiState.category,
                    onSelect = { onAction(CreateNotebookAction.OnCategorySelect(it)) },
                )
            }

            // Grupos de atividades (obrigatório, mín. 1)
            item {
                Text("Grupos de Atividades *", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Selecione os grupos que compõem este caderno", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            when {
                uiState.isLoadingGroups -> item {
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = TealPrimary)
                    }
                }
                uiState.groupsError != null -> item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(uiState.groupsError, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50.dp))
                                .background(TealPrimary)
                                .clickable { onAction(CreateNotebookAction.OnRetryGroups) }
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                        ) {
                            Text(stringResource(R.string.students_retry), style = MaterialTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
                uiState.availableGroups.isEmpty() -> item {
                    Text("Nenhum grupo cadastrado ainda. Crie um grupo de atividades primeiro.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                else -> item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        uiState.availableGroups.forEach { group ->
                            GroupSelectRow(
                                group = group,
                                selected = group.id in uiState.selectedGroupIds,
                                onToggle = { onAction(CreateNotebookAction.OnGroupToggle(group.id)) },
                            )
                        }
                    }
                }
            }

            uiState.errorMessage?.let { error ->
                item {
                    Text(error, style = MaterialTheme.typography.bodySmall, color = Color(0xFFC62828))
                }
            }

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
                            .clickable(enabled = !uiState.isSaving) { onAction(CreateNotebookAction.OnSave) },
                        contentAlignment = Alignment.Center,
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.Book, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(stringResource(R.string.create_notebook_button), color = Color.White, fontWeight = FontWeight.SemiBold)
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
private fun GroupSelectRow(
    group: GroupOption,
    selected: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val iconColor = Color(group.iconColorHex)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.5.dp, if (selected) TealPrimary else MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
            .clickable { onToggle() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(iconColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Outlined.Folder, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(group.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
            Text("${group.categoryLabel} · ${group.taskIds.size} atividades", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
        }
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(if (selected) TealPrimary else Color.Transparent)
                .border(1.5.dp, if (selected) TealPrimary else MaterialTheme.colorScheme.outline, RoundedCornerShape(11.dp)),
        )
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
