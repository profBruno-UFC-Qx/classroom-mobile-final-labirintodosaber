package com.labirintodosaber.ui.screen.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.labirintodosaber.data.model.Gender
import com.labirintodosaber.data.model.Student
import com.labirintodosaber.data.model.Task
import com.labirintodosaber.data.model.TaskCategory
import com.labirintodosaber.data.model.TaskNotebookSession
import com.labirintodosaber.data.remote.getOrNull
import com.labirintodosaber.data.repository.AuthRepository
import com.labirintodosaber.data.repository.SessionRepository
import com.labirintodosaber.data.repository.StudentRepository
import com.labirintodosaber.data.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val studentRepository: StudentRepository,
    private val sessionRepository: SessionRepository,
    private val taskRepository: TaskRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        load()
    }

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

    private fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val userName = authRepository.me().getOrNull()?.name ?: ""
            val students = studentRepository.list().getOrNull().orEmpty()

            // Sessões de cada aluno (uma chamada por aluno, em paralelo), como na Home web.
            val sessionsByStudent = coroutineScope {
                students.map { student ->
                    async {
                        student to sessionRepository.listByStudent(student.id).getOrNull().orEmpty()
                    }
                }.awaitAll()
            }

            val allSessions = sessionsByStudent.flatMap { (student, sessions) ->
                sessions.map { it to student }
            }

            val today = LocalDate.now()
            val todaySessions = allSessions
                .filter { (session, _) -> session.startedAt.toLocalDate() == today }
                .sortedBy { (session, _) -> session.startedAt }
                .mapIndexed { index, (session, student) -> session.toTodayItem(student, index) }

            val pastSessions = allSessions
                .sortedByDescending { (session, _) -> session.startedAt }
                .take(5)
                .map { (session, student) -> session.toPastItem(student) }

            val activities = taskRepository.list().getOrNull().orEmpty()
                .take(3)
                .map { it.toActivityItem() }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    userName = userName,
                    todaySessionCount = todaySessions.size,
                    todaySessions = todaySessions,
                    pastSessions = pastSessions,
                    recentActivities = activities,
                )
            }
        }
    }
}

// ── Mapeamento domínio → UI ────────────────────────────────────────────────────

private val AVATAR_PALETTE = listOf(0xFF4A90E2, 0xFF50C878, 0xFFE94B8F, 0xFF9B59B6, 0xFFF39C12)

private fun TaskNotebookSession.toTodayItem(student: Student, index: Int) = SessionItem(
    id = id,
    studentName = student.name,
    time = startedAt.formatTime(),
    category = name,
    description = "${answers.size} ${if (answers.size == 1) "questão" else "questões"} respondidas",
    isGirl = student.gender == Gender.FEMALE,
    borderColorHex = AVATAR_PALETTE[index % AVATAR_PALETTE.size],
)

private fun TaskNotebookSession.toPastItem(student: Student) = PastSessionItem(
    id = id,
    studentName = student.name,
    date = startedAt.formatDate(),
    time = startedAt.formatTime(),
    duration = durationLabel(),
    category = name,
    hitRatePercent = hitRate(),
    isGirl = student.gender == Gender.FEMALE,
)

private fun TaskNotebookSession.hitRate(): Int {
    if (answers.isEmpty()) return 0
    val correct = answers.count { it.isCorrect }
    return (correct * 100) / answers.size
}

private fun TaskNotebookSession.durationLabel(): String {
    val start = parseDateTime(startedAt) ?: return "—"
    val end = finishedAt?.let { parseDateTime(it) } ?: return "Em andamento"
    val minutes = Duration.between(start, end).toMinutes()
    return "${minutes}min"
}

private fun Task.toActivityItem(): ActivityItem {
    val style = category.activityStyle()
    return ActivityItem(
        id = id,
        name = prompt,
        description = if (type == com.labirintodosaber.data.model.TaskType.MULTIPLE_CHOICE_WITH_MEDIA) {
            "Múltipla escolha com mídia"
        } else {
            "Múltipla escolha"
        },
        backgroundColorHex = style.background,
        iconColorHex = style.icon,
        iconType = style.iconType,
        tags = listOf(ActivityTag(category.label())),
    )
}

private data class ActivityStyle(val background: Long, val icon: Long, val iconType: ActivityIconType)

private fun TaskCategory.activityStyle(): ActivityStyle = when (this) {
    TaskCategory.READING -> ActivityStyle(0xFFD8F5F3, 0xFF5CC8C0, ActivityIconType.BOOK)
    TaskCategory.WRITING -> ActivityStyle(0xFFFFF6D8, 0xFFC9A020, ActivityIconType.EDIT)
    TaskCategory.VOCABULARY -> ActivityStyle(0xFFFFE8F0, 0xFFE94B8F, ActivityIconType.BOOK)
    TaskCategory.COMPREHENSION -> ActivityStyle(0xFFE8EEFF, 0xFF4A90E2, ActivityIconType.CALCULATE)
}

private fun TaskCategory.label(): String = when (this) {
    TaskCategory.READING -> "Leitura"
    TaskCategory.WRITING -> "Escrita"
    TaskCategory.VOCABULARY -> "Vocabulário"
    TaskCategory.COMPREHENSION -> "Compreensão"
}

// ── Parsing de datas ISO (a API devolve ISO 8601) ──────────────────────────────

private val TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm")
private val DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy")

private fun parseDateTime(iso: String): Instant? =
    runCatching { Instant.parse(iso) }.getOrNull()
        ?: runCatching { OffsetDateTime.parse(iso).toInstant() }.getOrNull()

private fun String.toLocalDate(): LocalDate? =
    parseDateTime(this)?.atZone(ZoneId.systemDefault())?.toLocalDate()

private fun String.formatTime(): String =
    parseDateTime(this)?.atZone(ZoneId.systemDefault())?.format(TIME_FORMAT) ?: ""

private fun String.formatDate(): String =
    parseDateTime(this)?.atZone(ZoneId.systemDefault())?.format(DATE_FORMAT) ?: ""

enum class DashboardTab { HOME, ACTIVITIES, STUDENTS, REPORTS }

enum class ActivityIconType { BOOK, CALCULATE, EDIT }

data class DashboardUiState(
    val userName: String = "",
    val todaySessionCount: Int = 0,
    val todaySessions: List<SessionItem> = emptyList(),
    val pastSessions: List<PastSessionItem> = emptyList(),
    val recentActivities: List<ActivityItem> = emptyList(),
    val selectedTab: DashboardTab = DashboardTab.HOME,
    val isLoading: Boolean = false,
)

data class SessionItem(
    val id: String,
    val studentName: String,
    val time: String,
    val category: String,
    val description: String,
    val isGirl: Boolean,
    val borderColorHex: Long,
)

data class PastSessionItem(
    val id: String,
    val studentName: String,
    val date: String,
    val time: String,
    val duration: String,
    val category: String,
    val hitRatePercent: Int,
    val isGirl: Boolean,
)

data class ActivityItem(
    val id: String,
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
    data class OnSessionClick(val sessionId: String) : DashboardAction
    data class OnPastSessionClick(val sessionId: String) : DashboardAction
    data class OnActivityClick(val activityId: String) : DashboardAction
}
