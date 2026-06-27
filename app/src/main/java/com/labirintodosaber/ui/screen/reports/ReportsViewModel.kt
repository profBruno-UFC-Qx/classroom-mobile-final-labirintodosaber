package com.labirintodosaber.ui.screen.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.labirintodosaber.data.model.AnamneseQuestion
import com.labirintodosaber.data.model.AnamneseResponse
import com.labirintodosaber.data.model.AnamneseTemplate
import com.labirintodosaber.data.model.StudentAnalysis
import com.labirintodosaber.data.model.TaskCategory
import com.labirintodosaber.data.model.TaskNotebookSession
import com.labirintodosaber.data.pdf.ReportPdfAnamnese
import com.labirintodosaber.data.pdf.ReportPdfCategory
import com.labirintodosaber.data.pdf.ReportPdfData
import com.labirintodosaber.data.pdf.ReportPdfGenerator
import com.labirintodosaber.data.pdf.ReportPdfObservation
import com.labirintodosaber.data.pdf.ReportPdfQa
import com.labirintodosaber.data.pdf.ReportPdfSession
import com.labirintodosaber.data.remote.ApiResult
import com.labirintodosaber.data.remote.getOrNull
import com.labirintodosaber.data.repository.AnamneseRepository
import com.labirintodosaber.data.repository.SessionRepository
import com.labirintodosaber.data.repository.StudentRepository
import com.labirintodosaber.ui.screen.activities.displayName
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

data class ReportStudentOption(
    val id: String,
    val name: String,
)

data class ReportsUiState(
    val studentQuery: String = "",
    val studentResults: List<ReportStudentOption> = emptyList(),
    val selectedStudentId: String? = null,
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
    val isExporting: Boolean = false,
    val exportError: String? = null,
    val showNoSessions: Boolean = false,
    val generatedPdfPath: String? = null,
)

