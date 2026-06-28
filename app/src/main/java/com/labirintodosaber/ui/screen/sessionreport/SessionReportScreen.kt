package com.labirintodosaber.ui.screen.sessionreport

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.TrackChanges
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.labirintodosaber.R
import com.labirintodosaber.ui.components.ProfileActionIcon
import com.labirintodosaber.ui.theme.InputBackground
import com.labirintodosaber.ui.theme.InputBorder
import com.labirintodosaber.ui.theme.TealDark
import com.labirintodosaber.ui.theme.TealLight
import com.labirintodosaber.ui.theme.TealPrimary
import com.labirintodosaber.ui.theme.TextPrimary
import com.labirintodosaber.ui.theme.TextSecondary

@Composable
fun SessionReportScreen(
    onHomeClick: () -> Unit = {},
    onMenuClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: SessionReportViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SessionReportContent(
        uiState = uiState,
        onAction = viewModel::onAction,
        onHomeClick = onHomeClick,
        onMenuClick = onMenuClick,
        onProfileClick = onProfileClick,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SessionReportContent(
    uiState: SessionReportUiState,
    onAction: (SessionReportAction) -> Unit,
    onHomeClick: () -> Unit,
    onMenuClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.session_topbar_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Outlined.Menu, contentDescription = stringResource(R.string.dashboard_menu_desc), tint = TextPrimary)
                    }
                },
                actions = {
                    ProfileActionIcon(onClick = onProfileClick)
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White),
            )
        },
        bottomBar = {
            SessionReportBottomBar(onHomeClick = onHomeClick)
        },
        containerColor = Color(0xFFF5F5F5),
    ) { padding ->
        val hasData = uiState.totalQuestions > 0 ||
            uiState.categoryAccuracy.isNotEmpty() ||
            uiState.studentName.isNotBlank()
        when {
            uiState.isLoading -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = TealPrimary)
            }

            uiState.errorMessage != null && !hasData -> Box(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 32.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(uiState.errorMessage, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50.dp))
                            .background(TealPrimary)
                            .clickable { onAction(SessionReportAction.OnRetry) }
                            .padding(horizontal = 20.dp, vertical = 10.dp),
                    ) {
                        Text(stringResource(R.string.students_retry), color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            else -> Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Header card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        HeaderMetric(
                            modifier = Modifier.weight(1f),
                            label = stringResource(R.string.session_report_student),
                            value = uiState.studentName,
                        )
                        HeaderMetric(
                            modifier = Modifier.weight(1f),
                            label = stringResource(R.string.session_report_date),
                            value = uiState.date,
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        HeaderMetric(
                            modifier = Modifier.weight(1f),
                            label = stringResource(R.string.session_report_professional),
                            value = uiState.professional,
                        )
                        HeaderMetric(
                            modifier = Modifier.weight(1f),
                            label = stringResource(R.string.session_report_total_questions),
                            value = stringResource(R.string.session_report_questions_count, uiState.totalQuestions),
                        )
                    }
                }
            }

            // Relação de Tempos
            ReportCard(
                icon = Icons.Outlined.AccessTime,
                title = stringResource(R.string.session_report_time_title),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        TimeMetric(modifier = Modifier.weight(1f), label = stringResource(R.string.session_report_total_time),   value = uiState.totalTime,       valueColor = TextPrimary)
                        TimeMetric(modifier = Modifier.weight(1f), label = stringResource(R.string.session_report_avg_response), value = uiState.avgResponseTime, valueColor = TextPrimary)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        TimeMetric(modifier = Modifier.weight(1f), label = stringResource(R.string.session_report_avg_correct), value = uiState.avgCorrectTime, valueColor = Color(0xFF16A34A))
                        TimeMetric(modifier = Modifier.weight(1f), label = stringResource(R.string.session_report_avg_wrong),   value = uiState.avgWrongTime,   valueColor = Color(0xFFDC2626))
                    }
                }
            }

            // Taxa de Acerto por Categoria
            ReportCard(
                icon = Icons.Outlined.TrackChanges,
                title = stringResource(R.string.session_report_category_title),
            ) {
                uiState.categoryAccuracy.forEachIndexed { index, item ->
                    if (index > 0) Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary,
                        )
                        Text(
                            text = "${item.percent}%",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(item.colorHex),
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(50.dp))
                            .background(Color(0xFFE5E7EB)),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth((item.percent / 100f).coerceIn(0f, 1f))
                                .height(8.dp)
                                .clip(RoundedCornerShape(50.dp))
                                .background(Color(item.colorHex)),
                        )
                    }
                }
            }

            // Taxa de Acerto por Tipo de Atividade
            ReportCard(
                icon = Icons.AutoMirrored.Outlined.Assignment,
                title = stringResource(R.string.session_report_type_title),
            ) {
                val rows = uiState.typeAccuracy.chunked(2)
                rows.forEachIndexed { rowIdx, pair ->
                    if (rowIdx > 0) Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        pair.forEach { item ->
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.label,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Medium,
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFCBEAE6)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = "${item.percent}%",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TealPrimary,
                                    )
                                }
                            }
                        }
                        if (pair.size == 1) Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            // Relatório Descritivo
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.AutoMirrored.Outlined.Assignment,
                                contentDescription = null,
                                tint = TealPrimary,
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.session_report_descriptive_title),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                            )
                        }
                        Text(
                            text = stringResource(R.string.session_report_char_count, uiState.descriptiveReport.length),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = uiState.descriptiveReport,
                        onValueChange = { onAction(SessionReportAction.OnDescriptiveReportChange(it)) },
                        placeholder = {
                            Text(
                                stringResource(R.string.session_report_descriptive_placeholder),
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TealPrimary,
                            unfocusedBorderColor = InputBorder,
                            focusedContainerColor = InputBackground,
                            unfocusedContainerColor = InputBackground,
                        ),
                    )
                    uiState.savedMessage?.let { msg ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(msg, style = MaterialTheme.typography.bodySmall, color = Color(0xFF16A34A))
                    }
                    if (uiState.errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(uiState.errorMessage, style = MaterialTheme.typography.bodySmall, color = Color(0xFFDC2626))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Brush.horizontalGradient(listOf(TealDark, TealLight)))
                            .clickable(enabled = !uiState.isSaving) { onAction(SessionReportAction.OnSaveReport) },
                        contentAlignment = Alignment.Center,
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                        } else {
                            Text(
                                text = stringResource(R.string.session_report_save),
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
        }
    }
}

