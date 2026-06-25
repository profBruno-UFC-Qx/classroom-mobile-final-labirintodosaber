package com.labirintodosaber.ui.screen.createtaskgroup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.labirintodosaber.data.model.TaskCategory
import com.labirintodosaber.data.remote.ApiResult
import com.labirintodosaber.data.remote.getOrNull
import com.labirintodosaber.data.repository.TaskGroupRepository
import com.labirintodosaber.data.repository.TaskRepository
import com.labirintodosaber.ui.screen.activities.displayName
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateTaskGroupViewModel @Inject constructor(
    private val taskGroupRepository: TaskGroupRepository,
    private val taskRepository: TaskRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateTaskGroupUiState())
    val uiState: StateFlow<CreateTaskGroupUiState> = _uiState.asStateFlow()

    init {
        loadTasks()
    }

    fun onAction(action: CreateTaskGroupAction) {
        when (action) {
            is CreateTaskGroupAction.OnNameChange ->
                _uiState.update { it.copy(name = action.name, errorMessage = null) }
            is CreateTaskGroupAction.OnCategorySelect ->
                _uiState.update { it.copy(category = action.category, errorMessage = null) }
            is CreateTaskGroupAction.OnTaskToggle ->
                _uiState.update { state ->
                    val updated = if (action.taskId in state.selectedTaskIds)
                        state.selectedTaskIds - action.taskId
                    else
                        state.selectedTaskIds + action.taskId
                    state.copy(selectedTaskIds = updated, errorMessage = null)
                }
            CreateTaskGroupAction.OnRetryTasks -> loadTasks()
            CreateTaskGroupAction.OnSave -> save()
            CreateTaskGroupAction.OnCancel -> {}
        }
    }

    private fun loadTasks() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingTasks = true, tasksError = null) }
            val tasks = taskRepository.list().getOrNull()
            if (tasks == null) {
                _uiState.update { it.copy(isLoadingTasks = false, tasksError = "Não foi possível carregar as atividades.") }
                return@launch
            }
            _uiState.update {
                it.copy(
                    isLoadingTasks = false,
                    availableTasks = tasks.map { task -> TaskOption(task.id, task.prompt, task.category.displayName()) },
                )
            }
        }
    }

    private fun save() {
        val state = _uiState.value
        val validationError = when {
            state.name.isBlank() -> "Informe o nome do grupo."
            state.category == null -> "Selecione uma categoria."
            state.selectedTaskIds.isEmpty() -> "Selecione ao menos uma atividade."
            else -> null
        }
        if (validationError != null) {
            _uiState.update { it.copy(errorMessage = validationError) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            val result = taskGroupRepository.create(
                name = state.name.trim(),
                category = checkNotNull(state.category),
                tasksIds = state.selectedTaskIds.toList(),
            )
            when (result) {
                is ApiResult.Success -> _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
                is ApiResult.Error -> _uiState.update { it.copy(isSaving = false, errorMessage = result.message) }
            }
        }
    }
}

data class CreateTaskGroupUiState(
    val name: String = "",
    val category: TaskCategory? = null,
    val availableTasks: List<TaskOption> = emptyList(),
    val selectedTaskIds: Set<String> = emptySet(),
    val isLoadingTasks: Boolean = false,
    val tasksError: String? = null,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val saveSuccess: Boolean = false,
)

data class TaskOption(
    val id: String,
    val prompt: String,
    val categoryLabel: String,
)

sealed interface CreateTaskGroupAction {
    data class OnNameChange(val name: String) : CreateTaskGroupAction
    data class OnCategorySelect(val category: TaskCategory) : CreateTaskGroupAction
    data class OnTaskToggle(val taskId: String) : CreateTaskGroupAction
    data object OnRetryTasks : CreateTaskGroupAction
    data object OnSave : CreateTaskGroupAction
    data object OnCancel : CreateTaskGroupAction
}