sealed interface ReportsAction {
    data class OnStudentQueryChange(val query: String) : ReportsAction
    data class OnStudentSelect(val studentId: String) : ReportsAction
    data class OnPeriodSelect(val period: ReportPeriod) : ReportsAction
    data class OnToggleMetrics(val checked: Boolean) : ReportsAction
    data class OnToggleQualitative(val checked: Boolean) : ReportsAction
    data class OnToggleAnamnese(val checked: Boolean) : ReportsAction
    data object OnExportPdf : ReportsAction
    data object OnShowDatePicker : ReportsAction
    data object OnDismissDatePicker : ReportsAction
    data class OnDateRangeConfirmed(val startMs: Long?, val endMs: Long?) : ReportsAction
    data object OnPdfOpened : ReportsAction
    data object OnDismissNoSessions : ReportsAction
}

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val studentRepository: StudentRepository,
    private val sessionRepository: SessionRepository,
    private val anamneseRepository: AnamneseRepository,
    private val pdfGenerator: ReportPdfGenerator,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()

    private var allStudents: List<ReportStudentOption> = emptyList()
    private var currentSessions: List<TaskNotebookSession> = emptyList()

    init {
        loadStudents()
    }

    fun onAction(action: ReportsAction) {
        when (action) {
            is ReportsAction.OnStudentQueryChange -> onQueryChange(action.query)
            is ReportsAction.OnStudentSelect -> onStudentSelect(action.studentId)
            is ReportsAction.OnPeriodSelect -> {
                val showPicker = action.period == ReportPeriod.CUSTOM
                _uiState.update { it.copy(selectedPeriod = action.period, showDateRangePicker = showPicker) }
                recomputePreviews()
            }
            is ReportsAction.OnToggleMetrics -> _uiState.update { it.copy(includeMetrics = action.checked) }
            is ReportsAction.OnToggleQualitative -> _uiState.update { it.copy(includeQualitative = action.checked) }
            is ReportsAction.OnToggleAnamnese -> _uiState.update { it.copy(includeAnamnese = action.checked) }
            ReportsAction.OnExportPdf -> exportPdf()
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
            ReportsAction.OnPdfOpened -> _uiState.update { it.copy(generatedPdfPath = null) }
            ReportsAction.OnDismissNoSessions -> _uiState.update { it.copy(showNoSessions = false) }
        }
    }

    private fun loadStudents() {
        viewModelScope.launch {
            allStudents = studentRepository.list().getOrNull()
                ?.map { ReportStudentOption(it.id, it.name) }
                ?.sortedBy { it.name }
                .orEmpty()
            _uiState.update { it.copy(studentResults = filterStudents(it.studentQuery)) }
        }
    }

    private fun onQueryChange(query: String) {
        _uiState.update { it.copy(studentQuery = query, studentResults = filterStudents(query)) }
    }

    private fun filterStudents(query: String): List<ReportStudentOption> =
        if (query.isBlank()) allStudents
        else allStudents.filter { it.name.contains(query, ignoreCase = true) }

    private fun onStudentSelect(studentId: String) {
        val student = allStudents.firstOrNull { it.id == studentId } ?: return
        _uiState.update {
            it.copy(selectedStudentId = student.id, selectedStudentName = student.name, isLoading = true)
        }
        viewModelScope.launch {
            currentSessions = sessionRepository.listByStudent(student.id).getOrNull().orEmpty()
            _uiState.update { it.copy(isLoading = false) }
            recomputePreviews()
        }
    }

    private fun recomputePreviews() {
        val state = _uiState.value
        val zone = ZoneId.systemDefault()
        val (from, to) = dateRange(state)

        val filtered = currentSessions.filter { session ->
            val started = session.startedAt.toInstantOrNull() ?: return@filter true
            (from == null || !started.isBefore(from)) && (to == null || !started.isAfter(to))
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

    private fun exportPdf() {
        val state = _uiState.value
        val studentId = state.selectedStudentId ?: return
        if (state.selectedPeriod == ReportPeriod.CUSTOM &&
            (state.customStartDateMs == null || state.customEndDateMs == null)
        ) {
            _uiState.update { it.copy(exportError = "Selecione o intervalo de datas.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, exportError = null) }

            val (startIso, endIso) = isoRange(state)
            val analysisResult = sessionRepository.analysis(studentId, startDate = startIso, endDate = endIso)
            if (analysisResult is ApiResult.Error) {
                _uiState.update { it.copy(isExporting = false, exportError = analysisResult.message) }
                return@launch
            }
            val analysis = (analysisResult as ApiResult.Success).data

            if (analysis.sessions.isEmpty()) {
                _uiState.update { it.copy(isExporting = false, showNoSessions = true) }
                return@launch
            }

            // Persiste o snapshot no histórico (best-effort; não bloqueia o PDF).
            sessionRepository.snapshot(studentId, startDate = startIso, endDate = endIso)

            val anamnese = if (state.includeAnamnese) loadAnamnese(studentId) else emptyList()

            val data = buildPdfData(state, analysis, anamnese)
            val fileName = "relatorio-${(state.selectedStudentName ?: "aluno").sanitized()}-${System.currentTimeMillis()}.pdf"
            val generated = runCatching { pdfGenerator.generate(data, fileName) }.getOrNull()

            if (generated == null) {
                _uiState.update { it.copy(isExporting = false, exportError = "Falha ao gerar o PDF.") }
            } else {
                _uiState.update { it.copy(isExporting = false, generatedPdfPath = generated.file.absolutePath) }
            }
        }
    }

    private suspend fun loadAnamnese(studentId: String): List<ReportPdfAnamnese> {
        val responses = anamneseRepository.listResponsesByStudent(studentId).getOrNull().orEmpty()
        if (responses.isEmpty()) return emptyList()
        val templatesById = anamneseRepository.listTemplates().getOrNull().orEmpty().associateBy { it.id }
        return responses.map { it.toPdfBlock(templatesById[it.templateId]) }
    }

    private fun buildPdfData(
        state: ReportsUiState,
        analysis: StudentAnalysis,
        anamnese: List<ReportPdfAnamnese>,
    ): ReportPdfData {
        val zone = ZoneId.systemDefault()
        val categories = analysis.categories.values
            .sortedBy { it.category.ordinal }
            .map {
                ReportPdfCategory(
                    label = it.category.displayName(),
                    percent = (it.accuracy * 100).toInt(),
                    colorInt = it.category.colorInt(),
                )
            }
        val sessions = analysis.sessions
            .sortedByDescending { it.startedAt }
            .map { session ->
                val correct = session.answers.count { a -> a.isCorrect }
                ReportPdfSession(
                    name = session.name,
                    date = session.startedAt.toInstantOrNull()?.atZone(zone)?.format(PREVIEW_DATE_FORMAT) ?: session.startedAt,
                    score = "$correct/${session.answers.size}",
                )
            }
        val observations = analysis.sessions
            .filter { !it.observation.isNullOrBlank() }
            .map { ReportPdfObservation(it.name, it.observation!!.trim()) }

        return ReportPdfData(
            studentName = state.selectedStudentName.orEmpty(),
            periodLabel = state.selectedPeriod.label(),
            generatedAt = OffsetDateTime.now().format(GENERATED_AT_FORMAT),
            includeMetrics = state.includeMetrics,
            includeQualitative = state.includeQualitative,
            includeAnamnese = state.includeAnamnese,
            overallCorrect = analysis.total.correct,
            overallTotal = analysis.total.total,
            overallAccuracyPercent = (analysis.total.accuracy * 100).toInt(),
            categories = categories,
            sessions = sessions,
            observations = observations,
            anamnese = anamnese,
        )
    }

    /** Intervalo como Instant para filtrar a prévia na tela. */
    private fun dateRange(state: ReportsUiState): Pair<Instant?, Instant?> {
        val now = Instant.now()
        return when (state.selectedPeriod) {
            ReportPeriod.LAST_3_MONTHS -> now.minusDaysCompat(90) to now
            ReportPeriod.LAST_6_MONTHS -> now.minusDaysCompat(180) to now
            ReportPeriod.ALL -> null to null
            ReportPeriod.CUSTOM -> state.customStartDateMs?.let { Instant.ofEpochMilli(it) } to
                state.customEndDateMs?.let { Instant.ofEpochMilli(it) }
        }
    }

    /** Mesmo intervalo em ISO para os parâmetros da API. */
    private fun isoRange(state: ReportsUiState): Pair<String?, String?> {
        val (from, to) = dateRange(state)
        return from?.toString() to to?.toString()
    }
}

// ── Helpers ──────────────────────────────────────────────────────────────────

private fun ReportPeriod.label(): String = when (this) {
    ReportPeriod.LAST_3_MONTHS -> "Últimos 3 meses"
    ReportPeriod.LAST_6_MONTHS -> "Últimos 6 meses"
    ReportPeriod.ALL -> "Todo o período"
    ReportPeriod.CUSTOM -> "Período personalizado"
}

private fun TaskCategory.colorInt(): Int = when (this) {
    TaskCategory.READING -> 0xFF2563EB.toInt()
    TaskCategory.WRITING -> 0xFFEA580C.toInt()
    TaskCategory.VOCABULARY -> 0xFF7C3AED.toInt()
    TaskCategory.COMPREHENSION -> 0xFF16A34A.toInt()
}

private fun AnamneseResponse.toPdfBlock(template: AnamneseTemplate?): ReportPdfAnamnese {
    val questionsById = template?.questions?.associateBy { it.id }.orEmpty()
    val items = answers.map { answer ->
        val question = questionsById[answer.questionId]
        ReportPdfQa(
            question = question?.text ?: "Pergunta",
            answer = answer.toDisplayText(question),
        )
    }
    return ReportPdfAnamnese(
        title = template?.title ?: "Anamnese",
        date = answeredAt.toInstantOrNull()?.atZone(ZoneId.systemDefault())?.format(PREVIEW_DATE_FORMAT).orEmpty(),
        items = items,
    )
}

private fun com.labirintodosaber.data.model.AnamneseAnswer.toDisplayText(question: AnamneseQuestion?): String {
    textValue?.takeIf { it.isNotBlank() }?.let { return it }
    selectedOptionId?.let { id ->
        return question?.options?.firstOrNull { it.id == id }?.text ?: "—"
    }
    selectedOptionIds?.takeIf { it.isNotEmpty() }?.let { ids ->
        val opts = question?.options.orEmpty()
        return ids.mapNotNull { id -> opts.firstOrNull { it.id == id }?.text }.joinToString(", ").ifBlank { "—" }
    }
    fileUrl?.takeIf { it.isNotBlank() }?.let { return "Arquivo anexado" }
    return "—"
}

private fun String.sanitized(): String = trim().replace(Regex("[^A-Za-z0-9]+"), "_").trim('_').ifBlank { "aluno" }

private fun Instant.minusDaysCompat(days: Long): Instant = this.minusSeconds(days * 24 * 60 * 60)

private fun String.toInstantOrNull(): Instant? =
    runCatching { Instant.parse(this) }.getOrNull()
        ?: runCatching { OffsetDateTime.parse(this).toInstant() }.getOrNull()

private val PREVIEW_DATE_FORMAT: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd 'de' MMM 'de' yyyy", Locale("pt", "BR"))

private val GENERATED_AT_FORMAT: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale("pt", "BR"))
