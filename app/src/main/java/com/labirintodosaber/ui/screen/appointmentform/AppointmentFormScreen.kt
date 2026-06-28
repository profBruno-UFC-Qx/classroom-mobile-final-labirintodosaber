package com.labirintodosaber.ui.screen.appointmentform

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.labirintodosaber.R
import com.labirintodosaber.ui.theme.InputBackground
import com.labirintodosaber.ui.theme.InputBorder
import com.labirintodosaber.ui.theme.TealDark
import com.labirintodosaber.ui.theme.TealLight
import com.labirintodosaber.ui.theme.TealPrimary
import com.labirintodosaber.ui.theme.TextPrimary
import com.labirintodosaber.ui.theme.TextSecondary

@Composable
fun AppointmentFormScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AppointmentFormViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) onSaved()
    }

    AppointmentFormContent(
        uiState = uiState,
        onAction = viewModel::onAction,
        onBack = onBack,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppointmentFormContent(
    uiState: AppointmentFormUiState,
    onAction: (AppointmentFormAction) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(if (uiState.isEdit) R.string.appointment_edit_title else R.string.appointment_new_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = stringResource(R.string.back_button), tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White),
            )
        },
        containerColor = Color(0xFFF5F5F5),
    ) { padding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = TealPrimary)
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Aluno
            item {
                FieldLabel(stringResource(R.string.appointment_student_label))
                Spacer(modifier = Modifier.height(6.dp))
                if (uiState.isEdit) {
                    ReadOnlyField(value = uiState.selectedStudentName)
                } else {
                    StudentDropdown(
                        students = uiState.students,
                        selectedId = uiState.selectedStudentId,
                        onSelect = { onAction(AppointmentFormAction.OnStudentSelect(it)) },
                    )
                }
            }

            // Data
            item {
                FieldLabel(stringResource(R.string.appointment_date_label))
                Spacer(modifier = Modifier.height(6.dp))
                PickerField(
                    icon = Icons.Outlined.CalendarMonth,
                    text = uiState.dateLabel,
                    onClick = { onAction(AppointmentFormAction.OnShowDatePicker) },
                )
            }

            // Hora
            item {
                FieldLabel(stringResource(R.string.appointment_time_label))
                Spacer(modifier = Modifier.height(6.dp))
                PickerField(
                    icon = Icons.Outlined.Schedule,
                    text = uiState.timeLabel,
                    onClick = { onAction(AppointmentFormAction.OnShowTimePicker) },
                )
            }

            // Observação
            item {
                FieldLabel(stringResource(R.string.appointment_observation_label))
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = uiState.observation,
                    onValueChange = { onAction(AppointmentFormAction.OnObservationChange(it)) },
                    placeholder = { Text(stringResource(R.string.appointment_observation_placeholder), color = TextSecondary) },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TealPrimary,
                        unfocusedBorderColor = InputBorder,
                        focusedContainerColor = InputBackground,
                        unfocusedContainerColor = InputBackground,
                    ),
                )
            }

            uiState.errorMessage?.let { error ->
                item { Text(error, style = MaterialTheme.typography.bodySmall, color = Color(0xFFDC2626)) }
            }

            // Salvar
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Brush.horizontalGradient(listOf(TealDark, TealLight)))
                        .clickable(enabled = !uiState.isSaving) { onAction(AppointmentFormAction.OnSave) },
                    contentAlignment = Alignment.Center,
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
                    } else {
                        Text(stringResource(R.string.appointment_save), color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // Excluir (edição)
            if (uiState.isEdit) {
                item {
                    OutlinedButton(
                        onClick = { onAction(AppointmentFormAction.OnDelete) },
                        enabled = !uiState.isSaving,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDC2626)),
                    ) {
                        Icon(Icons.Outlined.Delete, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.appointment_delete), color = Color(0xFFDC2626), fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }

    // Date picker
    if (uiState.showDatePicker) {
        val dateState = rememberDatePickerState(initialSelectedDateMillis = uiState.dateMillis)
        DatePickerDialog(
            onDismissRequest = { onAction(AppointmentFormAction.OnDismissDatePicker) },
            confirmButton = {
                TextButton(onClick = { onAction(AppointmentFormAction.OnDateSelected(dateState.selectedDateMillis)) }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { onAction(AppointmentFormAction.OnDismissDatePicker) }) {
                    Text(stringResource(R.string.appointment_cancel))
                }
            },
        ) {
            DatePicker(state = dateState)
        }
    }

    // Time picker
    if (uiState.showTimePicker) {
        val timeState = rememberTimePickerState(initialHour = uiState.hour, initialMinute = uiState.minute, is24Hour = true)
        AlertDialog(
            onDismissRequest = { onAction(AppointmentFormAction.OnDismissTimePicker) },
            confirmButton = {
                TextButton(onClick = { onAction(AppointmentFormAction.OnTimeSelected(timeState.hour, timeState.minute)) }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { onAction(AppointmentFormAction.OnDismissTimePicker) }) {
                    Text(stringResource(R.string.appointment_cancel))
                }
            },
            text = {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                    TimePicker(state = timeState)
                }
            },
        )
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(text, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
}

@Composable
private fun ReadOnlyField(value: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF0F0F0))
            .border(1.dp, InputBorder, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 16.dp),
    ) {
        Text(value, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
    }
}

@Composable
private fun PickerField(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(InputBackground)
            .border(1.dp, InputBorder, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StudentDropdown(
    students: List<StudentOption>,
    selectedId: String?,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedName = students.firstOrNull { it.id == selectedId }?.name.orEmpty()

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = selectedName,
            onValueChange = {},
            readOnly = true,
            placeholder = { Text(stringResource(R.string.appointment_student_placeholder), color = TextSecondary) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = TealPrimary,
                unfocusedBorderColor = InputBorder,
                focusedContainerColor = InputBackground,
                unfocusedContainerColor = InputBackground,
            ),
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            students.forEach { student ->
                DropdownMenuItem(
                    text = { Text(student.name) },
                    onClick = {
                        onSelect(student.id)
                        expanded = false
                    },
                )
            }
        }
    }
}
