package com.labirintodosaber.ui.screen.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.labirintodosaber.R
import com.labirintodosaber.ui.theme.TealLight
import com.labirintodosaber.ui.theme.TealPrimary
import com.labirintodosaber.ui.theme.TextPrimary
import com.labirintodosaber.ui.theme.TextSecondary

@Composable
fun DashboardScreen(
    onStudentsClick: () -> Unit = {},
    onActivitiesClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Recarrega ao entrar e ao retomar a tela (ex.: após cadastrar aluno).
    LifecycleResumeEffect(Unit) {
        viewModel.refresh()
        onPauseOrDispose { }
    }

    DashboardContent(
        uiState = uiState,
        onAction = viewModel::onAction,
        onStudentsClick = onStudentsClick,
        onActivitiesClick = onActivitiesClick,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardContent(
    uiState: DashboardUiState,
    onAction: (DashboardAction) -> Unit,
    onStudentsClick: () -> Unit,
    onActivitiesClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.dashboard_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { /* TODO: abrir drawer */ }) {
                        Icon(
                            imageVector = Icons.Outlined.Menu,
                            contentDescription = stringResource(R.string.dashboard_menu_desc),
                            tint = TextPrimary,
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: abrir perfil */ }) {
                        Icon(
                            imageVector = Icons.Outlined.AccountCircle,
                            contentDescription = stringResource(R.string.dashboard_profile_desc),
                            tint = TextPrimary,
                            modifier = Modifier.size(28.dp),
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White,
                ),
            )
        },
        bottomBar = {
            DashboardBottomBar(
                selectedTab = uiState.selectedTab,
                onTabSelect = { onAction(DashboardAction.OnTabSelect(it)) },
                onStudentsClick = onStudentsClick,
                onActivitiesClick = onActivitiesClick,
            )
        },
        containerColor = Color.White,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            WelcomeBanner(
                userName = uiState.userName,
                sessionCount = uiState.todaySessionCount,
                onStartSession = { onAction(DashboardAction.OnStartSession) },
                modifier = Modifier.padding(horizontal = 16.dp),
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = stringResource(R.string.dashboard_today_sessions),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.padding(horizontal = 16.dp),
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (uiState.todaySessions.isEmpty()) {
                Text(
                    text = stringResource(R.string.dashboard_no_sessions_today),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            } else {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(uiState.todaySessions) { session ->
                        SessionCard(
                            session = session,
                            onClick = { onAction(DashboardAction.OnSessionClick(session.id)) },
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            PastSessionsCard(
                pastSessions = uiState.pastSessions,
                activities = uiState.recentActivities,
                onSeeAll = { onAction(DashboardAction.OnSeeAllSessionsClick) },
                onPastSessionClick = { onAction(DashboardAction.OnPastSessionClick(it)) },
                onActivityClick = { onAction(DashboardAction.OnActivityClick(it)) },
                modifier = Modifier.padding(horizontal = 16.dp),
            )

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

// ── Welcome Banner ────────────────────────────────────────────────────────────

@Composable
private fun WelcomeBanner(
    userName: String,
    sessionCount: Int,
    onStartSession: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = TealPrimary),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.dashboard_greeting, userName),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(R.string.dashboard_sessions_subtitle, sessionCount),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.9f),
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Surface(
                onClick = onStartSession,
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 2.dp,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.PlayArrow,
                        contentDescription = null,
                        tint = TealPrimary,
                        modifier = Modifier.size(14.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.dashboard_start_session),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TealPrimary,
                    )
                }
            }
        }
    }
}

// ── Session Card (Sessões de hoje) ────────────────────────────────────────────

