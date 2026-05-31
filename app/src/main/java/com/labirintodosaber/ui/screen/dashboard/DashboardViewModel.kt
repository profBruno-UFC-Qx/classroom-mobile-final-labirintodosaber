package com.labirintodosaber.ui.screen.dashboard

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    fun onAction(action: DashboardAction) {
        when (action) {
            is DashboardAction.OnTabSelect -> _uiState.update { it.copy(selectedTab = action.tab) }
            DashboardAction.OnStartSession -> { /* TODO: iniciar sessão */ }
            is DashboardAction.OnSessionClick -> { /* TODO: abrir sessão */ }
            is DashboardAction.OnActivityClick -> { /* TODO: abrir atividade */ }
        }
    }
}

enum class DashboardTab { HOME, ACTIVITIES, STUDENTS, REPORTS }

enum class ActivityIconType { BOOK, CALCULATE, EDIT }

data class DashboardUiState(
    val userName: String = "Dra. Ana Paula",
    val recentSessions: List<SessionItem> = defaultSessions(),
    val recentActivities: List<ActivityItem> = defaultActivities(),
    val selectedTab: DashboardTab = DashboardTab.HOME,
)

data class SessionItem(
    val id: Int,
    val studentName: String,
    val time: String,
    val category: String,
    val description: String,
    val isGirl: Boolean,
    val borderColorHex: Long,
)

data class ActivityItem(
    val id: Int,
    val name: String,
    val description: String,
    val backgroundColorHex: Long,
    val iconType: ActivityIconType,
    val tags: List<ActivityTag>,
)

data class ActivityTag(val label: String, val colorHex: Long)

sealed interface DashboardAction {
    data object OnStartSession : DashboardAction
    data class OnTabSelect(val tab: DashboardTab) : DashboardAction
    data class OnSessionClick(val sessionId: Int) : DashboardAction
    data class OnActivityClick(val activityId: Int) : DashboardAction
}

private fun defaultSessions() = listOf(
    SessionItem(1, "João Pedro Silva", "09:00", "Alfabetização",
        "Atividades de reconhecimento de letras e sílabas", isGirl = false, 0xFF4A90D9),
    SessionItem(2, "Lucas Carvalho", "14:00", "Sons e Palavras",
        "Formação de sílabas e palavras simples", isGirl = true, 0xFF4CAF79),
    SessionItem(3, "Maria Fernanda", "10:30", "Vocabulário",
        "Ampliação do repertório de palavras", isGirl = true, 0xFFF06292),
)

private fun defaultActivities() = listOf(
    ActivityItem(
        id = 1,
        name = "Alfabetização Divertida",
        description = "Atividades de reconhecimento de letras e sílabas",
        backgroundColorHex = 0xFFE8F7F5,
        iconType = ActivityIconType.BOOK,
        tags = listOf(ActivityTag("Leitura", 0xFF5CC8C0), ActivityTag("Vocabulário", 0xFF5CC8C0)),
    ),
    ActivityItem(
        id = 2,
        name = "Números e Quantidade",
        description = "Exercícios de contagem e operações básicas",
        backgroundColorHex = 0xFFFCEEF5,
        iconType = ActivityIconType.CALCULATE,
        tags = listOf(ActivityTag("Matemática", 0xFFD05090), ActivityTag("Contagem", 0xFFD05090)),
    ),
    ActivityItem(
        id = 3,
        name = "Sons e Palavras",
        description = "Formação de sílabas e palavras simples",
        backgroundColorHex = 0xFFFFF8E8,
        iconType = ActivityIconType.EDIT,
        tags = listOf(ActivityTag("Escrita", 0xFFC9A020), ActivityTag("Compreensão", 0xFFC9A020)),
    ),
)
