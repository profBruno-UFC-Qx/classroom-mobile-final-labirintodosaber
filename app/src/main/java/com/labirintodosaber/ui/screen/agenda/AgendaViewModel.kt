package com.labirintodosaber.ui.screen.agenda

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.labirintodosaber.data.model.Appointment
import com.labirintodosaber.data.model.AppointmentStatus
import com.labirintodosaber.data.remote.getOrNull
import com.labirintodosaber.data.repository.AppointmentRepository
import com.labirintodosaber.data.repository.StudentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

data class AgendaItem(
    val id: String,
    val studentName: String,
    val timeLabel: String,
    val statusLabel: String,
    val statusColorHex: Long,
    val observation: String?,
)

/** Atendimentos de um mesmo dia, com cabeçalho de data (estilo agenda/calendário). */
data class AgendaDaySection(
    val dateTitle: String,
    val items: List<AgendaItem>,
)

data class AgendaUiState(
    val sections: List<AgendaDaySection> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class AgendaViewModel @Inject constructor(
    private val appointmentRepository: AppointmentRepository,
    private val studentRepository: StudentRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AgendaUiState())
    val uiState: StateFlow<AgendaUiState> = _uiState.asStateFlow()

    /** Recarrega ao entrar e ao retomar a tela (volta de criar/editar). */
    fun refresh() = load()

    private fun load() {
        viewModelScope.launch {
            val isFirstLoad = _uiState.value.sections.isEmpty()
            if (isFirstLoad) _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val appointments = appointmentRepository.list().getOrNull()
            if (appointments == null) {
                _uiState.update {
                    if (isFirstLoad) it.copy(isLoading = false, errorMessage = "Não foi possível carregar a agenda.")
                    else it.copy(isLoading = false)
                }
                return@launch
            }
            val namesById = studentRepository.list().getOrNull().orEmpty().associate { it.id to it.name }
            val zone = ZoneId.systemDefault()
            val today = LocalDate.now()

            // Agrupa por dia, dias em ordem crescente e atendimentos por horário.
            val sections = appointments
                .mapNotNull { appt -> appt.scheduledAt.toInstantOrNull()?.let { it.atZone(zone) to appt } }
                .groupBy { (zoned, _) -> zoned.toLocalDate() }
                .toSortedMap()
                .map { (date, entries) ->
                    AgendaDaySection(
                        dateTitle = date.headerTitle(today),
                        items = entries
                            .sortedBy { (zoned, _) -> zoned }
                            .map { (zoned, appt) ->
                                appt.toItem(namesById[appt.studentId] ?: "Aluno", zoned.format(TIME_FORMAT))
                            },
                    )
                }

            _uiState.update { it.copy(isLoading = false, errorMessage = null, sections = sections) }
        }
    }
}

private fun Appointment.toItem(studentName: String, timeLabel: String) = AgendaItem(
    id = id,
    studentName = studentName,
    timeLabel = timeLabel,
    statusLabel = status.label(),
    statusColorHex = status.colorHex(),
    observation = observation?.takeIf { it.isNotBlank() },
)

private fun LocalDate.headerTitle(today: LocalDate): String = when (this) {
    today -> "Hoje • ${format(HEADER_FORMAT)}"
    today.plusDays(1) -> "Amanhã • ${format(HEADER_FORMAT)}"
    else -> format(HEADER_FORMAT).replaceFirstChar { it.uppercase() }
}

private fun AppointmentStatus.label(): String = when (this) {
    AppointmentStatus.PENDING -> "Pendente"
    AppointmentStatus.COMPLETED -> "Concluído"
    AppointmentStatus.CANCELLED -> "Cancelado"
}

private fun AppointmentStatus.colorHex(): Long = when (this) {
    AppointmentStatus.PENDING -> 0xFFEA580C
    AppointmentStatus.COMPLETED -> 0xFF16A34A
    AppointmentStatus.CANCELLED -> 0xFFDC2626
}

private fun String.toInstantOrNull(): Instant? =
    runCatching { Instant.parse(this) }.getOrNull()
        ?: runCatching { OffsetDateTime.parse(this).toInstant() }.getOrNull()

private val HEADER_FORMAT = DateTimeFormatter.ofPattern("EEE, dd 'de' MMM", Locale("pt", "BR"))
private val TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm")
