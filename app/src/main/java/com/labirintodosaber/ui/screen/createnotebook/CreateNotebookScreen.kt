package com.labirintodosaber.ui.screen.createnotebook

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
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
import com.labirintodosaber.data.model.TaskCategory
import com.labirintodosaber.ui.theme.InputBorder
import com.labirintodosaber.ui.theme.TealDark
import com.labirintodosaber.ui.theme.TealLight
import com.labirintodosaber.ui.theme.TealPrimary
import com.labirintodosaber.ui.theme.TextPrimary
import com.labirintodosaber.ui.theme.TextSecondary

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
                        color = TextPrimary,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onCancelClick) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = null, tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White),
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White, tonalElevation = 4.dp) {
                NavigationBarItem(selected = false, onClick = {}, icon = { Icon(Icons.Outlined.Menu, contentDescription = null) }, label = { Text(stringResource(R.string.dashboard_tab_home), style = MaterialTheme.typography.labelSmall) }, colors = navItemColors())
                NavigationBarItem(selected = true, onClick = {}, icon = { Icon(Icons.Outlined.Book, contentDescription = null) }, label = { Text(stringResource(R.string.dashboard_tab_activities), style = MaterialTheme.typography.labelSmall) }, colors = navItemColors())
                NavigationBarItem(selected = false, onClick = {}, icon = { Icon(Icons.Outlined.Folder, contentDescription = null) }, label = { Text(stringResource(R.string.dashboard_tab_students), style = MaterialTheme.typography.labelSmall) }, colors = navItemColors())
                NavigationBarItem(selected = false, onClick = {}, icon = { Icon(Icons.Outlined.AccountCircle, contentDescription = null) }, label = { Text(stringResource(R.string.dashboard_tab_reports), style = MaterialTheme.typography.labelSmall) }, colors = navItemColors())
            }
        },
        containerColor = Color.White,
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
                    placeholder = { Text("Ex: Alfabetização Divertida", color = TextSecondary) },
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
                    placeholder = { Text("Descreva o objetivo deste caderno...", color = TextSecondary) },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = fieldColors(),
                )
            }

            item {
                SectionHeader(
                    title = "Categorias *",
                    actionLabel = "+ Criar Categoria",
                    onActionClick = {},
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text("Selecione as categorias que este caderno abrange", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Spacer(modifier = Modifier.height(10.dp))
                CategoryChips(
                    selected = uiState.selectedCategories,
                    onToggle = { onAction(CreateNotebookAction.OnCategoryToggle(it)) },
                )
            }

            item {
                SectionHeader(
                    title = "Grupos de Atividades",
                    actionLabel = "+ Criar Grupo",
                    onActionClick = {},
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text("Selecione grupos existentes ou crie novos", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Spacer(modifier = Modifier.height(10.dp))
                GroupGrid(
                    groups = uiState.availableGroups,
                    selectedIds = uiState.selectedGroupIds,
                    onToggle = { onAction(CreateNotebookAction.OnGroupToggle(it)) },
                )
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
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                    ) {
                        Text("Cancelar", fontWeight = FontWeight.SemiBold)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Brush.horizontalGradient(listOf(TealDark, TealLight)))
                            .clickable(enabled = uiState.name.isNotBlank()) { onAction(CreateNotebookAction.OnSave) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Book, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Criar Caderno", color = Color.White, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    actionLabel: String,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50.dp))
                .background(TealPrimary)
                .clickable { onActionClick() }
                .padding(horizontal = 12.dp, vertical = 6.dp),
        ) {
            Text(actionLabel, style = MaterialTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.SemiBold)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategoryChips(
    selected: Set<TaskCategory>,
    onToggle: (TaskCategory) -> Unit,
    modifier: Modifier = Modifier,
) {
    val categories = listOf(
        TaskCategory.READING to "Leitura",
        TaskCategory.WRITING to "Escrita",
        TaskCategory.VOCABULARY to "Vocabulário",
        TaskCategory.COMPREHENSION to "Compreensão",
    )
    FlowRow(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        categories.forEach { (cat, label) ->
            val isSelected = cat in selected
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) TealPrimary else Color.White)
                    .border(1.dp, if (isSelected) TealPrimary else InputBorder, RoundedCornerShape(8.dp))
                    .clickable { onToggle(cat) }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
            ) {
                Text(label, style = MaterialTheme.typography.bodySmall, color = if (isSelected) Color.White else TextPrimary)
            }
        }
        // Matemática (display only, sem enum correspondente)
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White)
                .border(1.dp, InputBorder, RoundedCornerShape(8.dp))
                .padding(horizontal = 14.dp, vertical = 8.dp),
        ) {
            Text("Matemática", style = MaterialTheme.typography.bodySmall, color = TextPrimary)
        }
    }
}

@Composable
private fun GroupGrid(
    groups: List<GroupOption>,
    selectedIds: Set<String>,
    onToggle: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Usamos Column com rows de 2 colunas pois LazyVerticalGrid não pode estar em LazyColumn
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        groups.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { group ->
                    val isSelected = group.id in selectedIds
                    val iconColor = Color(group.iconColorHex)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .border(1.5.dp, if (isSelected) TealPrimary else InputBorder, RoundedCornerShape(12.dp))
                            .clickable { onToggle(group.id) }
                            .padding(12.dp),
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(iconColor.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(Icons.Outlined.Folder, contentDescription = null, tint = iconColor, modifier = Modifier.size(16.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(group.name, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(group.description, style = MaterialTheme.typography.labelSmall, color = TextSecondary, fontSize = 10.sp, lineHeight = 13.sp)
                        }
                    }
                }
                // filler se row tiver só 1 item
                if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = TealPrimary,
    unfocusedBorderColor = InputBorder,
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
)

@Composable
private fun navItemColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = TealPrimary,
    selectedTextColor = TealPrimary,
    unselectedIconColor = TextSecondary,
    unselectedTextColor = TextSecondary,
    indicatorColor = Color.Transparent,
)
