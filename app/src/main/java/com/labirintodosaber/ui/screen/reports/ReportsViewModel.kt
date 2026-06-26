package com.labirintodosaber.ui.screen.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.labirintodosaber.data.model.TaskNotebookSession
import com.labirintodosaber.data.remote.getOrNull
import com.labirintodosaber.data.repository.SessionRepository
import com.labirintodosaber.data.repository.StudentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

enum class ReportPeriod { LAST_3_MONTHS, LAST_6_MONTHS, ALL, CUSTOM }

data class ReportSessionPreview(
    val name: String,
    val date: String,
    val score: String,
)

data class ReportsUiState(
    val studentQuery: String = "",
    val selectedStudentName: String? = null,
    val selectedPeriod: ReportPeriod = ReportPeriod.LAST_3_MONTHS,
    val sessionPreviews: List<ReportSessionPreview> = emptyList(),
    val includeMetrics: Boolean = true,
    val includeQualitative: Boolean = true,
    val includeAnamnese: Boolean = false,
    val canExport: Boolean = false,
    val showDateRangePicker: Boolean = false,
    val customStartDateMs: Long? = null,
    val customEndDateMs: Long? = null,
    val isLoading: Boolean = false,
)

sealed interface ReportsAction {
    data class OnStudentQueryChange(val query: String) : ReportsAction
    data class OnPeriodSelect(val period: ReportPeriod) : ReportsAction
    data class OnToggleMetrics(val checked: Boolean) : ReportsAction
    data class OnToggleQualitative(val checked: Boolean) : ReportsAction
    data class OnToggleAnamnese(val checked: Boolean) : ReportsAction
    data object OnExportPdf : ReportsAction
    data object OnShowDatePicker : ReportsAction
    data object OnDismissDatePicker : ReportsAction
    data class OnDateRangeConfirmed(val startMs: Long?, val endMs: Long?) : ReportsAction
}

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val studentRepository: StudentRepository,
    private val sessionRepository: SessionRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()

    private var students: List<StudentRef> = emptyList()
    private var currentSessions: List<TaskNotebookSession> = emptyList()

    init {
        loadStudents()
    }

    fun onAction(action: ReportsAction) {
        when (action) {
            is ReportsAction.OnStudentQueryChange -> onQueryChange(action.query)
            is ReportsAction.OnPeriodSelect -> {
                val showPicker = action.period == ReportPeriod.CUSTOM
                _uiState.update { it.copy(selectedPeriod = action.period, showDateRangePicker = showPicker) }
                recomputePreviews()
            }
            is ReportsAction.OnToggleMetrics -> _uiState.update { it.copy(includeMetrics = action.checked) }
            is ReportsAction.OnToggleQualitative -> _uiState.update { it.copy(includeQualitative = action.checked) }
            is ReportsAction.OnToggleAnamnese -> _uiState.update { it.copy(includeAnamnese = action.checked) }
            ReportsAction.OnExportPdf -> Unit
            ReportsAction.OnShowDatePicker -> _uiState.update { it.copy(showDateRangePicker = true) }
            ReportsAction.OnDismissDatePicker -> _uiState.update { it.copy(showDateRangePicker = false) }
            is ReportsAction.OnDateRangeConfirmed -> {
                _uiState.update {
                    it.copy(
                        showDateRangePicker = false,
                        customStartDateMs = action.startMs,
                        customEndDateMs = action.endMs,
                    )
                }
                recomputePreviews()
            }
        }
    }

    private fun loadStudents() {
        viewModelScope.launch {
            students = studentRepository.list().getOrNull()
                ?.map { StudentRef(it.id, it.name) }
                .orEmpty()
        }
    }

    private fun onQueryChange(query: String) {
        _uiState.update { it.copy(studentQuery = query) }
        val match = if (query.isBlank()) null
        else students.firstOrNull { it.name.contains(query, ignoreCase = true) }

        if (match == null) {
            currentSessions = emptyList()
            _uiState.update { it.copy(selectedStudentName = null, sessionPreviews = emptyList(), canExport = false) }
            return
        }

        _uiState.update { it.copy(selectedStudentName = match.name, isLoading = true) }
        viewModelScope.launch {
            currentSessions = sessionRepository.listByStudent(match.id).getOrNull().orEmpty()
            _uiState.update { it.copy(isLoading = false) }
            recomputePreviews()
        }
    }

    private fun recomputePreviews() {
        val state = _uiState.value
        val now = Instant.now()
        val zone = ZoneId.systemDefault()

        val filtered = currentSessions.filter { session ->
            val started = session.startedAt.toInstantOrNull() ?: return@filter true
            when (state.selectedPeriod) {
                ReportPeriod.LAST_3_MONTHS -> started.isAfter(now.minusDaysCompat(90))
                ReportPeriod.LAST_6_MONTHS -> started.isAfter(now.minusDaysCompat(180))
                ReportPeriod.ALL -> true
                ReportPeriod.CUSTOM -> {
                    val from = state.customStartDateMs?.let { Instant.ofEpochMilli(it) }
                    val to = state.customEndDateMs?.let { Instant.ofEpochMilli(it) }
                    (from == null || !started.isBefore(from)) && (to == null || !started.isAfter(to))
                }
            }
        }

        val previews = filtered
            .sortedByDescending { it.startedAt }
            .map { session ->
                val correct = session.answers.count { it.isCorrect }
                ReportSessionPreview(
                    name = session.name,
                    date = session.startedAt.toInstantOrNull()?.atZone(zone)?.format(PREVIEW_DATE_FORMAT) ?: session.startedAt,
                    score = "$correct/${session.answers.size}",
                )
            }

        _uiState.update { it.copy(sessionPreviews = previews, canExport = previews.isNotEmpty()) }
    }
}

private data class StudentRef(val id: String, val name: String)

private fun Instant.minusDaysCompat(days: Long): Instant = this.minusSeconds(days * 24 * 60 * 60)

private fun String.toInstantOrNull(): Instant? =
    runCatching { Instant.parse(this) }.getOrNull()
        ?: runCatching { OffsetDateTime.parse(this).toInstant() }.getOrNull()

private val PREVIEW_DATE_FORMAT: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd 'de' MMM 'de' yyyy", Locale("pt", "BR"))
