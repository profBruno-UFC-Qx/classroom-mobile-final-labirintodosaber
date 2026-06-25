package com.labirintodosaber.ui.screen.activities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.labirintodosaber.data.model.NotebookWithGroups
import com.labirintodosaber.data.model.Task
import com.labirintodosaber.data.model.TaskCategory
import com.labirintodosaber.data.model.TaskGroup
import com.labirintodosaber.data.remote.ApiResult
import com.labirintodosaber.data.repository.TaskGroupRepository
import com.labirintodosaber.data.repository.TaskNotebookRepository
import com.labirintodosaber.data.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ActivitiesViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val taskGroupRepository: TaskGroupRepository,
    private val taskNotebookRepository: TaskNotebookRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ActivitiesUiState())
    val uiState: StateFlow<ActivitiesUiState> = _uiState.asStateFlow()

    fun onAction(action: ActivitiesAction) {
        when (action) {
            is ActivitiesAction.OnTabSelected -> _uiState.update { it.copy(selectedTab = action.tab) }
            is ActivitiesAction.OnSearchChange -> _uiState.update { it.copy(searchQuery = action.query) }
            ActivitiesAction.OnRetry -> load()
        }
    }

    /** Recarrega ao entrar e sempre que a tela é retomada (volta de criação de tarefa/grupo/caderno). */
    fun refresh() = load()

    private fun load() {
        viewModelScope.launch {
            val isFirstLoad = _uiState.value.allItems.isEmpty()
            if (isFirstLoad) _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val tasksDeferred = async { taskRepository.list() }
            val groupsDeferred = async { taskGroupRepository.listByEducator() }
            val notebooksDeferred = async { taskNotebookRepository.list() }

            val tasksResult = tasksDeferred.await()
            val groupsResult = groupsDeferred.await()
            val notebooksResult = notebooksDeferred.await()

            val firstError = listOf(tasksResult, groupsResult, notebooksResult)
                .filterIsInstance<ApiResult.Error>()
                .firstOrNull()
            if (firstError != null) {
                // Em refresh silencioso (já há dados), mantém o conteúdo atual em vez de mostrar erro.
                _uiState.update {
                    if (isFirstLoad) it.copy(isLoading = false, errorMessage = firstError.message)
                    else it.copy(isLoading = false)
                }
                return@launch
            }

            val tasks = (tasksResult as ApiResult.Success).data.map { it.toCardItem() }
            val groups = (groupsResult as ApiResult.Success).data.map { it.toCardItem() }
            val notebooks = (notebooksResult as ApiResult.Success).data.map { it.toCardItem() }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = null,
                    tasks = tasks,
                    groups = groups,
                    notebooks = notebooks,
                    allItems = notebooks + groups + tasks,
                )
            }
        }
    }
}

// ── Mapeamento domínio → UI ─────────────────────────────────────────────────────

private fun Task.toCardItem() = ActivityCardItem(
    id = id,
    title = prompt,
    subtitle = "${alternatives.size} alternativas",
    categories = listOf(category.displayName()),
    iconType = ActivityIconType.DOCUMENT,
    iconColorHex = category.colorHex(),
)

private fun TaskGroup.toCardItem() = ActivityCardItem(
    id = id,
    title = name,
    subtitle = "${tasksIds.size} atividades",
    categories = listOf(category.displayName()),
    iconType = ActivityIconType.FOLDER,
    iconColorHex = category.colorHex(),
)

private fun NotebookWithGroups.toCardItem() = ActivityCardItem(
    id = notebook.id,
    title = notebook.description,
    subtitle = "${notebook.tasks.size} atividades · ${taskGroups.size} grupos",
    categories = listOf(notebook.category.displayName()),
    iconType = ActivityIconType.BOOK,
    iconColorHex = notebook.category.colorHex(),
)

internal fun TaskCategory.displayName() = when (this) {
    TaskCategory.READING -> "Leitura"
    TaskCategory.WRITING -> "Escrita"
    TaskCategory.VOCABULARY -> "Vocabulário"
    TaskCategory.COMPREHENSION -> "Compreensão"
}

internal fun TaskCategory.colorHex(): Long = when (this) {
    TaskCategory.READING -> 0xFF5CC8C0
    TaskCategory.WRITING -> 0xFF50C878
    TaskCategory.VOCABULARY -> 0xFFE94B8F
    TaskCategory.COMPREHENSION -> 0xFFE5A820
}

enum class ActivitiesTab { ALL, NOTEBOOKS, GROUPS, TASKS }

data class ActivitiesUiState(
    val selectedTab: ActivitiesTab = ActivitiesTab.ALL,
    val searchQuery: String = "",
    val allItems: List<ActivityCardItem> = emptyList(),
    val notebooks: List<ActivityCardItem> = emptyList(),
    val groups: List<ActivityCardItem> = emptyList(),
    val tasks: List<ActivityCardItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
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
    data object OnRetry : ActivitiesAction
}
