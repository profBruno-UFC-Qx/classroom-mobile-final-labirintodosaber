package com.labirintodosaber.ui.screen.studentprofile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.labirintodosaber.data.model.AnamneseResponse
import com.labirintodosaber.data.model.AnamneseTemplate
import com.labirintodosaber.data.model.Gender
import com.labirintodosaber.data.model.Student
import com.labirintodosaber.data.model.StudentAnalysisReport
import com.labirintodosaber.data.model.TaskNotebookSession
import com.labirintodosaber.data.remote.getOrNull
import com.labirintodosaber.data.repository.AnamneseRepository
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

@HiltViewModel
class StudentProfileViewModel @Inject constructor(
    private val studentRepository: StudentRepository,
    private val sessionRepository: SessionRepository,
    private val anamneseRepository: AnamneseRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val studentId: String = checkNotNull(savedStateHandle["studentId"])

    private val _uiState = MutableStateFlow(StudentProfileUiState())
    val uiState: StateFlow<StudentProfileUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun onAction(action: StudentProfileAction) {
        when (action) {
            is StudentProfileAction.OnTabSelect -> _uiState.update { it.copy(selectedTab = action.tab) }
            StudentProfileAction.OnGenerateReportClick -> generateReport()
            StudentProfileAction.OnEditClick -> { /* TODO: editar aluno */ }
        }
    }

    private fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val student = studentRepository.list().getOrNull()?.firstOrNull { it.id == studentId }
            if (student == null) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Aluno não encontrado.") }
                return@launch
            }

            val analysis = sessionRepository.analysis(studentId).getOrNull()
            val progress = ((analysis?.total?.accuracy ?: 0.0) * 100).roundToInt()
            val categories = analysis?.categories?.map { (key, value) ->
                CategoryProgress(categoryName(key), (value.accuracy * 100).roundToInt())
            }.orEmpty()

            val sessions = sessionRepository.listByStudent(studentId).getOrNull().orEmpty()
                .sortedByDescending { it.startedAt }
                .map { it.toSessionRow() }

            val documents = sessionRepository.analysisHistory(studentId).getOrNull().orEmpty()
                .mapIndexed { index, report -> report.toDocumentRow(index) }

            val templates = anamneseRepository.listTemplates().getOrNull().orEmpty()
            val anamneses = anamneseRepository.listResponsesByStudent(studentId).getOrNull().orEmpty()
                .map { it.toAnamneseRow(templates) }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    name = student.name,
                    age = student.age,
                    gender = if (student.gender == Gender.FEMALE) "Feminino" else "Masculino",
                    address = student.formattedAddress(),
                    objective = student.learningTopics.joinToString(", "),
                    isGirl = student.gender == Gender.FEMALE,
                    photoUrl = student.photoUrl?.takeIf { url -> url.isNotBlank() },
                    progressPercent = progress,
                    categoryProgress = categories,
                    sessions = sessions,
                    documents = documents,
                    anamneses = anamneses,
                )
            }
        }
    }

    private fun generateReport() {
        viewModelScope.launch {
            _uiState.update { it.copy(isGeneratingReport = true) }
            sessionRepository.snapshot(studentId)
            val documents = sessionRepository.analysisHistory(studentId).getOrNull().orEmpty()
                .mapIndexed { index, report -> report.toDocumentRow(index) }
            _uiState.update {
                it.copy(isGeneratingReport = false, documents = documents, selectedTab = StudentProfileTab.DOCUMENTS)
            }
        }
    }
}

// ── Mapeamento domínio → UI ────────────────────────────────────────────────────

private fun Student.formattedAddress(): String =
    "$road, $housenumber — CEP $zipcode"

private fun TaskNotebookSession.toSessionRow(): SessionRow {
    val correct = answers.count { it.isCorrect }
    val hitRate = if (answers.isEmpty()) 0 else (correct * 100) / answers.size
    return SessionRow(
        id = id,
        name = name,
        date = startedAt.formatDate(),
        hitRatePercent = hitRate,
        questionCount = answers.size,
        finished = finishedAt != null,
    )
}

private fun StudentAnalysisReport.toDocumentRow(index: Int) = DocumentRow(
    title = "Relatório de desempenho #${index + 1}",
    period = when {
        startDate != null && endDate != null -> "${startDate.formatDate()} – ${endDate.formatDate()}"
        limit != null -> "Últimas $limit sessões"
        else -> "Geral"
    },
    accuracyPercent = (accuracy * 100).roundToInt(),
)

private fun AnamneseResponse.toAnamneseRow(templates: List<AnamneseTemplate>) = AnamneseRow(
    id = id,
    title = templates.firstOrNull { it.id == templateId }?.title ?: "Anamnese",
    date = answeredAt.formatDate(),
)

private fun categoryName(wire: String): String = when (wire) {
    "reading" -> "Leitura"
    "writing" -> "Escrita"
    "vocabulary" -> "Vocabulário"
    "comprehension" -> "Compreensão"
    else -> wire.replaceFirstChar { it.uppercase() }
}

private val DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy")

private fun String.formatDate(): String {
    val instant = runCatching { Instant.parse(this) }.getOrNull()
        ?: runCatching { OffsetDateTime.parse(this).toInstant() }.getOrNull()
        ?: return this
    return instant.atZone(ZoneId.systemDefault()).format(DATE_FORMAT)
}

enum class StudentProfileTab { PROGRESS, SESSIONS, DOCUMENTS, ANAMNESE }

data class CategoryProgress(val name: String, val percent: Int)

data class SessionRow(
    val id: String,
    val name: String,
    val date: String,
    val hitRatePercent: Int,
    val questionCount: Int,
    val finished: Boolean,
)

data class DocumentRow(
    val title: String,
    val period: String,
    val accuracyPercent: Int,
)

data class AnamneseRow(
    val id: String,
    val title: String,
    val date: String,
)

data class StudentProfileUiState(
    val name: String = "",
    val age: Int = 0,
    val gender: String = "",
    val birthDate: String = "—",
    val address: String = "",
    val objective: String = "",
    val isGirl: Boolean = true,
    val avatarBorderColorHex: Long = 0xFFE94B8F,
    val photoUrl: String? = null,
    val progressPercent: Int = 0,
    val categoryProgress: List<CategoryProgress> = emptyList(),
    val sessions: List<SessionRow> = emptyList(),
    val documents: List<DocumentRow> = emptyList(),
    val anamneses: List<AnamneseRow> = emptyList(),
    val selectedTab: StudentProfileTab = StudentProfileTab.PROGRESS,
    val isLoading: Boolean = false,
    val isGeneratingReport: Boolean = false,
    val errorMessage: String? = null,
)

sealed interface StudentProfileAction {
    data class OnTabSelect(val tab: StudentProfileTab) : StudentProfileAction
    data object OnGenerateReportClick : StudentProfileAction
    data object OnEditClick : StudentProfileAction
}
