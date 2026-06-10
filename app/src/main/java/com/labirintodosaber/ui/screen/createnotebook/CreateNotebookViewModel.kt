package com.labirintodosaber.ui.screen.createnotebook

import androidx.lifecycle.ViewModel
import com.labirintodosaber.data.model.TaskCategory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class CreateNotebookViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(CreateNotebookUiState())
    val uiState: StateFlow<CreateNotebookUiState> = _uiState.asStateFlow()

    fun onAction(action: CreateNotebookAction) {
        when (action) {
            is CreateNotebookAction.OnNameChange ->
                _uiState.update { it.copy(name = action.name) }
            is CreateNotebookAction.OnDescriptionChange ->
                _uiState.update { it.copy(description = action.description) }
            is CreateNotebookAction.OnCategoryToggle ->
                _uiState.update { state ->
                    val updated = if (action.category in state.selectedCategories)
                        state.selectedCategories - action.category
                    else
                        state.selectedCategories + action.category
                    state.copy(selectedCategories = updated)
                }
            is CreateNotebookAction.OnGroupToggle ->
                _uiState.update { state ->
                    val updated = if (action.groupId in state.selectedGroupIds)
                        state.selectedGroupIds - action.groupId
                    else
                        state.selectedGroupIds + action.groupId
                    state.copy(selectedGroupIds = updated)
                }
            CreateNotebookAction.OnSave -> {
                if (_uiState.value.name.isNotBlank())
                    _uiState.update { it.copy(saveSuccess = true) }
            }
            CreateNotebookAction.OnCancel -> {}
        }
    }
}

data class CreateNotebookUiState(
    val name: String = "",
    val description: String = "",
    val selectedCategories: Set<TaskCategory> = emptySet(),
    val availableGroups: List<GroupOption> = defaultGroups(),
    val selectedGroupIds: Set<String> = emptySet(),
    val saveSuccess: Boolean = false,
)

data class GroupOption(
    val id: String,
    val name: String,
    val description: String,
    val iconColorHex: Long,
)

sealed interface CreateNotebookAction {
    data class OnNameChange(val name: String) : CreateNotebookAction
    data class OnDescriptionChange(val description: String) : CreateNotebookAction
    data class OnCategoryToggle(val category: TaskCategory) : CreateNotebookAction
    data class OnGroupToggle(val groupId: String) : CreateNotebookAction
    data object OnSave : CreateNotebookAction
    data object OnCancel : CreateNotebookAction
}

private fun defaultGroups() = listOf(
    GroupOption("g1", "Reconhecimento de Letras", "Identificação de letras do alfabeto", 0xFF5CC8C0),
    GroupOption("g2", "Formação de Sílabas", "Combinação de letras para formar sílabas", 0xFF7EC8C8),
    GroupOption("g3", "Palavras Simples", "Leitura de palavras de fácil compreensão", 0xFFF4A0A0),
    GroupOption("g4", "Numeração Básica", "Reconhecimento de números de 1 a 10", 0xFFE5A820),
)
