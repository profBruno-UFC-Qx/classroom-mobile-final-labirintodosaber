package com.labirintodosaber.ui.screen.sessionselectstudent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.labirintodosaber.data.model.Gender
import com.labirintodosaber.data.model.Student
import com.labirintodosaber.data.remote.ApiResult
import com.labirintodosaber.data.remote.getOrNull
import com.labirintodosaber.data.repository.SessionRepository
import com.labirintodosaber.data.repository.StudentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.ceil
import kotlin.math.roundToInt

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
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
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
    data object OnRetry : SessionSelectStudentAction
}

sealed interface SessionSelectStudentEvent {
    data object NavigateBack : SessionSelectStudentEvent
    data class NavigateToConfigure(val studentId: String) : SessionSelectStudentEvent
}

private const val PAGE_SIZE = 3

@HiltViewModel
class SessionSelectStudentViewModel @Inject constructor(
    private val studentRepository: StudentRepository,
    private val sessionRepository: SessionRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SessionSelectStudentUiState())
    val uiState: StateFlow<SessionSelectStudentUiState> = _uiState.asStateFlow()

    private val _events = Channel<SessionSelectStudentEvent>(Channel.BUFFERED)
    val events: Flow<SessionSelectStudentEvent> = _events.receiveAsFlow()

    /** Lista completa carregada da API; o filtro de busca opera sobre ela. */
    private var loadedStudents: List<SessionStudentItem> = emptyList()

    init {
        loadStudents()
    }

    fun onAction(action: SessionSelectStudentAction) {
        when (action) {
            is SessionSelectStudentAction.OnQueryChange -> filterStudents(action.query)
            is SessionSelectStudentAction.OnStudentSelect -> selectStudent(action.studentId)
            is SessionSelectStudentAction.OnPageChange -> _uiState.update { it.copy(currentPage = action.page) }
            SessionSelectStudentAction.OnNextStep -> proceedToNextStep()
            SessionSelectStudentAction.OnRetry -> loadStudents()
            SessionSelectStudentAction.OnBack -> viewModelScope.launch {
                _events.send(SessionSelectStudentEvent.NavigateBack)
            }
        }
    }

    private fun loadStudents() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = studentRepository.list()) {
                is ApiResult.Success -> {
                    loadedStudents = mapWithLevel(result.data)
                    applyQuery(_uiState.value.query)
                    _uiState.update { it.copy(isLoading = false) }
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = result.message)
                }
            }
        }
    }

    private suspend fun mapWithLevel(students: List<Student>): List<SessionStudentItem> = coroutineScope {
        students.map { student ->
            async {
                val accuracy = sessionRepository.analysis(student.id).getOrNull()?.total?.accuracy ?: 0.0
                student.toItem((accuracy * 100).roundToInt())
            }
        }.awaitAll()
    }

    private fun filterStudents(query: String) {
        _uiState.update { it.copy(query = query) }
        applyQuery(query)
    }

    private fun applyQuery(query: String) {
        val filtered = if (query.isBlank()) loadedStudents
        else loadedStudents.filter { it.name.contains(query, ignoreCase = true) }
        val total = ceil(filtered.size / PAGE_SIZE.toDouble()).toInt()
        _uiState.update { it.copy(allStudents = filtered, currentPage = 0, totalPages = maxOf(1, total)) }
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

private fun Student.toItem(progressPercent: Int) = SessionStudentItem(
    id = id,
    name = name,
    age = age,
    gender = if (gender == Gender.FEMALE) "Feminino" else "Masculino",
    level = levelFor(progressPercent),
    isGirl = gender == Gender.FEMALE,
)

private fun levelFor(percent: Int): String = when {
    percent < 40 -> "Nível 1 - Inicial"
    percent < 70 -> "Nível 2 - Desenvolvimento"
    else -> "Nível 3 - Avançado"
}
