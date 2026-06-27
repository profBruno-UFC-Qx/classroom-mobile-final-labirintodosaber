package com.labirintodosaber.ui.screen.reports

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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

private val MOCK_STUDENTS = listOf("Ana Carolina Lima", "Lara Julia Silva", "João Pedro Silva", "Maria Eduarda Santos")

private val MOCK_SESSIONS = listOf(
    ReportSessionPreview("Alfabetização - Reconhecimento de Letras", "05 de jan de 2026", "8/10"),
    ReportSessionPreview("Números e Quantidades - Contagem até 20", "12 de dez de 2025", "15/20"),
    ReportSessionPreview("Sons e Palavras - Formação de Sílabas", "28 de nov de 2025", "12/15"),
)

@HiltViewModel
class ReportsViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()

    fun onAction(action: ReportsAction) {
        when (action) {
            is ReportsAction.OnStudentQueryChange -> {
                val name = MOCK_STUDENTS.firstOrNull { it.contains(action.query, ignoreCase = true) }
                _uiState.update {
                    it.copy(
                        studentQuery = action.query,
                        selectedStudentName = if (action.query.isNotBlank()) name else null,
                        sessionPreviews = if (action.query.isNotBlank()) MOCK_SESSIONS else emptyList(),
                    )
                }
                updateCanExport()
            }
            is ReportsAction.OnPeriodSelect -> {
                val showPicker = action.period == ReportPeriod.CUSTOM
                _uiState.update { it.copy(selectedPeriod = action.period, showDateRangePicker = showPicker) }
                updateCanExport()
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
            }
        }
    }

    private fun updateCanExport() {
        _uiState.update { it.copy(canExport = it.sessionPreviews.isNotEmpty()) }
    }
}
