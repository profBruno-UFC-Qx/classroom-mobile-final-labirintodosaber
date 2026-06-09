package com.labirintodosaber.ui.screen.activities

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class ActivitiesViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(ActivitiesUiState())
    val uiState: StateFlow<ActivitiesUiState> = _uiState.asStateFlow()

    fun onAction(action: ActivitiesAction) {
        when (action) {
            is ActivitiesAction.OnTabSelected -> _uiState.update { it.copy(selectedTab = action.tab) }
            is ActivitiesAction.OnSearchChange -> _uiState.update { it.copy(searchQuery = action.query) }
        }
    }
}

enum class ActivitiesTab { ALL, NOTEBOOKS, GROUPS, TASKS }

data class ActivitiesUiState(
    val selectedTab: ActivitiesTab = ActivitiesTab.ALL,
    val searchQuery: String = "",
    val allItems: List<ActivityCardItem> = buildAll(),
    val notebooks: List<ActivityCardItem> = buildNotebooks(),
    val groups: List<ActivityCardItem> = buildGroups(),
    val tasks: List<ActivityCardItem> = buildTasks(),
)

data class ActivityCardItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val categories: List<String>,
    val iconType: ActivityIconType,
    val iconColorHex: Long,
)

enum class ActivityIconType { BOOK, FOLDER, DOCUMENT }

sealed interface ActivitiesAction {
    data class OnTabSelected(val tab: ActivitiesTab) : ActivitiesAction
    data class OnSearchChange(val query: String) : ActivitiesAction
}

private fun buildNotebooks() = ActivitiesMockData.notebooks.map { n ->
    ActivityCardItem(
        id = n.id,
        title = n.title,
        subtitle = n.subtitle,
        categories = n.categories.map { it.displayName() } + listOf("${n.taskIds.size} atividades"),
        iconType = ActivityIconType.BOOK,
        iconColorHex = n.iconColorHex,
    )
}

private fun buildGroups() = ActivitiesMockData.groups.map { g ->
    ActivityCardItem(
        id = g.id,
        title = g.name,
        subtitle = g.description,
        categories = listOf(g.category.displayName(), "${g.notebookIds.size} cadernos"),
        iconType = ActivityIconType.FOLDER,
        iconColorHex = g.iconColorHex,
    )
}

private fun buildTasks() = ActivitiesMockData.tasks.map { t ->
    ActivityCardItem(
        id = t.id,
        title = t.prompt,
        subtitle = "${t.alternatives.size} alternativas",
        categories = listOf(t.category.displayName()),
        iconType = ActivityIconType.DOCUMENT,
        iconColorHex = 0xFFF4A0A0,
    )
}

private fun buildAll() = buildNotebooks() + buildGroups() + buildTasks()

private fun com.labirintodosaber.data.model.TaskCategory.displayName() = when (this) {
    com.labirintodosaber.data.model.TaskCategory.READING -> "Leitura"
    com.labirintodosaber.data.model.TaskCategory.WRITING -> "Escrita"
    com.labirintodosaber.data.model.TaskCategory.VOCABULARY -> "Vocabulário"
    com.labirintodosaber.data.model.TaskCategory.COMPREHENSION -> "Compreensão"
}
