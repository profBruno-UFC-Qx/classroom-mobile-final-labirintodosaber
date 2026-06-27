package com.labirintodosaber.ui.screen.sessionconfigure

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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.labirintodosaber.R
import com.labirintodosaber.ui.screen.sessionselectstudent.PaginationRow
import com.labirintodosaber.ui.screen.sessionselectstudent.SessionActionButtonsRow
import com.labirintodosaber.ui.screen.sessionselectstudent.SessionBottomBar
import com.labirintodosaber.ui.screen.sessionselectstudent.SessionTopBar
import com.labirintodosaber.ui.theme.InputBackground
import com.labirintodosaber.ui.theme.InputBorder
import com.labirintodosaber.ui.theme.TealPrimary
import com.labirintodosaber.ui.theme.TextPrimary
import com.labirintodosaber.ui.theme.TextSecondary

@Composable
fun SessionConfigureScreen(
    onBack: () -> Unit,
    onStartSession: (studentId: String, contentIds: String, sessionName: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SessionConfigureViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                SessionConfigureEvent.NavigateBack -> onBack()
                is SessionConfigureEvent.NavigateToRun -> onStartSession(
                    event.studentId,
                    event.contentIds,
                    event.sessionName,
                )
            }
        }
    }

    SessionConfigureContent(
        uiState = uiState,
        onAction = viewModel::onAction,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SessionConfigureContent(
    uiState: SessionConfigureUiState,
    onAction: (SessionConfigureAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = { SessionTopBar() },
        bottomBar = {
            Column {
                SessionActionButtonsRow(
                    confirmLabel = stringResource(R.string.session_start_button),
                    confirmEnabled = uiState.canStart,
                    onBack = { onAction(SessionConfigureAction.OnBack) },
                    onConfirm = { onAction(SessionConfigureAction.OnStartSession) },
                )
                SessionBottomBar()
            }
        },
        containerColor = Color.White,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = buildAnnotatedString {
                    append(stringResource(R.string.session_name_label))
                    withStyle(SpanStyle(color = Color(0xFFE53935))) { append(" *") }
                },
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.sessionName,
                onValueChange = { onAction(SessionConfigureAction.OnSessionNameChange(it)) },
                placeholder = {
                    Text(
                        text = stringResource(R.string.session_name_placeholder),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                    )
                },
                isError = uiState.sessionNameError,
                supportingText = if (uiState.sessionNameError) {
                    { Text("Campo obrigatório", color = Color(0xFFE53935)) }
                } else null,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TealPrimary,
                    unfocusedBorderColor = InputBorder,
                    errorBorderColor = Color(0xFFE53935),
                    focusedContainerColor = InputBackground,
                    unfocusedContainerColor = InputBackground,
                    errorContainerColor = Color(0xFFFFF0F0),
                ),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = stringResource(R.string.session_content_hint),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.contentQuery,
                onValueChange = { onAction(SessionConfigureAction.OnContentQueryChange(it)) },
                placeholder = {
                    Text(
                        text = stringResource(R.string.session_search_content_placeholder),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = null,
                        tint = TextSecondary,
                    )
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

            Spacer(modifier = Modifier.height(12.dp))

            ContentFilterRow(
                activeFilter = uiState.activeFilter,
                onFilterChange = { onAction(SessionConfigureAction.OnFilterChange(it)) },
            )

            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                uiState.visibleContent.forEach { item ->
                    ContentItemRow(
                        item = item,
                        isSelected = item.id in uiState.selectedContentIds,
                        onClick = { onAction(SessionConfigureAction.OnContentSelect(item.id)) },
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            PaginationRow(
                currentPage = uiState.currentPage,
                totalPages = uiState.totalPages,
                onPageChange = { onAction(SessionConfigureAction.OnPageChange(it)) },
            )

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ContentFilterRow(
    activeFilter: ContentFilter,
    onFilterChange: (ContentFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    data class FilterOption(val filter: ContentFilter, val label: String)

    val options = listOf(
        FilterOption(ContentFilter.ALL, stringResource(R.string.session_filter_all)),
        FilterOption(ContentFilter.NOTEBOOKS, stringResource(R.string.session_filter_notebooks)),
        FilterOption(ContentFilter.GROUPS, stringResource(R.string.session_filter_groups)),
        FilterOption(ContentFilter.ACTIVITIES, stringResource(R.string.session_filter_activities)),
    )

    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        options.forEach { option ->
            val isSelected = activeFilter == option.filter
            Surface(
                onClick = { onFilterChange(option.filter) },
                shape = RoundedCornerShape(50.dp),
                color = if (isSelected) TealPrimary else Color.White,
                shadowElevation = if (isSelected) 0.dp else 1.dp,
                modifier = Modifier.border(
                    width = if (isSelected) 0.dp else 1.dp,
                    color = if (isSelected) Color.Transparent else InputBorder,
                    shape = RoundedCornerShape(50.dp),
                ),
            ) {
                Text(
                    text = option.label,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSelected) Color.White else TextSecondary,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                )
            }
        }
    }
}

@Composable
private fun ContentItemRow(
    item: SessionContentItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val icon: ImageVector = when (item.type) {
        ContentType.NOTEBOOK -> Icons.AutoMirrored.Outlined.MenuBook
        ContentType.GROUP -> Icons.Outlined.Folder
        ContentType.ACTIVITY -> Icons.AutoMirrored.Outlined.Assignment
    }

    val borderColor = if (isSelected) TealPrimary else InputBorder

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(1.5.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFD8F5F3)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TealPrimary,
                modifier = Modifier.size(24.dp),
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
            )
            Text(
                text = item.description,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                maxLines = 2,
            )
            if (item.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    item.tags.forEach { tag ->
                        ContentTag(label = tag)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Icon(
            imageVector = if (isSelected) Icons.Outlined.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (isSelected) TealPrimary else TextSecondary,
            modifier = Modifier.size(22.dp),
        )
    }
}

@Composable
private fun ContentTag(
    label: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50.dp),
        color = Color.White,
        shadowElevation = 1.dp,
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            fontSize = 10.sp,
            color = TextSecondary,
        )
    }
}