@Composable
private fun ReportCard(
    icon: ImageVector,
    title: String,
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
            }
            Spacer(modifier = Modifier.height(14.dp))
            content()
        }
    }
}

@Composable
private fun HeaderMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.Start) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = TextSecondary, fontSize = 10.sp)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = TextPrimary)
    }
}

@Composable
private fun TimeMetric(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.Start) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = TextSecondary, fontSize = 10.sp)
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = valueColor,
        )
    }
}

@Composable
private fun SessionReportBottomBar(
    onHomeClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    data class NavItem(val label: Int, val icon: ImageVector, val selected: Boolean, val onClick: () -> Unit)
    val items = listOf(
        NavItem(R.string.dashboard_tab_home,       Icons.Filled.Home,                     true,  onHomeClick),
        NavItem(R.string.dashboard_tab_activities, Icons.AutoMirrored.Outlined.MenuBook,  false, {}),
        NavItem(R.string.dashboard_tab_students,   Icons.Outlined.Person,                 false, {}),
        NavItem(R.string.dashboard_tab_reports,    Icons.AutoMirrored.Outlined.Assignment, false, {}),
    )

    NavigationBar(containerColor = Color.White, tonalElevation = 4.dp, modifier = modifier) {
        items.forEach { item ->
            NavigationBarItem(
                selected = item.selected,
                onClick = item.onClick,
                icon = { Icon(item.icon, contentDescription = null) },
                label = { Text(stringResource(item.label), style = MaterialTheme.typography.labelSmall) },
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