@Composable
private fun SessionCard(
    session: SessionItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor = Color(session.borderColorHex)

    Row(
        modifier = modifier
            .width(220.dp)
            .height(IntrinsicSize.Min)
            .clip(RoundedCornerShape(13.dp))
            .background(Color(0xFFF6F8F8))
            .clickable { onClick() },
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .fillMaxHeight()
                .background(borderColor),
        )
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(
                        if (session.isGirl) R.drawable.girl_home else R.drawable.boy_home,
                    ),
                    contentDescription = stringResource(R.string.dashboard_student_avatar_desc),
                    modifier = Modifier
                        .size(33.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = session.studentName,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.AccessTime,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(10.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = session.time,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.GridView,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(10.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = session.category,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = session.description,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

// ── Past Sessions + Activities Card ───────────────────────────────────────────

@Composable
private fun PastSessionsCard(
    pastSessions: List<PastSessionItem>,
    activities: List<ActivityItem>,
    onSeeAll: () -> Unit,
    onPastSessionClick: (String) -> Unit,
    onActivityClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.dashboard_recent_sessions),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                )
                Text(
                    text = stringResource(R.string.dashboard_see_all),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = TealPrimary,
                    modifier = Modifier.clickable { onSeeAll() },
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (pastSessions.isEmpty()) {
                Text(
                    text = stringResource(R.string.dashboard_no_recent_sessions),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
            } else {
                pastSessions.forEachIndexed { index, session ->
                    PastSessionRow(
                        session = session,
                        onClick = { onPastSessionClick(session.id) },
                    )
                    if (index < pastSessions.lastIndex) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = stringResource(R.string.dashboard_recent_activities),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                activities.forEach { activity ->
                    ActivityCard(
                        activity = activity,
                        onClick = { onActivityClick(activity.id) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun PastSessionRow(
    session: PastSessionItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(Color(0xFFF6F8F8), Color(0xFFFAFBFB), Color.White),
                ),
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(
                if (session.isGirl) R.drawable.girl_home else R.drawable.boy_home,
            ),
            contentDescription = stringResource(R.string.dashboard_student_avatar_desc),
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop,
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = session.studentName,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.CalendarToday,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(8.dp),
                )
                Text(
                    text = session.date,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.sp,
                    color = TextSecondary,
                )
                Icon(
                    imageVector = Icons.Outlined.AccessTime,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(8.dp),
                )
                Text(
                    text = "${session.time} • ${session.duration}",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.sp,
                    color = TextSecondary,
                )
                TagChip(label = session.category)
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = stringResource(R.string.dashboard_hit_rate_label),
                style = MaterialTheme.typography.labelSmall,
                fontSize = 7.sp,
                color = TextSecondary,
            )
            Text(
                text = "${session.hitRatePercent}%",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = TealPrimary,
            )
        }

        Spacer(modifier = Modifier.width(4.dp))

        Icon(
            imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
            contentDescription = stringResource(R.string.dashboard_session_detail_desc),
            tint = TextSecondary,
            modifier = Modifier.size(16.dp),
        )
    }
}

// ── Activity Card ─────────────────────────────────────────────────────────────

@Composable
private fun ActivityCard(
    activity: ActivityItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bgColor = Color(activity.backgroundColorHex)
    val iconColor = Color(activity.iconColorHex)
    val icon = when (activity.iconType) {
        ActivityIconType.BOOK -> Icons.AutoMirrored.Outlined.MenuBook
        ActivityIconType.CALCULATE -> Icons.Outlined.Calculate
        ActivityIconType.EDIT -> Icons.Outlined.Edit
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .clickable { onClick() }
            .padding(10.dp),
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color.White,
            shadowElevation = 1.dp,
            modifier = Modifier.size(26.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(14.dp),
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = activity.name,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            fontSize = 9.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = activity.description,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
            fontSize = 7.sp,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(8.dp))
        activity.tags.forEach { tag ->
            TagChip(label = tag.label)
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
private fun TagChip(
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
            fontSize = 6.sp,
            color = TextSecondary,
        )
    }
}

// ── Bottom Navigation ─────────────────────────────────────────────────────────

@Composable
private fun DashboardBottomBar(
    selectedTab: DashboardTab,
    onTabSelect: (DashboardTab) -> Unit,
    onStudentsClick: () -> Unit,
    onActivitiesClick: () -> Unit,
) {
    data class TabItem(val tab: DashboardTab, val label: Int, val icon: ImageVector, val iconFilled: ImageVector)

    val tabs = listOf(
        TabItem(DashboardTab.HOME, R.string.dashboard_tab_home, Icons.Outlined.Home, Icons.Filled.Home),
        TabItem(DashboardTab.ACTIVITIES, R.string.dashboard_tab_activities, Icons.AutoMirrored.Outlined.MenuBook, Icons.AutoMirrored.Outlined.MenuBook),
        TabItem(DashboardTab.STUDENTS, R.string.dashboard_tab_students, Icons.Outlined.Person, Icons.Outlined.Person),
        TabItem(DashboardTab.REPORTS, R.string.dashboard_tab_reports, Icons.AutoMirrored.Outlined.Assignment, Icons.AutoMirrored.Outlined.Assignment),
    )

    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 4.dp,
    ) {
        tabs.forEach { item ->
            val selected = selectedTab == item.tab
            NavigationBarItem(
                selected = selected,
                onClick = {
                    when (item.tab) {
                        DashboardTab.STUDENTS -> onStudentsClick()
                        DashboardTab.ACTIVITIES -> onActivitiesClick()
                        else -> onTabSelect(item.tab)
                    }
                },
                icon = {
                    Icon(
                        imageVector = if (selected) item.iconFilled else item.icon,
                        contentDescription = null,
                    )
                },
                label = {
                    Text(
                        text = stringResource(item.label),
                        style = MaterialTheme.typography.labelSmall,
                    )
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
