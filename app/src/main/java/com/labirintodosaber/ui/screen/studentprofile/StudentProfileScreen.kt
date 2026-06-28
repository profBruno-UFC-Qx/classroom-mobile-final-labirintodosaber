package com.labirintodosaber.ui.screen.studentprofile

import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.labirintodosaber.R
import com.labirintodosaber.ui.theme.GradientBottom
import com.labirintodosaber.ui.theme.TealLight
import com.labirintodosaber.ui.theme.TealPrimary
import com.labirintodosaber.ui.theme.TextPrimary
import com.labirintodosaber.ui.theme.TextSecondary

@Composable
fun StudentProfileScreen(
    onBackClick: () -> Unit,
    onSessionClick: (String) -> Unit = {},
    onProfileClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: StudentProfileViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    StudentProfileContent(
        uiState = uiState,
        onAction = viewModel::onAction,
        onBackClick = onBackClick,
        onSessionClick = onSessionClick,
        onProfileClick = onProfileClick,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StudentProfileContent(
    uiState: StudentProfileUiState,
    onAction: (StudentProfileAction) -> Unit,
    onBackClick: () -> Unit,
    onSessionClick: (String) -> Unit,
    onProfileClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.students_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.students_back_desc),
                            tint = TextPrimary,
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onProfileClick) {
                        Icon(Icons.Outlined.AccountCircle, contentDescription = stringResource(R.string.dashboard_profile_desc), tint = TextPrimary, modifier = Modifier.size(28.dp))
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White),
            )
        },
        containerColor = GradientBottom,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            StudentInfoCard(uiState = uiState, onAction = onAction)

            ProfileTabRow(
                selectedTab = uiState.selectedTab,
                onTabSelect = { onAction(StudentProfileAction.OnTabSelect(it)) },
            )

            when (uiState.selectedTab) {
                StudentProfileTab.PROGRESS -> ProgressTabContent(uiState = uiState)
                StudentProfileTab.SESSIONS -> SessionsTabContent(sessions = uiState.sessions, onSessionClick = onSessionClick)
                StudentProfileTab.DOCUMENTS -> DocumentsTabContent(documents = uiState.documents)
                StudentProfileTab.ANAMNESE -> AnamneseTabContent(anamneses = uiState.anamneses)
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

// ── Student Info Card ─────────────────────────────────────────────────────────

@Composable
private fun StudentInfoCard(
    uiState: StudentProfileUiState,
    onAction: (StudentProfileAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor = Color(uiState.avatarBorderColorHex)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                val genericAvatar = painterResource(
                    if (uiState.isGirl) R.drawable.girl_home else R.drawable.boy_home,
                )
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .border(2.dp, borderColor, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    if (uiState.photoUrl != null) {
                        AsyncImage(
                            model = uiState.photoUrl,
                            contentDescription = stringResource(R.string.student_profile_avatar_desc),
                            placeholder = genericAvatar,
                            error = genericAvatar,
                            fallback = genericAvatar,
                            modifier = Modifier.size(52.dp).clip(CircleShape),
                            contentScale = ContentScale.Crop,
                        )
                    } else {
                        Image(
                            painter = genericAvatar,
                            contentDescription = stringResource(R.string.student_profile_avatar_desc),
                            modifier = Modifier.size(52.dp).clip(CircleShape),
                            contentScale = ContentScale.Crop,
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top,
                    ) {
                        Column {
                            Text(
                                text = uiState.name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                            )
                            Text(
                                text = "${uiState.age} anos • ${uiState.gender}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary,
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Surface(
                                shape = RoundedCornerShape(13.dp),
                                color = Color.White,
                                shadowElevation = 2.dp,
                                modifier = Modifier.border(1.dp, TealPrimary, RoundedCornerShape(13.dp)),
                            ) {
                                Text(
                                    text = stringResource(R.string.student_profile_generate_report),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 7.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TealPrimary,
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(13.dp),
                                color = TealPrimary,
                                shadowElevation = 2.dp,
                                modifier = Modifier
                                    .size(22.dp)
                                    .clickable { onAction(StudentProfileAction.OnEditClick) },
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Outlined.Edit,
                                        contentDescription = stringResource(R.string.student_profile_edit_desc),
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp),
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    InfoRow(label = stringResource(R.string.student_profile_birth_date_label), value = uiState.birthDate)
                    Spacer(modifier = Modifier.height(4.dp))
                    InfoRow(label = stringResource(R.string.student_profile_address_label), value = uiState.address)
                    Spacer(modifier = Modifier.height(4.dp))
                    InfoRow(label = stringResource(R.string.student_profile_objective_label), value = uiState.objective)
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = TextSecondary)
        Text(text = value, style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = TextPrimary)
    }
}

// ── Profile Tab Row ───────────────────────────────────────────────────────────

@Composable
private fun ProfileTabRow(
    selectedTab: StudentProfileTab,
    onTabSelect: (StudentProfileTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tabs = listOf(
        StudentProfileTab.PROGRESS to stringResource(R.string.student_profile_tab_progress),
        StudentProfileTab.SESSIONS to stringResource(R.string.student_profile_tab_sessions),
        StudentProfileTab.DOCUMENTS to stringResource(R.string.student_profile_tab_documents),
        StudentProfileTab.ANAMNESE to stringResource(R.string.student_profile_tab_anamnese),
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 3.dp),
        ) {
            tabs.forEach { (tab, label) ->
                val isSelected = selectedTab == tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onTabSelect(tab) }
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) TealPrimary else TextPrimary,
                    )
                }
            }
        }
    }
}

// ── Progress Tab Content ──────────────────────────────────────────────────────

@Composable
private fun ProgressTabContent(
    uiState: StudentProfileUiState,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.student_profile_progress_category_title),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
            )
            Spacer(modifier = Modifier.height(16.dp))
            uiState.categoryProgress.forEachIndexed { index, category ->
                CategoryProgressRow(category = category)
                if (index < uiState.categoryProgress.lastIndex) {
                    Spacer(modifier = Modifier.height(14.dp))
                }
            }
        }
    }
}

