package com.labirintodosaber.ui.screen.createtaskgroup

import androidx.lifecycle.ViewModel
import com.labirintodosaber.data.model.TaskCategory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class CreateTaskGroupViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(CreateTaskGroupUiState())
    val uiState: StateFlow<CreateTaskGroupUiState> = _uiState.asStateFlow()

    fun onAction(action: CreateTaskGroupAction) {
        when (action) {
            is CreateTaskGroupAction.OnNameChange ->
                _uiState.update { it.copy(name = action.name) }
            is CreateTaskGroupAction.OnDescriptionChange ->
                _uiState.update { it.copy(description = action.description) }
            is CreateTaskGroupAction.OnCategoryToggle ->
                _uiState.update { state ->
                    val updated = if (action.category in state.selectedCategories)
                        state.selectedCategories - action.category
                    else
                        state.selectedCategories + action.category
                    state.copy(selectedCategories = updated)
                }
            CreateTaskGroupAction.OnSave -> {
                if (_uiState.value.name.isNotBlank())
                    _uiState.update { it.copy(saveSuccess = true) }
            }
            CreateTaskGroupAction.OnCancel -> {}
        }
    }
}

data class CreateTaskGroupUiState(
    val name: String = "",
    val description: String = "",
    val selectedCategories: Set<TaskCategory> = emptySet(),
    val saveSuccess: Boolean = false,
)

sealed interface CreateTaskGroupAction {
    data class OnNameChange(val name: String) : CreateTaskGroupAction
    data class OnDescriptionChange(val description: String) : CreateTaskGroupAction
    data class OnCategoryToggle(val category: TaskCategory) : CreateTaskGroupAction
    data object OnSave : CreateTaskGroupAction
    data object OnCancel : CreateTaskGroupAction
}
