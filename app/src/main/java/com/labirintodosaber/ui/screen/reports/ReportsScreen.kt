package com.labirintodosaber.ui.screen.reports

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.CheckBoxOutlineBlank
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val DATE_FMT = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))

private fun Long.toFormattedDate(): String = DATE_FMT.format(Date(this))

@Composable
fun ReportsScreen(
    onHomeClick: () -> Unit = {},
    onActivitiesClick: () -> Unit = {},
    onStudentsClick: () -> Unit = {},
    onMenuClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onPdfGenerated: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: ReportsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.generatedPdfPath) {
        uiState.generatedPdfPath?.let {
            onPdfGenerated(it)
            viewModel.onAction(ReportsAction.OnPdfOpened)
        }
    }

    ReportsContent(
        uiState = uiState,
        onAction = viewModel::onAction,
        onHomeClick = onHomeClick,
        onActivitiesClick = onActivitiesClick,
        onStudentsClick = onStudentsClick,
        onMenuClick = onMenuClick,
        onProfileClick = onProfileClick,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReportsContent(
    uiState: ReportsUiState,
    onAction: (ReportsAction) -> Unit,
    onHomeClick: () -> Unit,
    onActivitiesClick: () -> Unit,
    onStudentsClick: () -> Unit,
    onMenuClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    if (uiState.showDateRangePicker) {
        DateRangePickerModal(
            initialStartMs = uiState.customStartDateMs,
            initialEndMs = uiState.customEndDateMs,
            onConfirm = { startMs, endMs ->
                onAction(ReportsAction.OnDateRangeConfirmed(startMs = startMs, endMs = endMs))
            },
            onDismiss = { onAction(ReportsAction.OnDismissDatePicker) },
        )
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.reports_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Outlined.Menu, contentDescription = null, tint = TextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = onProfileClick) {
                        Icon(Icons.Outlined.AccountCircle, contentDescription = null, tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White),
            )
        },
        bottomBar = {
            ReportsBottomBar(
                onHomeClick = onHomeClick,
                onActivitiesClick = onActivitiesClick,
                onStudentsClick = onStudentsClick,
            )
        },
        containerColor = Color(0xFFF5F5F5),
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Step 1 — Buscar Aluno
            ReportSectionCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StepBadge(number = 1)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.reports_step_student),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = uiState.studentQuery,
                    onValueChange = { onAction(ReportsAction.OnStudentQueryChange(it)) },
                    placeholder = {
                        Text(
                            stringResource(R.string.reports_student_placeholder),
                            color = TextSecondary,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    },
                    leadingIcon = {
                        Icon(Icons.Outlined.Search, contentDescription = null, tint = TextSecondary)
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TealPrimary,
                        unfocusedBorderColor = InputBorder,
                        focusedContainerColor = InputBackground,
                        unfocusedContainerColor = InputBackground,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )

                if (uiState.studentResults.isEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.reports_no_students),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                } else {
                    Spacer(modifier = Modifier.height(8.dp))
                    uiState.studentResults.forEach { student ->
                        StudentOptionRow(
                            name = student.name,
                            selected = student.id == uiState.selectedStudentId,
                            onClick = { onAction(ReportsAction.OnStudentSelect(student.id)) },
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            }

            // Step 2 — Período
            ReportSectionCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StepBadge(number = 2, amber = true)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.reports_step_period),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                listOf(
                    ReportPeriod.LAST_3_MONTHS to stringResource(R.string.reports_period_3_months),
                    ReportPeriod.LAST_6_MONTHS to stringResource(R.string.reports_period_6_months),
                    ReportPeriod.ALL           to stringResource(R.string.reports_period_all),
                    ReportPeriod.CUSTOM        to stringResource(R.string.reports_period_custom),
                ).forEach { (period, label) ->
                    val isCustomSelected = period == ReportPeriod.CUSTOM && uiState.selectedPeriod == ReportPeriod.CUSTOM
                    val dateRangeSubtitle = if (isCustomSelected && uiState.customStartDateMs != null) {
                        val start = uiState.customStartDateMs.toFormattedDate()
                        val end = uiState.customEndDateMs?.toFormattedDate() ?: "—"
                        "$start → $end"
                    } else null

                    PeriodOptionRow(
                        label = label,
                        subtitle = dateRangeSubtitle,
                        selected = uiState.selectedPeriod == period,
                        onClick = {
                            if (period == ReportPeriod.CUSTOM && uiState.selectedPeriod == ReportPeriod.CUSTOM) {
                                // já selecionado — re-abre o picker
                                onAction(ReportsAction.OnShowDatePicker)
                            } else {
                                onAction(ReportsAction.OnPeriodSelect(period))
                            }
                        },
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // Prévia das sessões
            if (uiState.sessionPreviews.isNotEmpty()) {
                ReportSectionCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.AutoMirrored.Outlined.Assignment,
                            contentDescription = null,
                            tint = TealPrimary,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.reports_session_preview_title),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    uiState.sessionPreviews.forEach { session ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = session.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    text = session.date,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                )
                            }
                            Text(
                                text = session.score,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary,
                            )
                        }
                    }
                }
            }

            // Conteúdo do Relatório
            ReportSectionCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.AutoMirrored.Outlined.Assignment,
                        contentDescription = null,
                        tint = TealPrimary,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.reports_content_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                ReportCheckboxRow(
                    label = stringResource(R.string.reports_content_metrics),
                    description = stringResource(R.string.reports_content_metrics_desc),
                    checked = uiState.includeMetrics,
                    onToggle = { onAction(ReportsAction.OnToggleMetrics(!uiState.includeMetrics)) },
                )
                Spacer(modifier = Modifier.height(10.dp))
                ReportCheckboxRow(
                    label = stringResource(R.string.reports_content_qualitative),
                    description = stringResource(R.string.reports_content_qualitative_desc),
                    checked = uiState.includeQualitative,
                    onToggle = { onAction(ReportsAction.OnToggleQualitative(!uiState.includeQualitative)) },
                )
                Spacer(modifier = Modifier.height(10.dp))
                ReportCheckboxRow(
                    label = stringResource(R.string.reports_content_anamnese),
                    description = stringResource(R.string.reports_content_anamnese_desc),
                    checked = uiState.includeAnamnese,
                    onToggle = { onAction(ReportsAction.OnToggleAnamnese(!uiState.includeAnamnese)) },
                )
            }

            // Botão exportar
            val studentLabel = uiState.selectedStudentName
                ?: stringResource(R.string.reports_export_no_student)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (uiState.canExport)
                            Brush.horizontalGradient(listOf(TealDark, TealLight))
                        else
                            Brush.horizontalGradient(listOf(Color(0xFFB0B0B0), Color(0xFFD0D0D0)))
                    )
                    .clickable(enabled = uiState.canExport && !uiState.isExporting) {
                        onAction(ReportsAction.OnExportPdf)
                    },
                contentAlignment = Alignment.Center,
            ) {
                if (uiState.isExporting) {
                    CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(24.dp))
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Outlined.Download,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.reports_export_button),
                                color = Color.White,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                        Text(
                            text = stringResource(R.string.reports_export_subtitle, studentLabel),
                            color = Color.White.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                        )
                    }
                }
            }

            uiState.exportError?.let { error ->
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFDC2626),
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }

    if (uiState.showNoSessions) {
        AlertDialog(
            onDismissRequest = { onAction(ReportsAction.OnDismissNoSessions) },
            confirmButton = {
                TextButton(onClick = { onAction(ReportsAction.OnDismissNoSessions) }) {
                    Text(stringResource(R.string.reports_no_sessions_dismiss))
                }
            },
            title = { Text(stringResource(R.string.reports_no_sessions_title)) },
            text = { Text(stringResource(R.string.reports_no_sessions_message)) },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateRangePickerModal(
    initialStartMs: Long?,
    initialEndMs: Long?,
    onConfirm: (Long?, Long?) -> Unit,
    onDismiss: () -> Unit,
) {
    val state = rememberDateRangePickerState(
        initialSelectedStartDateMillis = initialStartMs,
        initialSelectedEndDateMillis = initialEndMs,
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onConfirm(state.selectedStartDateMillis, state.selectedEndDateMillis) }) {
                Text(stringResource(R.string.reports_date_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.reports_date_cancel))
            }
        },
    ) {
        DateRangePicker(
            state = state,
            title = {
                Text(
                    text = stringResource(R.string.reports_date_picker_title),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(start = 24.dp, top = 16.dp),
                )
            },
            headline = {
                val start = state.selectedStartDateMillis?.toFormattedDate() ?: "—"
                val end = state.selectedEndDateMillis?.toFormattedDate() ?: "—"
                Text(
                    text = "$start  →  $end",
                    style = MaterialTheme.typography.titleSmall,
                    color = TealPrimary,
                    modifier = Modifier.padding(start = 24.dp, bottom = 8.dp),
                )
            },
            showModeToggle = false,
            modifier = Modifier.height(500.dp),
        )
    }
}