@Composable
private fun CategoryProgressRow(
    category: CategoryProgress,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = category.name,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                fontSize = 9.sp,
            )
            Text(
                text = "${category.percent}%",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                fontSize = 8.sp,
            )
        }
        Spacer(modifier = Modifier.height(5.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(7.dp)
                .clip(RoundedCornerShape(50.dp))
                .background(Color(0xFFF3F4F6)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(category.percent / 100f)
                    .fillMaxSize()
                    .background(TealPrimary),
            )
        }
    }
}

// ── Sessões ───────────────────────────────────────────────────────────────────

@Composable
private fun SessionsTabContent(
    sessions: List<SessionRow>,
    onSessionClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    TabCard(modifier = modifier) {
        if (sessions.isEmpty()) {
            EmptyTabMessage(text = stringResource(R.string.student_profile_sessions_empty))
        } else {
            sessions.forEachIndexed { index, session ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSessionClick(session.id) },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = session.name,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 10.sp,
                        )
                        Text(
                            text = if (session.finished) {
                                stringResource(R.string.student_profile_session_finished, session.date, session.questionCount)
                            } else {
                                stringResource(R.string.student_profile_session_ongoing, session.date)
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            fontSize = 8.sp,
                        )
                    }
                    Text(
                        text = "${session.hitRatePercent}%",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TealPrimary,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp),
                    )
                }
                if (index < sessions.lastIndex) {
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

// ── Documentos (relatórios gerados) ───────────────────────────────────────────

@Composable
private fun DocumentsTabContent(
    documents: List<DocumentRow>,
    modifier: Modifier = Modifier,
) {
    TabCard(modifier = modifier) {
        if (documents.isEmpty()) {
            EmptyTabMessage(text = stringResource(R.string.student_profile_documents_empty))
        } else {
            documents.forEachIndexed { index, document ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = document.title,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 10.sp,
                        )
                        Text(
                            text = document.period,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            fontSize = 8.sp,
                        )
                    }
                    Text(
                        text = "${document.accuracyPercent}%",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TealPrimary,
                    )
                }
                if (index < documents.lastIndex) {
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

// ── Anamneses ─────────────────────────────────────────────────────────────────

@Composable
private fun AnamneseTabContent(
    anamneses: List<AnamneseRow>,
    modifier: Modifier = Modifier,
) {
    TabCard(modifier = modifier) {
        if (anamneses.isEmpty()) {
            EmptyTabMessage(text = stringResource(R.string.student_profile_anamnese_empty))
        } else {
            anamneses.forEachIndexed { index, anamnese ->
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = anamnese.title,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 10.sp,
                    )
                    Text(
                        text = stringResource(R.string.student_profile_anamnese_answered_at, anamnese.date),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        fontSize = 8.sp,
                    )
                }
                if (index < anamneses.lastIndex) {
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
private fun TabCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

@Composable
private fun EmptyTabMessage(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(88.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = text, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
    }
}
