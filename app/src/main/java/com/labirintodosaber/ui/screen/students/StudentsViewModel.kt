package com.labirintodosaber.ui.screen.students

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class StudentsViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(StudentsUiState())
    val uiState: StateFlow<StudentsUiState> = _uiState.asStateFlow()

    fun onAction(action: StudentsAction) {
        when (action) {
            is StudentsAction.OnSearchChange -> _uiState.update { it.copy(searchQuery = action.query) }
            is StudentsAction.OnStudentClick -> { /* navegação via callback */ }
        }
    }
}

data class StudentsUiState(
    val students: List<StudentItem> = defaultStudents(),
    val searchQuery: String = "",
)

data class StudentItem(
    val id: Int,
    val name: String,
    val age: Int,
    val gender: String,
    val level: String,
    val progressPercent: Int,
    val isGirl: Boolean,
    val avatarBorderColorHex: Long,
)

sealed interface StudentsAction {
    data class OnSearchChange(val query: String) : StudentsAction
    data class OnStudentClick(val studentId: Int) : StudentsAction
}

private fun defaultStudents() = listOf(
    StudentItem(1, "Ana Carolina Lima", 8, "Feminino", "Nível 1 - Inicial", 45, isGirl = true, 0xFFE94B8F),
    StudentItem(2, "Lara Julia Silva", 7, "Feminino", "Nível 2 - Desenvolvimento", 65, isGirl = true, 0xFF9B59B6),
    StudentItem(3, "João Pedro Souza", 9, "Masculino", "Nível 2 - Desenvolvimento", 72, isGirl = false, 0xFF4A90E2),
    StudentItem(4, "Lucas Martins", 6, "Masculino", "Nível 1 - Inicial", 30, isGirl = false, 0xFF50C878),
)
