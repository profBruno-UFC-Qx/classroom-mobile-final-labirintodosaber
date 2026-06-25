package com.labirintodosaber.ui.screen.students

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
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class StudentsViewModel @Inject constructor(
    private val studentRepository: StudentRepository,
    private val sessionRepository: SessionRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudentsUiState())
    val uiState: StateFlow<StudentsUiState> = _uiState.asStateFlow()

    fun onAction(action: StudentsAction) {
        when (action) {
            is StudentsAction.OnSearchChange -> _uiState.update { it.copy(searchQuery = action.query) }
            StudentsAction.OnRetry -> loadStudents()
            is StudentsAction.OnStudentClick -> { /* navegação via callback */ }
        }
    }

    /** Recarrega a lista. Chamado ao entrar e sempre que a tela é retomada (volta de cadastro). */
    fun refresh() = loadStudents()

    private fun loadStudents() {
        viewModelScope.launch {
            val isFirstLoad = _uiState.value.students.isEmpty()
            if (isFirstLoad) _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = studentRepository.list()) {
                is ApiResult.Success -> {
                    val items = mapWithProgress(result.data)
                    _uiState.update { it.copy(isLoading = false, students = items, errorMessage = null) }
                }
                is ApiResult.Error -> _uiState.update {
                    // Em refresh silencioso (já há dados), mantém a lista atual em vez de mostrar erro.
                    if (isFirstLoad) it.copy(isLoading = false, errorMessage = result.message)
                    else it.copy(isLoading = false)
                }
            }
        }
    }

    /** Mapeia cada aluno e busca a acurácia agregada (análise) para preencher o progresso. */
    private suspend fun mapWithProgress(students: List<Student>): List<StudentItem> = coroutineScope {
        students.mapIndexed { index, student ->
            async {
                val accuracy = sessionRepository.analysis(student.id).getOrNull()?.total?.accuracy ?: 0.0
                val percent = (accuracy * 100).roundToInt()
                student.toItem(percent, AVATAR_PALETTE[index % AVATAR_PALETTE.size])
            }
        }.awaitAll()
    }
}

private val AVATAR_PALETTE = listOf(0xFFE94B8F, 0xFF9B59B6, 0xFF4A90E2, 0xFF50C878, 0xFFF39C12)

private fun Student.toItem(progressPercent: Int, colorHex: Long) = StudentItem(
    id = id,
    name = name,
    age = age,
    gender = if (gender == Gender.FEMALE) "Feminino" else "Masculino",
    level = levelFor(progressPercent),
    progressPercent = progressPercent,
    isGirl = gender == Gender.FEMALE,
    avatarBorderColorHex = colorHex,
    photoUrl = photoUrl?.takeIf { it.isNotBlank() },
)

private fun levelFor(percent: Int): String = when {
    percent < 40 -> "Nível 1 - Inicial"
    percent < 70 -> "Nível 2 - Desenvolvimento"
    else -> "Nível 3 - Avançado"
}

data class StudentsUiState(
    val students: List<StudentItem> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

data class StudentItem(
    val id: String,
    val name: String,
    val age: Int,
    val gender: String,
    val level: String,
    val progressPercent: Int,
    val isGirl: Boolean,
    val avatarBorderColorHex: Long,
    val photoUrl: String? = null,
)

sealed interface StudentsAction {
    data class OnSearchChange(val query: String) : StudentsAction
    data class OnStudentClick(val studentId: String) : StudentsAction
    data object OnRetry : StudentsAction
}
