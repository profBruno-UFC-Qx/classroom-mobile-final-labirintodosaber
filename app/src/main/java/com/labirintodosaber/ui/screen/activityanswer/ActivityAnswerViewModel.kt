package com.labirintodosaber.ui.screen.activityanswer

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.labirintodosaber.data.remote.ApiResult
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
class ActivityAnswerViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val taskId: String = checkNotNull(savedStateHandle["taskId"])

    private val _uiState = MutableStateFlow(ActivityAnswerUiState())
    val uiState: StateFlow<ActivityAnswerUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun onRetry() = load()

    private fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = taskRepository.getById(taskId)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(
                        isLoading = false,
                        prompt = result.data.prompt,
                        categoryLabel = result.data.category.displayName(),
                        alternatives = result.data.alternatives.map { alt ->
                            AnswerAlternative(text = alt.text, isCorrect = alt.isCorrect)
                        },
                    )
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = result.message)
                }
            }
        }
    }
}

data class AnswerAlternative(
    val text: String,
    val isCorrect: Boolean,
)

data class ActivityAnswerUiState(
    val prompt: String = "",
    val categoryLabel: String = "",
    val alternatives: List<AnswerAlternative> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)
