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
            DashboardAction.OnStartSession -> { /* TODO */ }
            DashboardAction.OnSeeAllSessionsClick -> { /* TODO */ }
            is DashboardAction.OnSessionClick -> { /* TODO */ }
            is DashboardAction.OnPastSessionClick -> { /* TODO */ }
            is DashboardAction.OnActivityClick -> { /* TODO */ }
        }
    }
}

enum class DashboardTab { HOME, ACTIVITIES, STUDENTS, REPORTS }

enum class ActivityIconType { BOOK, CALCULATE, EDIT }

data class DashboardUiState(
    val userName: String = "Dra. Ana Paula",
    val todaySessionCount: Int = 5,
    val todaySessions: List<SessionItem> = defaultSessions(),
    val pastSessions: List<PastSessionItem> = defaultPastSessions(),
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

data class PastSessionItem(
    val id: Int,
    val studentName: String,
    val date: String,
    val time: String,
    val duration: String,
    val category: String,
    val hitRatePercent: Int,
    val isGirl: Boolean,
)

data class ActivityItem(
    val id: Int,
    val name: String,
    val description: String,
    val backgroundColorHex: Long,
    val iconColorHex: Long,
    val iconType: ActivityIconType,
    val tags: List<ActivityTag>,
)

data class ActivityTag(val label: String)

sealed interface DashboardAction {
    data object OnStartSession : DashboardAction
    data object OnSeeAllSessionsClick : DashboardAction
    data class OnTabSelect(val tab: DashboardTab) : DashboardAction
    data class OnSessionClick(val sessionId: Int) : DashboardAction
    data class OnPastSessionClick(val sessionId: Int) : DashboardAction
    data class OnActivityClick(val activityId: Int) : DashboardAction
}

private fun defaultSessions() = listOf(
    SessionItem(1, "João Pedro Silva", "09:00", "Alfabetização",
        "Atividades de reconhecimento de letras e sílabas", isGirl = false, 0xFF4A90E2),
    SessionItem(2, "Lucas Oliveira", "14:00", "Sons e Palavras",
        "Formação de sílabas e palavras simples", isGirl = false, 0xFF50C878),
    SessionItem(3, "Maria Fernanda", "10:30", "Vocabulário",
        "Ampliação do repertório de palavras", isGirl = true, 0xFFE94B8F),
)

private fun defaultPastSessions() = listOf(
    PastSessionItem(1, "João Pedro Silva", "08/04/2026", "09:00", "45min", "Alfabetização", 85, isGirl = false),
    PastSessionItem(2, "Maria Eduarda Santos", "07/04/2026", "14:30", "50min", "Matemática", 92, isGirl = true),
)

private fun defaultActivities() = listOf(
    ActivityItem(
        id = 1,
        name = "Alfabetização Divertida",
        description = "Atividades de reconhecimento de letras e sílabas",
        backgroundColorHex = 0xFFD8F5F3,
        iconColorHex = 0xFF5CC8C0,
        iconType = ActivityIconType.BOOK,
        tags = listOf(ActivityTag("Leitura"), ActivityTag("Vocabulário")),
    ),
    ActivityItem(
        id = 2,
        name = "Números e Quantidade",
        description = "Exercícios de contagem e operações básicas",
        backgroundColorHex = 0xFFFFE8F0,
        iconColorHex = 0xFFE94B8F,
        iconType = ActivityIconType.CALCULATE,
        tags = listOf(ActivityTag("Matemática"), ActivityTag("Contagem")),
    ),
    ActivityItem(
        id = 3,
        name = "Sons e Palavras",
        description = "Formação de sílabas e palavras simples",
        backgroundColorHex = 0xFFFFF6D8,
        iconColorHex = 0xFFC9A020,
        iconType = ActivityIconType.EDIT,
        tags = listOf(ActivityTag("Escrita"), ActivityTag("Compreensão")),
    ),
)
