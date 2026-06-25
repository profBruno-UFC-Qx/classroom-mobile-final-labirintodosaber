package com.labirintodosaber.ui.screen.createactivity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.labirintodosaber.data.model.TaskCategory
import com.labirintodosaber.data.model.TaskType
import com.labirintodosaber.data.remote.ApiResult
import com.labirintodosaber.data.remote.FileUpload
import com.labirintodosaber.data.remote.dto.TaskForm
import com.labirintodosaber.data.repository.TaskRepository
import com.labirintodosaber.data.remote.dto.AlternativeInput as AlternativeDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateActivityViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateActivityUiState())
    val uiState: StateFlow<CreateActivityUiState> = _uiState.asStateFlow()

    fun onAction(action: CreateActivityAction) {
        when (action) {
            is CreateActivityAction.OnNameChange ->
                _uiState.update { it.copy(name = action.name, errorMessage = null) }
            is CreateActivityAction.OnEnunciadoChange ->
                _uiState.update { it.copy(enunciado = action.text, errorMessage = null) }
            is CreateActivityAction.OnCategorySelect ->
                _uiState.update { it.copy(category = action.category, errorMessage = null) }
            is CreateActivityAction.OnImagePicked ->
                _uiState.update { it.copy(imageFile = action.file, errorMessage = null) }
            is CreateActivityAction.OnAudioPicked ->
                _uiState.update { it.copy(audioFile = action.file, errorMessage = null) }
            is CreateActivityAction.OnAlternativeTextChange ->
                _uiState.update { state ->
                    state.copy(
                        alternatives = state.alternatives.mapIndexed { i, alt ->
                            if (i == action.index) alt.copy(text = action.text) else alt
                        },
                        errorMessage = null,
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
            CreateActivityAction.OnSave -> save()
            CreateActivityAction.OnCancel -> {}
        }
    }

    private fun save() {
        val state = _uiState.value
        val filled = state.alternatives.filter { it.text.isNotBlank() }

        val validationError = when {
            state.name.isBlank() -> "Informe o nome da atividade."
            state.enunciado.isBlank() -> "Informe o enunciado."
            state.category == null -> "Selecione uma categoria."
            filled.size < 2 -> "Preencha ao menos 2 alternativas."
            filled.none { it.isCorrect } -> "Marque a alternativa correta."
            else -> null
        }
        if (validationError != null) {
            _uiState.update { it.copy(errorMessage = validationError) }
            return
        }

        val hasMedia = state.imageFile != null || state.audioFile != null
        val form = TaskForm(
            category = checkNotNull(state.category),
            type = if (hasMedia) TaskType.MULTIPLE_CHOICE_WITH_MEDIA else TaskType.MULTIPLE_CHOICE,
            prompt = state.enunciado.trim(),
            alternatives = filled.map { AlternativeDto(text = it.text.trim(), isCorrect = it.isCorrect) },
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            val result = taskRepository.create(
                form = form,
                imageFile = state.imageFile,
                audioFile = state.audioFile,
            )
            when (result) {
                is ApiResult.Success -> _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
                is ApiResult.Error -> _uiState.update { it.copy(isSaving = false, errorMessage = result.message) }
            }
        }
    }
}

data class CreateActivityUiState(
    val name: String = "",
    val enunciado: String = "",
    val category: TaskCategory? = null,
    val imageFile: FileUpload? = null,
    val audioFile: FileUpload? = null,
    val alternatives: List<AlternativeInput> = listOf(
        AlternativeInput(label = "A"),
        AlternativeInput(label = "B"),
        AlternativeInput(label = "C"),
        AlternativeInput(label = "D"),
    ),
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
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
    data class OnCategorySelect(val category: TaskCategory) : CreateActivityAction
    data class OnImagePicked(val file: FileUpload?) : CreateActivityAction
    data class OnAudioPicked(val file: FileUpload?) : CreateActivityAction
    data class OnAlternativeTextChange(val index: Int, val text: String) : CreateActivityAction
    data class OnMarkCorrect(val index: Int) : CreateActivityAction
    data object OnSave : CreateActivityAction
    data object OnCancel : CreateActivityAction
}
