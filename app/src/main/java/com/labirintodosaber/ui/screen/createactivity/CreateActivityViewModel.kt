package com.labirintodosaber.ui.screen.createactivity

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class CreateActivityViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(CreateActivityUiState())
    val uiState: StateFlow<CreateActivityUiState> = _uiState.asStateFlow()

    fun onAction(action: CreateActivityAction) {
        when (action) {
            is CreateActivityAction.OnNameChange ->
                _uiState.update { it.copy(name = action.name) }
            is CreateActivityAction.OnEnunciadoChange ->
                _uiState.update { it.copy(enunciado = action.text) }
            is CreateActivityAction.OnAlternativeTextChange ->
                _uiState.update { state ->
                    state.copy(
                        alternatives = state.alternatives.mapIndexed { i, alt ->
                            if (i == action.index) alt.copy(text = action.text) else alt
                        },
                    )
                }
            is CreateActivityAction.OnMarkCorrect ->
                _uiState.update { state ->
                    state.copy(
                        alternatives = state.alternatives.mapIndexed { i, alt ->
                            alt.copy(isCorrect = i == action.index)
                        },
                    )
                }
            CreateActivityAction.OnSave -> {
                if (_uiState.value.name.isNotBlank())
                    _uiState.update { it.copy(saveSuccess = true) }
            }
            CreateActivityAction.OnCancel -> {}
        }
    }
}

data class CreateActivityUiState(
    val name: String = "",
    val enunciado: String = "",
    val alternatives: List<AlternativeInput> = listOf(
        AlternativeInput(label = "A"),
        AlternativeInput(label = "B"),
        AlternativeInput(label = "C"),
        AlternativeInput(label = "D"),
    ),
    val saveSuccess: Boolean = false,
)

data class AlternativeInput(
    val label: String,
    val text: String = "",
    val isCorrect: Boolean = false,
)

sealed interface CreateActivityAction {
    data class OnNameChange(val name: String) : CreateActivityAction
    data class OnEnunciadoChange(val text: String) : CreateActivityAction
    data class OnAlternativeTextChange(val index: Int, val text: String) : CreateActivityAction
    data class OnMarkCorrect(val index: Int) : CreateActivityAction
    data object OnSave : CreateActivityAction
    data object OnCancel : CreateActivityAction
}