@Composable
private fun ReportSectionCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

@Composable
private fun StepBadge(
    number: Int,
    amber: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val bg = if (amber) Color(0xFFF5A623) else TealPrimary
    Box(
        modifier = modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(bg),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = number.toString(),
            color = Color.White,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun PeriodOptionRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
) {
    val borderColor = if (selected) TealPrimary else InputBorder
    val bgColor = if (selected) Color(0xFFEBFAF9) else Color.White
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .border(if (selected) 1.5.dp else 1.dp, borderColor, RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (selected) TealPrimary else TextPrimary,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TealPrimary.copy(alpha = 0.8f),
                )
            }
        }
    }
}

@Composable
private fun StudentOptionRow(
    name: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor = if (selected) TealPrimary else InputBorder
    val bgColor = if (selected) Color(0xFFEBFAF9) else Color.White
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .border(if (selected) 1.5.dp else 1.dp, borderColor, RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Outlined.Person,
            contentDescription = null,
            tint = if (selected) TealPrimary else TextSecondary,
            modifier = Modifier.size(20.dp),
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) TealPrimary else TextPrimary,
            modifier = Modifier.weight(1f),
        )
        if (selected) {
            Icon(
                imageVector = Icons.Outlined.CheckBox,
                contentDescription = null,
                tint = TealPrimary,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun ReportCheckboxRow(
    label: String,
    description: String,
    checked: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onToggle() },
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            imageVector = if (checked) Icons.Outlined.CheckBox else Icons.Outlined.CheckBoxOutlineBlank,
            contentDescription = null,
            tint = if (checked) TealPrimary else TextSecondary,
            modifier = Modifier.size(22.dp),
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
            )
        }
    }
}

