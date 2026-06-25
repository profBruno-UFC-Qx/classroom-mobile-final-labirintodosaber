package com.labirintodosaber.ui.screen.notebookdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.labirintodosaber.data.model.Task
import com.labirintodosaber.data.remote.getOrNull
import com.labirintodosaber.data.repository.TaskNotebookRepository
import com.labirintodosaber.data.repository.TaskRepository
import com.labirintodosaber.ui.screen.activities.colorHex
import com.labirintodosaber.ui.screen.activities.displayName
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotebookDetailViewModel @Inject constructor(
    private val taskNotebookRepository: TaskNotebookRepository,
    private val taskRepository: TaskRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val notebookId: String = checkNotNull(savedStateHandle["notebookId"])

    private val _uiState = MutableStateFlow(NotebookDetailUiState())
    val uiState: StateFlow<NotebookDetailUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun onRetry() = load()

    private fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val item = taskNotebookRepository.list(id = notebookId).getOrNull()?.firstOrNull()
            if (item == null) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Caderno não encontrado.") }
                return@launch
            }
            val notebook = item.notebook

            val allTasks = taskRepository.list().getOrNull().orEmpty()
            val tasks = notebook.tasks.mapNotNull { id -> allTasks.firstOrNull { it.id == id } }
                .map { it.toRow() }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    title = notebook.description,
                    categoryLabel = notebook.category.displayName(),
                    iconColorHex = notebook.category.colorHex(),
                    groupCount = item.taskGroups.size,
                    tasks = tasks,
                )
            }
        }
    }
}

private fun Task.toRow() = NotebookTaskRow(
    id = id,
    prompt = prompt,
    categoryLabel = category.displayName(),
    alternativeCount = alternatives.size,
    iconColorHex = category.colorHex(),
)

data class NotebookTaskRow(
    val id: String,
    val prompt: String,
    val categoryLabel: String,
    val alternativeCount: Int,
    val iconColorHex: Long,
)

data class NotebookDetailUiState(
    val title: String = "",
    val categoryLabel: String = "",
    val iconColorHex: Long = 0xFF5CC8C0,
    val groupCount: Int = 0,
    val tasks: List<NotebookTaskRow> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)
