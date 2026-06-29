package com.labirintodosaber.ui.screen.activities

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.labirintodosaber.R
import com.labirintodosaber.ui.components.ProfileActionIcon
import com.labirintodosaber.ui.theme.TealPrimary

@Composable
fun ActivitiesScreen(
    onHomeClick: () -> Unit,
    onStudentsClick: () -> Unit,
    onReportsClick: () -> Unit,
    onCreateActivityClick: () -> Unit,
    onCreateNotebookClick: () -> Unit,
    onCreateGroupClick: () -> Unit,
    onNotebookClick: (String) -> Unit,
    onGroupClick: (String) -> Unit,
    onTaskClick: (String) -> Unit,
    onMenuClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: ActivitiesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Recarrega ao entrar e ao retomar a tela (ex.: voltar de criar tarefa/grupo/caderno).
    LifecycleResumeEffect(Unit) {
        viewModel.refresh()
        onPauseOrDispose { }
    }

    ActivitiesContent(
        uiState = uiState,
        onAction = viewModel::onAction,
        onHomeClick = onHomeClick,
        onStudentsClick = onStudentsClick,
        onReportsClick = onReportsClick,
        onCreateActivityClick = onCreateActivityClick,
        onCreateNotebookClick = onCreateNotebookClick,
        onCreateGroupClick = onCreateGroupClick,
        onNotebookClick = onNotebookClick,
        onGroupClick = onGroupClick,
        onTaskClick = onTaskClick,
        onMenuClick = onMenuClick,
        onProfileClick = onProfileClick,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActivitiesContent(
    uiState: ActivitiesUiState,
    onAction: (ActivitiesAction) -> Unit,
    onHomeClick: () -> Unit,
    onStudentsClick: () -> Unit,
    onReportsClick: () -> Unit,
    onCreateActivityClick: () -> Unit,
    onCreateNotebookClick: () -> Unit,
    onCreateGroupClick: () -> Unit,
    onNotebookClick: (String) -> Unit,
    onGroupClick: (String) -> Unit,
    onTaskClick: (String) -> Unit,
    onMenuClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var showCreateMenu by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val dismissSheet: (() -> Unit) -> Unit = { action ->
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            if (!sheetState.isVisible) { showCreateMenu = false; action() }
        }
    }

    val currentItems = when (uiState.selectedTab) {
        ActivitiesTab.ALL -> uiState.allItems
        ActivitiesTab.NOTEBOOKS -> uiState.notebooks
        ActivitiesTab.GROUPS -> uiState.groups
        ActivitiesTab.TASKS -> uiState.tasks
    }.filter {
        uiState.searchQuery.isBlank() ||
                it.title.contains(uiState.searchQuery, ignoreCase = true)
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.activities_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Outlined.Menu, contentDescription = stringResource(R.string.dashboard_menu_desc), tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                actions = {
                    ProfileActionIcon(onClick = onProfileClick)
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
            )
        },
        bottomBar = {
            ActivitiesBottomBar(
                onHomeClick = onHomeClick,
                onStudentsClick = onStudentsClick,
                onReportsClick = onReportsClick,
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 16.dp),
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                ) {
                    // "+ Criar novo conteúdo" — abre bottom sheet
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { showCreateMenu = true },
                        ) {
                            Icon(Icons.Outlined.Add, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "Criar novo conteúdo",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = TealPrimary,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Search bar
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = { onAction(ActivitiesAction.OnSearchChange(it)) },
                        placeholder = {
                            Text(
                                text = "Buscar caderno por nome...",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        },
                        leadingIcon = {
                            Icon(Icons.Outlined.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(50.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.outline,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Tabs em chips pill
                    TabChips(
                        selectedTab = uiState.selectedTab,
                        onTabSelected = { onAction(ActivitiesAction.OnTabSelected(it)) },
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                }
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }

            when {
                uiState.isLoading -> item {
                    Box(modifier = Modifier.fillMaxWidth().padding(top = 48.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = TealPrimary)
                    }
                }

                uiState.errorMessage != null -> item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(
                            text = uiState.errorMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50.dp))
                                .background(TealPrimary)
                                .clickable { onAction(ActivitiesAction.OnRetry) }
                                .padding(horizontal = 20.dp, vertical = 10.dp),
                        ) {
                            Text(
                                text = stringResource(R.string.students_retry),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                            )
                        }
                    }
                }

                currentItems.isEmpty() -> item {
                    Box(modifier = Modifier.fillMaxWidth().padding(top = 48.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = stringResource(R.string.activities_empty),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                else -> items(currentItems) { item ->
                    val onClick: () -> Unit = when (item.iconType) {
                        ActivityIconType.BOOK -> { { onNotebookClick(item.id) } }
                        ActivityIconType.FOLDER -> { { onGroupClick(item.id) } }
                        ActivityIconType.DOCUMENT -> { { onTaskClick(item.id) } }
                    }
                    ActivityCard(
                        item = item,
                        onClick = onClick,
                        modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 12.dp),
                    )
                }
            }
        }
    }

    if (showCreateMenu) {
        ModalBottomSheet(
            onDismissRequest = { showCreateMenu = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            CreateSheetContent(
                onActivityClick = { dismissSheet { onCreateActivityClick() } },
                onGroupClick = { dismissSheet { onCreateGroupClick() } },
                onNotebookClick = { dismissSheet { onCreateNotebookClick() } },
            )
        }
    }
}

// ── Tab chips ─────────────────────────────────────────────────────────────────

@Composable
private fun TabChips(
    selectedTab: ActivitiesTab,
    onTabSelected: (ActivitiesTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tabs = listOf(
        ActivitiesTab.ALL to "Ver Tudo",
        ActivitiesTab.NOTEBOOKS to "Cadernos",
        ActivitiesTab.GROUPS to "Grupos",
        ActivitiesTab.TASKS to "Atividades",
    )
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(tabs) { (tab, label) ->
            val isSelected = selectedTab == tab
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(if (isSelected) TealPrimary else MaterialTheme.colorScheme.surface)
                    .border(1.dp, if (isSelected) TealPrimary else MaterialTheme.colorScheme.outline, RoundedCornerShape(50.dp))
                    .clickable { onTabSelected(tab) }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

// ── Activity Card ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ActivityCard(
    item: ActivityCardItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val iconColor = Color(item.iconColorHex)

    Card(
        modifier = modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Ícone colorido
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = item.iconType.icon(),
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(26.dp),
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = item.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    item.categories.forEach { category ->
                        CategoryPill(label = category)
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryPill(label: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50.dp))
            .background(Color(0xFFF0F0F0))
            .padding(horizontal = 10.dp, vertical = 3.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 10.sp,
        )
    }
}

private fun ActivityIconType.icon(): ImageVector = when (this) {
    ActivityIconType.BOOK -> Icons.Outlined.Book
    ActivityIconType.FOLDER -> Icons.Outlined.Folder
    ActivityIconType.DOCUMENT -> Icons.Outlined.Article
}

// ── Bottom navigation ─────────────────────────────────────────────────────────

@Composable
private fun ActivitiesBottomBar(
    onHomeClick: () -> Unit,
    onStudentsClick: () -> Unit,
    onReportsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp,
    ) {
        NavigationBarItem(
            selected = false,
            onClick = onHomeClick,
            icon = { Icon(Icons.Outlined.Home, contentDescription = null) },
            label = { Text(stringResource(R.string.dashboard_tab_home), style = MaterialTheme.typography.labelSmall) },
            colors = navItemColors(),
        )
        NavigationBarItem(
            selected = true,
            onClick = {},
            icon = { Icon(Icons.AutoMirrored.Outlined.MenuBook, contentDescription = null) },
            label = { Text(stringResource(R.string.dashboard_tab_activities), style = MaterialTheme.typography.labelSmall) },
            colors = navItemColors(),
        )
        NavigationBarItem(
            selected = false,
            onClick = onStudentsClick,
            icon = { Icon(Icons.Outlined.Person, contentDescription = null) },
            label = { Text(stringResource(R.string.dashboard_tab_students), style = MaterialTheme.typography.labelSmall) },
            colors = navItemColors(),
        )
        NavigationBarItem(
            selected = false,
            onClick = onReportsClick,
            icon = { Icon(Icons.Outlined.Article, contentDescription = null) },
            label = { Text(stringResource(R.string.dashboard_tab_reports), style = MaterialTheme.typography.labelSmall) },
            colors = navItemColors(),
        )
    }
}

// ── Create bottom sheet ───────────────────────────────────────────────────────

@Composable
private fun CreateSheetContent(
    onActivityClick: () -> Unit,
    onGroupClick: () -> Unit,
    onNotebookClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 36.dp),
    ) {
        Text(
            text = "O que deseja criar?",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Escolha o tipo de conteúdo",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(20.dp))
        CreateOptionRow(
            icon = Icons.Outlined.Article,
            iconColor = Color(0xFF5CC8C0),
            title = "Atividade",
            subtitle = "Questão de múltipla escolha para o aluno responder",
            onClick = onActivityClick,
        )
        Spacer(modifier = Modifier.height(10.dp))
        CreateOptionRow(
            icon = Icons.Outlined.Folder,
            iconColor = Color(0xFFE5A820),
            title = "Grupo de Atividades",
            subtitle = "Reúna atividades por tema ou nível de dificuldade",
            onClick = onGroupClick,
        )
        Spacer(modifier = Modifier.height(10.dp))
        CreateOptionRow(
            icon = Icons.AutoMirrored.Outlined.MenuBook,
            iconColor = Color(0xFF50C878),
            title = "Caderno",
            subtitle = "Monte um caderno completo com grupos de atividades",
            onClick = onNotebookClick,
        )
    }
}

@Composable
private fun CreateOptionRow(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconColor.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(24.dp),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp),
        )
    }
}

@Composable
private fun navItemColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = TealPrimary,
    selectedTextColor = TealPrimary,
    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
    indicatorColor = Color.Transparent,
)
