package com.labirintodosaber.ui.screen.createnotebook

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.labirintodosaber.data.model.TaskCategory
import com.labirintodosaber.data.remote.ApiResult
import com.labirintodosaber.data.remote.dto.TaskNotebookCreateRequest
import com.labirintodosaber.data.remote.getOrNull
import com.labirintodosaber.data.repository.TaskGroupRepository
import com.labirintodosaber.data.repository.TaskNotebookRepository
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
class CreateNotebookViewModel @Inject constructor(
    private val taskNotebookRepository: TaskNotebookRepository,
    private val taskGroupRepository: TaskGroupRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateNotebookUiState())
    val uiState: StateFlow<CreateNotebookUiState> = _uiState.asStateFlow()

    init {
        loadGroups()
    }

    fun onAction(action: CreateNotebookAction) {
        when (action) {
            is CreateNotebookAction.OnDescriptionChange ->
                _uiState.update { it.copy(description = action.description, errorMessage = null) }
            is CreateNotebookAction.OnCategorySelect ->
                _uiState.update { it.copy(category = action.category, errorMessage = null) }
            is CreateNotebookAction.OnGroupToggle ->
                _uiState.update { state ->
                    val updated = if (action.groupId in state.selectedGroupIds)
                        state.selectedGroupIds - action.groupId
                    else
                        state.selectedGroupIds + action.groupId
                    state.copy(selectedGroupIds = updated, errorMessage = null)
                }
            CreateNotebookAction.OnRetryGroups -> loadGroups()
            CreateNotebookAction.OnSave -> save()
            CreateNotebookAction.OnCancel -> {}
        }
    }

    private fun loadGroups() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingGroups = true, groupsError = null) }
            val groups = taskGroupRepository.listByEducator().getOrNull()
            if (groups == null) {
                _uiState.update { it.copy(isLoadingGroups = false, groupsError = "Não foi possível carregar os grupos.") }
                return@launch
            }
            _uiState.update {
                it.copy(
                    isLoadingGroups = false,
                    availableGroups = groups.map { group ->
                        GroupOption(
                            id = group.id,
                            name = group.name,
                            categoryLabel = group.category.displayName(),
                            taskIds = group.tasksIds,
                            iconColorHex = group.category.colorHex(),
                        )
                    },
                )
            }
        }
    }

    private fun save() {
        val state = _uiState.value
        // O caderno é composto por grupos; as tarefas vêm das tarefas desses grupos.
        val derivedTasks = state.availableGroups
            .filter { it.id in state.selectedGroupIds }
            .flatMap { it.taskIds }
            .distinct()

        val validationError = when {
            state.description.isBlank() -> "Informe a descrição do caderno."
            state.category == null -> "Selecione uma categoria."
            state.selectedGroupIds.isEmpty() -> "Selecione ao menos um grupo."
            derivedTasks.isEmpty() -> "Os grupos selecionados não têm atividades. Adicione atividades a eles primeiro."
            else -> null
        }
        if (validationError != null) {
            _uiState.update { it.copy(errorMessage = validationError) }
            return
        }

        val request = TaskNotebookCreateRequest(
            tasks = derivedTasks,
            category = checkNotNull(state.category),
            description = state.description.trim(),
            taskGroupsIds = state.selectedGroupIds.toList(),
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            when (val result = taskNotebookRepository.create(request)) {
                is ApiResult.Success -> _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
                is ApiResult.Error -> _uiState.update { it.copy(isSaving = false, errorMessage = result.message) }
            }
        }
    }
}

data class CreateNotebookUiState(
    val description: String = "",
    val category: TaskCategory? = null,
    val availableGroups: List<GroupOption> = emptyList(),
    val selectedGroupIds: Set<String> = emptySet(),
    val isLoadingGroups: Boolean = false,
    val groupsError: String? = null,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val saveSuccess: Boolean = false,
)

data class GroupOption(
    val id: String,
    val name: String,
    val categoryLabel: String,
    val taskIds: List<String>,
    val iconColorHex: Long,
)

sealed interface CreateNotebookAction {
    data class OnDescriptionChange(val description: String) : CreateNotebookAction
    data class OnCategorySelect(val category: TaskCategory) : CreateNotebookAction
    data class OnGroupToggle(val groupId: String) : CreateNotebookAction
    data object OnRetryGroups : CreateNotebookAction
    data object OnSave : CreateNotebookAction
    data object OnCancel : CreateNotebookAction
}
