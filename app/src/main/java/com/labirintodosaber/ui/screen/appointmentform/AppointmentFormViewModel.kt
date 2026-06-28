package com.labirintodosaber.ui.screen.appointmentform

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.labirintodosaber.data.remote.ApiResult
import com.labirintodosaber.data.remote.dto.AppointmentUpdateRequest
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
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

data class StudentOption(val id: String, val name: String)

data class AppointmentFormUiState(
    val isEdit: Boolean = false,
    val isLoading: Boolean = false,
    val students: List<StudentOption> = emptyList(),
    val selectedStudentId: String? = null,
    val selectedStudentName: String = "",
    val dateMillis: Long? = null,
    val hour: Int = 9,
    val minute: Int = 0,
    val observation: String = "",
    val showDatePicker: Boolean = false,
    val showTimePicker: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val saveSuccess: Boolean = false,
)

val AppointmentFormUiState.dateLabel: String
    get() = dateMillis?.let {
        Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate().format(DATE_LABEL_FORMAT)
    } ?: "Selecione a data"

val AppointmentFormUiState.timeLabel: String
    get() = "%02d:%02d".format(hour, minute)

sealed interface AppointmentFormAction {
    data class OnStudentSelect(val studentId: String) : AppointmentFormAction
    data class OnDateSelected(val millis: Long?) : AppointmentFormAction
    data class OnTimeSelected(val hour: Int, val minute: Int) : AppointmentFormAction
    data class OnObservationChange(val text: String) : AppointmentFormAction
    data object OnShowDatePicker : AppointmentFormAction
    data object OnDismissDatePicker : AppointmentFormAction
    data object OnShowTimePicker : AppointmentFormAction
    data object OnDismissTimePicker : AppointmentFormAction
    data object OnSave : AppointmentFormAction
    data object OnDelete : AppointmentFormAction
}

@HiltViewModel
class AppointmentFormViewModel @Inject constructor(
    private val appointmentRepository: AppointmentRepository,
    private val studentRepository: StudentRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val appointmentId: String? = savedStateHandle.get<String>("appointmentId")

    private val _uiState = MutableStateFlow(AppointmentFormUiState(isEdit = appointmentId != null))
    val uiState: StateFlow<AppointmentFormUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun onAction(action: AppointmentFormAction) {
        when (action) {
            is AppointmentFormAction.OnStudentSelect -> _uiState.update {
                it.copy(selectedStudentId = action.studentId, errorMessage = null)
            }
            is AppointmentFormAction.OnDateSelected -> _uiState.update {
                it.copy(dateMillis = action.millis, showDatePicker = false, errorMessage = null)
            }
            is AppointmentFormAction.OnTimeSelected -> _uiState.update {
                it.copy(hour = action.hour, minute = action.minute, showTimePicker = false)
            }
            is AppointmentFormAction.OnObservationChange -> _uiState.update { it.copy(observation = action.text) }
            AppointmentFormAction.OnShowDatePicker -> _uiState.update { it.copy(showDatePicker = true) }
            AppointmentFormAction.OnDismissDatePicker -> _uiState.update { it.copy(showDatePicker = false) }
            AppointmentFormAction.OnShowTimePicker -> _uiState.update { it.copy(showTimePicker = true) }
            AppointmentFormAction.OnDismissTimePicker -> _uiState.update { it.copy(showTimePicker = false) }
            AppointmentFormAction.OnSave -> save()
            AppointmentFormAction.OnDelete -> delete()
        }
    }

    private fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val students = studentRepository.list().getOrNull().orEmpty().map { StudentOption(it.id, it.name) }

            if (appointmentId != null) {
                val appointment = appointmentRepository.getById(appointmentId).getOrNull()
                if (appointment == null) {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Agendamento não encontrado.") }
                    return@launch
                }
                val zoned = appointment.scheduledAt.toInstantOrNull()?.atZone(ZoneId.systemDefault())
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        students = students,
                        selectedStudentId = appointment.studentId,
                        selectedStudentName = students.firstOrNull { s -> s.id == appointment.studentId }?.name ?: "Aluno",
                        dateMillis = zoned?.toLocalDate()?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli(),
                        hour = zoned?.hour ?: 9,
                        minute = zoned?.minute ?: 0,
                        observation = appointment.observation.orEmpty(),
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false, students = students) }
            }
        }
    }

    private fun save() {
        val state = _uiState.value
        val validation = when {
            state.selectedStudentId == null -> "Selecione um aluno."
            state.dateMillis == null -> "Selecione a data."
            else -> null
        }
        if (validation != null) {
            _uiState.update { it.copy(errorMessage = validation) }
            return
        }

        val scheduledAt = buildScheduledAtIso(state.dateMillis!!, state.hour, state.minute)
        val observation = state.observation.trim().takeIf { it.isNotEmpty() }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            val result = if (state.isEdit && appointmentId != null) {
                appointmentRepository.update(
                    appointmentId,
                    AppointmentUpdateRequest(scheduledAt = scheduledAt, observation = observation),
                )
            } else {
                appointmentRepository.create(
                    studentId = state.selectedStudentId!!,
                    scheduledAt = scheduledAt,
                    observation = observation,
                )
            }
            when (result) {
                is ApiResult.Success -> _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
                is ApiResult.Error -> _uiState.update { it.copy(isSaving = false, errorMessage = result.message) }
            }
        }
    }

    private fun delete() {
        val id = appointmentId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            when (val result = appointmentRepository.delete(id)) {
                is ApiResult.Success -> _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
                is ApiResult.Error -> _uiState.update { it.copy(isSaving = false, errorMessage = result.message) }
            }
        }
    }
}

/** Combina data (millis UTC do DatePicker) + hora/minuto no fuso local → ISO. */
private fun buildScheduledAtIso(dateMillis: Long, hour: Int, minute: Int): String {
    val date: LocalDate = Instant.ofEpochMilli(dateMillis).atZone(ZoneOffset.UTC).toLocalDate()
    val instant = date.atTime(hour, minute).atZone(ZoneId.systemDefault()).toInstant()
    return instant.toString()
}

private fun String.toInstantOrNull(): Instant? =
    runCatching { Instant.parse(this) }.getOrNull()
        ?: runCatching { OffsetDateTime.parse(this).toInstant() }.getOrNull()

private val DATE_LABEL_FORMAT = DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy", Locale("pt", "BR"))