@Composable
private fun ReportsBottomBar(
    onHomeClick: () -> Unit,
    onActivitiesClick: () -> Unit,
    onStudentsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    data class NavItem(val label: Int, val icon: ImageVector, val filledIcon: ImageVector, val onClick: () -> Unit, val selected: Boolean)
    val items = listOf(
        NavItem(R.string.dashboard_tab_home,       Icons.Outlined.Home,                   Icons.Filled.Home,                   onHomeClick,       false),
        NavItem(R.string.dashboard_tab_activities, Icons.AutoMirrored.Outlined.MenuBook,  Icons.AutoMirrored.Outlined.MenuBook, onActivitiesClick, false),
        NavItem(R.string.dashboard_tab_students,   Icons.Outlined.Person,                 Icons.Outlined.Person,               onStudentsClick,   false),
        NavItem(R.string.dashboard_tab_reports,    Icons.AutoMirrored.Outlined.Assignment, Icons.AutoMirrored.Outlined.Assignment, {},              true),
    )

    NavigationBar(containerColor = Color.White, tonalElevation = 4.dp, modifier = modifier) {
        items.forEach { item ->
            NavigationBarItem(
                selected = item.selected,
                onClick = item.onClick,
                icon = {
                    Icon(
                        imageVector = if (item.selected) item.filledIcon else item.icon,
                        contentDescription = null,
                    )
                },
                label = {
                    Text(stringResource(item.label), style = MaterialTheme.typography.labelSmall)
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = TealPrimary,
                    selectedTextColor = TealPrimary,
                    unselectedIconColor = TextSecondary,
                    unselectedTextColor = TextSecondary,
                    indicatorColor = Color.Transparent,
                ),
            )
        }
    }
}
