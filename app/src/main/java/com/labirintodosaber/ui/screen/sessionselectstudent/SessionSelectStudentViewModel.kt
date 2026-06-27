package com.labirintodosaber.ui.screen.sessionselectstudent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.ceil

data class SessionStudentItem(
    val id: String,
    val name: String,
    val age: Int,
    val gender: String,
    val level: String,
    val isGirl: Boolean,
)

data class SessionSelectStudentUiState(
    val query: String = "",
    val allStudents: List<SessionStudentItem> = emptyList(),
    val currentPage: Int = 0,
    val totalPages: Int = 1,
    val selectedStudentId: String? = null,
    val canProceed: Boolean = false,
)

val SessionSelectStudentUiState.visibleStudents: List<SessionStudentItem>
    get() {
        val from = currentPage * PAGE_SIZE
        val to = minOf(from + PAGE_SIZE, allStudents.size)
        return if (from >= allStudents.size) emptyList() else allStudents.subList(from, to)
    }

sealed interface SessionSelectStudentAction {
    data class OnQueryChange(val query: String) : SessionSelectStudentAction
    data class OnStudentSelect(val studentId: String) : SessionSelectStudentAction
    data class OnPageChange(val page: Int) : SessionSelectStudentAction
    data object OnNextStep : SessionSelectStudentAction
    data object OnBack : SessionSelectStudentAction
}

sealed interface SessionSelectStudentEvent {
    data object NavigateBack : SessionSelectStudentEvent
    data class NavigateToConfigure(val studentId: String) : SessionSelectStudentEvent
}

private const val PAGE_SIZE = 3

private val MOCK_STUDENTS = listOf(
    SessionStudentItem(id = "s1", name = "Ana Carolina Lima",   age = 8,  gender = "Feminino",  level = "Nível 1 - Inicial",        isGirl = true),
    SessionStudentItem(id = "s2", name = "Lara Julia Silva",    age = 7,  gender = "Feminino",  level = "Nível 2 - Desenvolvimento", isGirl = true),
    SessionStudentItem(id = "s3", name = "Pedro Henrique",      age = 9,  gender = "Masculino", level = "Nível 1 - Inicial",        isGirl = false),
    SessionStudentItem(id = "s4", name = "Maria Fernanda",      age = 8,  gender = "Feminino",  level = "Nível 3 - Avançado",       isGirl = true),
    SessionStudentItem(id = "s5", name = "João Victor",         age = 10, gender = "Masculino", level = "Nível 2 - Desenvolvimento", isGirl = false),
    SessionStudentItem(id = "s6", name = "Sofia Oliveira",      age = 7,  gender = "Feminino",  level = "Nível 1 - Inicial",        isGirl = true),
    SessionStudentItem(id = "s7", name = "Lucas Martins",       age = 9,  gender = "Masculino", level = "Nível 3 - Avançado",       isGirl = false),
    SessionStudentItem(id = "s8", name = "Isabela Costa",       age = 8,  gender = "Feminino",  level = "Nível 2 - Desenvolvimento", isGirl = true),
)

@HiltViewModel
class SessionSelectStudentViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(SessionSelectStudentUiState())
    val uiState: StateFlow<SessionSelectStudentUiState> = _uiState.asStateFlow()

    private val _events = Channel<SessionSelectStudentEvent>(Channel.BUFFERED)
    val events: Flow<SessionSelectStudentEvent> = _events.receiveAsFlow()

    init {
        loadStudents()
    }

    fun onAction(action: SessionSelectStudentAction) {
        when (action) {
            is SessionSelectStudentAction.OnQueryChange -> filterStudents(action.query)
            is SessionSelectStudentAction.OnStudentSelect -> selectStudent(action.studentId)
            is SessionSelectStudentAction.OnPageChange -> _uiState.update { it.copy(currentPage = action.page) }
            SessionSelectStudentAction.OnNextStep -> proceedToNextStep()
            SessionSelectStudentAction.OnBack -> viewModelScope.launch {
                _events.send(SessionSelectStudentEvent.NavigateBack)
            }
        }
    }

    private fun loadStudents() {
        val total = ceil(MOCK_STUDENTS.size / PAGE_SIZE.toDouble()).toInt()
        _uiState.update { it.copy(allStudents = MOCK_STUDENTS, totalPages = maxOf(1, total)) }
    }

    private fun filterStudents(query: String) {
        val filtered = if (query.isBlank()) MOCK_STUDENTS
        else MOCK_STUDENTS.filter { it.name.contains(query, ignoreCase = true) }
        val total = ceil(filtered.size / PAGE_SIZE.toDouble()).toInt()
        _uiState.update {
            it.copy(query = query, allStudents = filtered, currentPage = 0, totalPages = maxOf(1, total))
        }
    }

    private fun selectStudent(id: String) {
        val alreadySelected = _uiState.value.selectedStudentId == id
        val newSelection = if (alreadySelected) null else id
        _uiState.update { it.copy(selectedStudentId = newSelection, canProceed = newSelection != null) }
    }

    private fun proceedToNextStep() {
        val studentId = _uiState.value.selectedStudentId ?: return
        viewModelScope.launch { _events.send(SessionSelectStudentEvent.NavigateToConfigure(studentId)) }
    }
}
