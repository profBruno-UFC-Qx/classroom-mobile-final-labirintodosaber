package com.labirintodosaber.ui.screen.studentprofile

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class StudentProfileViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(StudentProfileUiState())
    val uiState: StateFlow<StudentProfileUiState> = _uiState.asStateFlow()

    fun onAction(action: StudentProfileAction) {
        when (action) {
            is StudentProfileAction.OnTabSelect -> _uiState.update { it.copy(selectedTab = action.tab) }
            StudentProfileAction.OnGenerateReportClick -> { /* TODO */ }
            StudentProfileAction.OnEditClick -> { /* TODO */ }
        }
    }
}

enum class StudentProfileTab { PROGRESS, SESSIONS, DOCUMENTS, ANAMNESE }

data class CategoryProgress(val name: String, val percent: Int)

data class StudentProfileUiState(
    val name: String = "Ana Carolina Lima",
    val age: Int = 8,
    val gender: String = "Feminino",
    val birthDate: String = "14/03/2020",
    val address: String = "Rua das Flores, 123 - Centro, São Paulo - SP",
    val objective: String = "Desenvolver habilidades de leitura e escrita, melhorar vocabulário",
    val isGirl: Boolean = true,
    val avatarBorderColorHex: Long = 0xFFE94B8F,
    val progressPercent: Int = 45,
    val categoryProgress: List<CategoryProgress> = defaultCategoryProgress(),
    val selectedTab: StudentProfileTab = StudentProfileTab.PROGRESS,
)

sealed interface StudentProfileAction {
    data class OnTabSelect(val tab: StudentProfileTab) : StudentProfileAction
    data object OnGenerateReportClick : StudentProfileAction
    data object OnEditClick : StudentProfileAction
}

private fun defaultCategoryProgress() = listOf(
    CategoryProgress("Leitura", 55),
    CategoryProgress("Escrita", 45),
    CategoryProgress("Vocabulário", 60),
    CategoryProgress("Compreensão", 50),
)
