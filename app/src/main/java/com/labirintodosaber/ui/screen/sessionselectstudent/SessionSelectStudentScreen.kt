package com.labirintodosaber.ui.screen.sessionselectstudent

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
fun SessionSelectStudentScreen(
    onBack: () -> Unit,
    onNextStep: (studentId: String) -> Unit,
    onMenuClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: SessionSelectStudentViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                SessionSelectStudentEvent.NavigateBack -> onBack()
                is SessionSelectStudentEvent.NavigateToConfigure -> onNextStep(event.studentId)
            }
        }
    }

    SessionSelectStudentContent(
        uiState = uiState,
        onAction = viewModel::onAction,
        onMenuClick = onMenuClick,
        onProfileClick = onProfileClick,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SessionSelectStudentContent(
    uiState: SessionSelectStudentUiState,
    onAction: (SessionSelectStudentAction) -> Unit,
    onMenuClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = { SessionTopBar(onMenuClick = onMenuClick, onProfileClick = onProfileClick) },
        bottomBar = {
            Column {
                SessionActionButtonsRow(
                    confirmLabel = stringResource(R.string.session_next_step_button),
                    confirmEnabled = uiState.canProceed,
                    onBack = { onAction(SessionSelectStudentAction.OnBack) },
                    onConfirm = { onAction(SessionSelectStudentAction.OnNextStep) },
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
                text = stringResource(R.string.session_select_student_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = uiState.query,
                onValueChange = { onAction(SessionSelectStudentAction.OnQueryChange(it)) },
                placeholder = {
                    Text(
                        text = stringResource(R.string.session_search_student_placeholder),
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

            Spacer(modifier = Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                uiState.visibleStudents.forEach { student ->
                    StudentSelectRow(
                        student = student,
                        isSelected = uiState.selectedStudentId == student.id,
                        onClick = { onAction(SessionSelectStudentAction.OnStudentSelect(student.id)) },
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            PaginationRow(
                currentPage = uiState.currentPage,
                totalPages = uiState.totalPages,
                onPageChange = { onAction(SessionSelectStudentAction.OnPageChange(it)) },
            )

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun StudentSelectRow(
    student: SessionStudentItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderStroke = if (isSelected) BorderStroke(1.5.dp, TealPrimary) else BorderStroke(1.dp, InputBorder)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = borderStroke,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(if (student.isGirl) R.drawable.girl_home else R.drawable.boy_home),
                contentDescription = stringResource(R.string.session_student_avatar_desc),
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = student.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                )
                Text(
                    text = "${student.age} anos • ${student.gender}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                )
                Text(
                    text = student.level,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    tint = TealPrimary,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@Composable
internal fun PaginationRow(
    currentPage: Int,
    totalPages: Int,
    onPageChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = { if (currentPage > 0) onPageChange(currentPage - 1) },
            enabled = currentPage > 0,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowLeft,
                contentDescription = null,
                tint = if (currentPage > 0) TextPrimary else TextSecondary,
            )
        }

        (0 until totalPages).forEach { pageIndex ->
            val isCurrentPage = currentPage == pageIndex
            Text(
                text = "${pageIndex + 1}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isCurrentPage) FontWeight.Bold else FontWeight.Normal,
                color = if (isCurrentPage) TealPrimary else TextSecondary,
                modifier = Modifier
                    .padding(horizontal = 6.dp)
                    .clickable { onPageChange(pageIndex) },
            )
        }

        IconButton(
            onClick = { if (currentPage < totalPages - 1) onPageChange(currentPage + 1) },
            enabled = currentPage < totalPages - 1,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = null,
                tint = if (currentPage < totalPages - 1) TextPrimary else TextSecondary,
            )
        }
    }
}

@Composable
internal fun SessionActionButtonsRow(
    confirmLabel: String,
    confirmEnabled: Boolean,
    onBack: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            onClick = onBack,
            shape = RoundedCornerShape(50.dp),
            color = Color.White,
            shadowElevation = 2.dp,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = null,
                    tint = TextPrimary,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.session_back_button),
                    style = MaterialTheme.typography.labelMedium,
                    color = TextPrimary,
                )
            }
        }

        val confirmModifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                brush = if (confirmEnabled) {
                    Brush.horizontalGradient(colors = listOf(TealDark, TealLight))
                } else {
                    Brush.horizontalGradient(colors = listOf(TealDark.copy(alpha = 0.4f), TealLight.copy(alpha = 0.4f)))
                },
            )
            .then(if (confirmEnabled) Modifier.clickable { onConfirm() } else Modifier)
            .padding(horizontal = 20.dp, vertical = 12.dp)

        Row(
            modifier = confirmModifier,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = confirmLabel,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
            )
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                imageVector = Icons.Outlined.PlayArrow,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(14.dp),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SessionTopBar(
    modifier: Modifier = Modifier,
    onMenuClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
) {
    CenterAlignedTopAppBar(
        modifier = modifier,
        title = {
            Text(
                text = stringResource(R.string.session_topbar_title),
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
            )
        },
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(
                    imageVector = Icons.Outlined.Menu,
                    contentDescription = stringResource(R.string.session_menu_desc),
                    tint = TextPrimary,
                )
            }
        },
        actions = {
            IconButton(onClick = onProfileClick) {
                Icon(
                    imageVector = Icons.Outlined.AccountCircle,
                    contentDescription = stringResource(R.string.session_profile_desc),
                    tint = TextPrimary,
                    modifier = Modifier.size(28.dp),
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White),
    )
}

@Composable
internal fun SessionBottomBar(modifier: Modifier = Modifier) {
    NavigationBar(
        modifier = modifier,
        containerColor = Color.White,
        tonalElevation = 4.dp,
    ) {
        NavigationBarItem(
            selected = true,
            onClick = { },
            icon = { Icon(Icons.Filled.Home, contentDescription = null) },
            label = { Text(stringResource(R.string.dashboard_tab_home), style = MaterialTheme.typography.labelSmall) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = TealPrimary,
                selectedTextColor = TealPrimary,
                unselectedIconColor = TextSecondary,
                unselectedTextColor = TextSecondary,
                indicatorColor = Color.Transparent,
            ),
        )
        NavigationBarItem(
            selected = false,
            onClick = { },
            icon = { Icon(Icons.AutoMirrored.Outlined.MenuBook, contentDescription = null) },
            label = { Text(stringResource(R.string.dashboard_tab_activities), style = MaterialTheme.typography.labelSmall) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = TealPrimary,
                selectedTextColor = TealPrimary,
                unselectedIconColor = TextSecondary,
                unselectedTextColor = TextSecondary,
                indicatorColor = Color.Transparent,
            ),
        )
        NavigationBarItem(
            selected = false,
            onClick = { },
            icon = { Icon(Icons.Outlined.Person, contentDescription = null) },
            label = { Text(stringResource(R.string.dashboard_tab_students), style = MaterialTheme.typography.labelSmall) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = TealPrimary,
                selectedTextColor = TealPrimary,
                unselectedIconColor = TextSecondary,
                unselectedTextColor = TextSecondary,
                indicatorColor = Color.Transparent,
            ),
        )
        NavigationBarItem(
            selected = false,
            onClick = { },
            icon = { Icon(Icons.AutoMirrored.Outlined.Assignment, contentDescription = null) },
            label = { Text(stringResource(R.string.dashboard_tab_reports), style = MaterialTheme.typography.labelSmall) },
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
