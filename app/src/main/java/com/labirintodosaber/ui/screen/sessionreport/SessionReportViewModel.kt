package com.labirintodosaber.ui.screen.sessionreport

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.labirintodosaber.data.model.SessionReport
import com.labirintodosaber.data.remote.ApiResult
import com.labirintodosaber.data.remote.getOrNull
import com.labirintodosaber.data.repository.AuthRepository
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
import javax.inject.Inject
import kotlin.math.roundToInt

data class CategoryAccuracyItem(
    val label: String,
    val percent: Int,
    val colorHex: Long,
)

data class ActivityTypeAccuracyItem(
    val label: String,
    val count: Int,
    val percent: Int,
)

data class SessionReportUiState(
    val studentName: String = "",
    val date: String = "",
    val professional: String = "",
    val totalQuestions: Int = 0,
    val totalTime: String = "00:00",
    val avgResponseTime: String = "00:00",
    val avgCorrectTime: String = "00:00",
    val avgWrongTime: String = "00:00",
    val categoryAccuracy: List<CategoryAccuracyItem> = emptyList(),
    val typeAccuracy: List<ActivityTypeAccuracyItem> = emptyList(),
    val descriptiveReport: String = "",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val savedMessage: String? = null,
)

sealed interface SessionReportAction {
    data class OnDescriptiveReportChange(val text: String) : SessionReportAction
    data object OnSaveReport : SessionReportAction
    data object OnRetry : SessionReportAction
}

@HiltViewModel
class SessionReportViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val authRepository: AuthRepository,
    private val studentRepository: StudentRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val sessionId: String = checkNotNull(savedStateHandle["sessionId"])
    private val studentId: String = checkNotNull(savedStateHandle["studentId"])

    private val _uiState = MutableStateFlow(SessionReportUiState())
    val uiState: StateFlow<SessionReportUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun onAction(action: SessionReportAction) {
        when (action) {
            is SessionReportAction.OnDescriptiveReportChange ->
                _uiState.update { it.copy(descriptiveReport = action.text, savedMessage = null) }
            SessionReportAction.OnSaveReport -> saveReport()
            SessionReportAction.OnRetry -> load()
        }
    }

    private fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            when (val result = sessionRepository.report(sessionId)) {
                is ApiResult.Success -> {
                    val report = result.data
                    val professional = authRepository.me().getOrNull()?.name.orEmpty()
                    val student = studentRepository.list().getOrNull()?.firstOrNull { it.id == studentId }
                    val sessionDate = sessionRepository.listByStudent(studentId).getOrNull()
                        ?.firstOrNull { it.id == sessionId }?.startedAt?.formatDate().orEmpty()

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            studentName = student?.name.orEmpty(),
                            professional = professional,
                            date = sessionDate,
                            totalQuestions = report.totalQuestions,
                            totalTime = (report.totalTimeSession ?: 0.0).toClock(),
                            avgResponseTime = report.averageTimePerQuestion.toClock(),
                            avgCorrectTime = (report.averageCorrectTime ?: 0.0).toClock(),
                            avgWrongTime = (report.averageIncorrectTime ?: 0.0).toClock(),
                            categoryAccuracy = report.toCategoryItems(),
                            typeAccuracy = report.toTypeItems(),
                            descriptiveReport = report.observation.orEmpty(),
                        )
                    }
                }
                is ApiResult.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
            }
        }
    }

    private fun saveReport() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, savedMessage = null, errorMessage = null) }
            when (val result = sessionRepository.addObservation(sessionId, _uiState.value.descriptiveReport)) {
                is ApiResult.Success -> _uiState.update { it.copy(isSaving = false, savedMessage = "Relatório salvo.") }
                is ApiResult.Error -> _uiState.update { it.copy(isSaving = false, errorMessage = result.message) }
            }
        }
    }
}

// ── Mapeamento ──────────────────────────────────────────────────────────────────

private fun SessionReport.toCategoryItems(): List<CategoryAccuracyItem> =
    percentageByCategory.entries.map { (key, value) ->
        CategoryAccuracyItem(
            label = categoryLabel(key),
            percent = value.toPercent(),
            colorHex = categoryColor(key),
        )
    }

private fun SessionReport.toTypeItems(): List<ActivityTypeAccuracyItem> =
    percentageByType.entries.map { (key, value) ->
        ActivityTypeAccuracyItem(
            label = typeLabel(key),
            count = 0,
            percent = value.toPercent(),
        )
    }

/** Aceita tanto fração (0..1) quanto percentual (0..100). */
private fun Double?.toPercent(): Int {
    val v = this ?: return 0
    return (if (v <= 1.0) v * 100 else v).roundToInt()
}

/** Segundos (Double) → "mm:ss". */
private fun Double.toClock(): String {
    val totalSeconds = this.roundToInt().coerceAtLeast(0)
    return "%02d:%02d".format(totalSeconds / 60, totalSeconds % 60)
}

private fun categoryLabel(wire: String): String = when (wire.lowercase()) {
    "reading" -> "Leitura"
    "writing" -> "Escrita"
    "vocabulary" -> "Vocabulário"
    "comprehension" -> "Compreensão"
    else -> wire.replaceFirstChar { it.uppercase() }
}

private fun categoryColor(wire: String): Long = when (wire.lowercase()) {
    "reading" -> 0xFF2563EB
    "writing" -> 0xFFEA580C
    "vocabulary" -> 0xFF7C3AED
    "comprehension" -> 0xFF16A34A
    else -> 0xFF5CC8C0
}

private fun typeLabel(wire: String): String = when (wire) {
    "multipleChoice" -> "Múltipla Escolha"
    "multipleChoiceWithMedia" -> "Com Mídia"
    else -> wire.replaceFirstChar { it.uppercase() }
}

private val DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy")

private fun String.formatDate(): String {
    val instant = runCatching { Instant.parse(this) }.getOrNull()
        ?: runCatching { OffsetDateTime.parse(this).toInstant() }.getOrNull()
        ?: return this
    return instant.atZone(ZoneId.systemDefault()).format(DATE_FORMAT)
}
