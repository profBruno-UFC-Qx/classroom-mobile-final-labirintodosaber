package com.labirintodosaber.ui.screen.groupdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.labirintodosaber.data.model.Task
import com.labirintodosaber.data.model.TaskCategory
import com.labirintodosaber.data.remote.getOrNull
import com.labirintodosaber.data.repository.TaskGroupRepository
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
class GroupDetailViewModel @Inject constructor(
    private val taskGroupRepository: TaskGroupRepository,
    private val taskRepository: TaskRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val groupId: String = checkNotNull(savedStateHandle["groupId"])

    private val _uiState = MutableStateFlow(GroupDetailUiState())
    val uiState: StateFlow<GroupDetailUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun onRetry() = load()

    private fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val group = taskGroupRepository.listByEducator().getOrNull()?.firstOrNull { it.id == groupId }
            if (group == null) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Grupo não encontrado.") }
                return@launch
            }

            val allTasks = taskRepository.list().getOrNull().orEmpty()
            val tasks = group.tasksIds.mapNotNull { id -> allTasks.firstOrNull { it.id == id } }
                .map { it.toRow() }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    name = group.name,
                    categoryLabel = group.category.displayName(),
                    iconColorHex = group.category.colorHex(),
                    tasks = tasks,
                )
            }
        }
    }
}

private fun Task.toRow() = GroupTaskRow(
    id = id,
    prompt = prompt,
    categoryLabel = category.displayName(),
    alternativeCount = alternatives.size,
    iconColorHex = category.colorHex(),
)

data class GroupTaskRow(
    val id: String,
    val prompt: String,
    val categoryLabel: String,
    val alternativeCount: Int,
    val iconColorHex: Long,
)

data class GroupDetailUiState(
    val name: String = "",
    val categoryLabel: String = "",
    val iconColorHex: Long = 0xFF5CC8C0,
    val tasks: List<GroupTaskRow> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)
