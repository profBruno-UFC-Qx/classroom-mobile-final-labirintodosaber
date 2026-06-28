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
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

data class AgendaItem(
    val id: String,
    val studentName: String,
    val dateLabel: String,
    val timeLabel: String,
    val statusLabel: String,
    val statusColorHex: Long,
    val observation: String?,
)

data class AgendaUiState(
    val appointments: List<AgendaItem> = emptyList(),
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
            val isFirstLoad = _uiState.value.appointments.isEmpty()
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

            val items = appointments
                .sortedByDescending { it.scheduledAt }
                .map { it.toItem(namesById[it.studentId] ?: "Aluno") }

            _uiState.update { it.copy(isLoading = false, errorMessage = null, appointments = items) }
        }
    }
}

private fun Appointment.toItem(studentName: String): AgendaItem {
    val instant = scheduledAt.toInstantOrNull()
    val zoned = instant?.atZone(ZoneId.systemDefault())
    return AgendaItem(
        id = id,
        studentName = studentName,
        dateLabel = zoned?.format(DATE_FORMAT) ?: scheduledAt,
        timeLabel = zoned?.format(TIME_FORMAT) ?: "",
        statusLabel = status.label(),
        statusColorHex = status.colorHex(),
        observation = observation?.takeIf { it.isNotBlank() },
    )
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

private val DATE_FORMAT = DateTimeFormatter.ofPattern("dd 'de' MMM 'de' yyyy", Locale("pt", "BR"))
private val TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm")
