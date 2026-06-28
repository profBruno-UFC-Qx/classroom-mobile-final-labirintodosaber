package com.labirintodosaber.ui.screen.sessionrun

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.outlined.ZoomIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import coil.compose.AsyncImage
import com.labirintodosaber.R
import com.labirintodosaber.ui.screen.sessionselectstudent.SessionBottomBar
import com.labirintodosaber.ui.screen.sessionselectstudent.SessionTopBar
import com.labirintodosaber.ui.theme.TealDark
import com.labirintodosaber.ui.theme.TealLight
import com.labirintodosaber.ui.theme.TealPrimary

@Composable
fun SessionRunScreen(
    onBack: () -> Unit,
    onFinishSession: (String) -> Unit = { onBack() },
    onMenuClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: SessionRunViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.navigateSessionId) {
        uiState.navigateSessionId?.let { onFinishSession(it) }
    }

    SessionRunContent(
        uiState = uiState,
        onAction = viewModel::onAction,
        onFinish = { viewModel.onAction(SessionRunAction.OnFinishSession) },
        onMenuClick = onMenuClick,
        onProfileClick = onProfileClick,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SessionRunContent(
    uiState: SessionRunUiState,
    onAction: (SessionRunAction) -> Unit,
    onFinish: () -> Unit,
    onMenuClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = { SessionTopBar(onMenuClick = onMenuClick, onProfileClick = onProfileClick) },
        bottomBar = {
            Column {
                if (uiState.isFinished) {
                    FinishSessionButton(onClick = onFinish)
                } else {
                    ConfirmAnswerButton(
                        enabled = uiState.canConfirm,
                        onClick = { onAction(SessionRunAction.OnConfirmAnswer) },
                    )
                }
                SessionBottomBar()
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
    ) { padding ->
        when {
            uiState.isLoading -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = TealPrimary)
            }

            uiState.errorMessage != null -> Box(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 32.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(uiState.errorMessage, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { onAction(SessionRunAction.OnRetryLoad) },
                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                    ) {
                        Text(stringResource(R.string.students_retry), color = Color.White)
                    }
                }
            }

            uiState.isFinished -> SessionFinishedContent(modifier = Modifier.padding(padding))

            else -> {
            val task = uiState.currentTask
            if (task != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState()),
                ) {
                    TimerRow(
                        timerLabel = uiState.timerLabel,
                        isRunning = uiState.isTimerRunning,
                        onToggle = { onAction(SessionRunAction.OnToggleTimer) },
                    )

                    if (task.imageUrl != null) {
                        TaskImageCard(
                            imageUrl = task.imageUrl,
                            modifier = Modifier.padding(horizontal = 16.dp),
                            onZoomClick = { onAction(SessionRunAction.OnZoomImage) },
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    if (task.hasAudio && task.audioDurationLabel != null) {
                        AudioPlayerBar(
                            isPlaying = uiState.isAudioPlaying,
                            durationLabel = task.audioDurationLabel,
                            onToggle = { onAction(SessionRunAction.OnToggleAudio) },
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    Text(
                        text = task.prompt,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        task.alternatives.forEach { alternative ->
                            AlternativeCard(
                                alternative = alternative,
                                isSelected = uiState.selectedAlternativeId == alternative.id,
                                onSelect = { onAction(SessionRunAction.OnAlternativeSelect(alternative.id)) },
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
            }
        }
    }

    val task = uiState.currentTask
    if (uiState.showImageZoom && task?.imageUrl != null) {
        ImageZoomDialog(
            imageUrl = task.imageUrl,
            onDismiss = { onAction(SessionRunAction.OnDismissZoom) },
        )
    }

    when (uiState.answerResult) {
        AnswerResult.CORRECT -> CorrectAnswerDialog(
            onNextActivity = { onAction(SessionRunAction.OnNextActivity) },
        )
        AnswerResult.WRONG -> WrongAnswerDialog(
            onNextActivity = { onAction(SessionRunAction.OnNextActivity) },
            onRetry = { onAction(SessionRunAction.OnRetry) },
        )
        null -> Unit
    }
}

@Composable
private fun TimerRow(
    timerLabel: String,
    isRunning: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = timerLabel,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.width(6.dp))
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(50.dp))
                .background(Color(0xFFF5E6C8))
                .clickable { onToggle() },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = if (isRunning) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                contentDescription = stringResource(
                    if (isRunning) R.string.session_timer_pause_desc else R.string.session_timer_resume_desc
                ),
                tint = Color(0xFF1A1A1A),
                modifier = Modifier.size(14.dp),
            )
        }
    }
}

@Composable
private fun TaskImageCard(
    imageUrl: String,
    onZoomClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFF8FAFA))
            .aspectRatio(1.4f),
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(8.dp)
                .size(32.dp)
                .clip(RoundedCornerShape(50.dp))
                .background(MaterialTheme.colorScheme.surface)
                .clickable { onZoomClick() },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.ZoomIn,
                contentDescription = stringResource(R.string.session_zoom_image_desc),
                tint = TealPrimary,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
private fun AudioPlayerBar(
    isPlaying: Boolean,
    durationLabel: String,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50.dp))
            .background(TealPrimary)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = onToggle,
            modifier = Modifier.size(24.dp),
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Outlined.Pause else Icons.AutoMirrored.Outlined.VolumeUp,
                contentDescription = stringResource(R.string.session_audio_play_desc),
                tint = Color.White,
                modifier = Modifier.size(20.dp),
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        AudioWaveformView(modifier = Modifier.weight(1f))

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = durationLabel,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
        )
    }
}

@Composable
private fun AudioWaveformView(modifier: Modifier = Modifier) {
    val heights = listOf(4, 8, 14, 10, 16, 12, 8, 14, 6, 10, 16, 8, 12, 6, 10)

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        heights.forEach { h ->
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(h.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White.copy(alpha = 0.8f)),
            )
        }
    }
}

@Composable
private fun AlternativeCard(
    alternative: SessionAlternative,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor = if (isSelected) TealPrimary else Color(0xFFE5E7EB)
    val bgColor = if (isSelected) Color(0xFFD8F5F3) else MaterialTheme.colorScheme.surface

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp),
            )
            .clickable { onSelect() }
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = alternative.text,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (isSelected) TealPrimary else MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun ConfirmAnswerButton(
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    brush = if (enabled) {
                        Brush.horizontalGradient(listOf(TealDark, TealLight))
                    } else {
                        Brush.horizontalGradient(
                            listOf(TealDark.copy(alpha = 0.4f), TealLight.copy(alpha = 0.4f))
                        )
                    },
                )
                .then(if (enabled) Modifier.clickable { onClick() } else Modifier),
            contentAlignment = Alignment.Center,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.session_confirm_answer_button),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                )
            }
        }
    }
}

@Composable
private fun ImageZoomDialog(
    imageUrl: String,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.Black,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
        icon = null,
        title = null,
        text = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                contentAlignment = Alignment.Center,
            ) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        },
        confirmButton = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Fechar",
                    color = Color.White,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onDismiss() }
                        .padding(horizontal = 24.dp, vertical = 10.dp),
                )
            }
        },
    )
}

@Composable
private fun FinishSessionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Brush.horizontalGradient(listOf(TealDark, TealLight)))
                .clickable { onClick() },
            contentAlignment = Alignment.Center,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.session_finish_button),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                )
            }
        }
    }
}

@Composable
private fun CorrectAnswerDialog(
    onNextActivity: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onNextActivity,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp),
        icon = {
            Icon(
                imageVector = Icons.Outlined.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(56.dp),
            )
        },
        title = {
            Text(
                text = stringResource(R.string.session_correct_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4CAF50),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        text = {
            Text(
                text = stringResource(R.string.session_correct_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Brush.horizontalGradient(listOf(TealDark, TealLight)))
                    .clickable { onNextActivity() },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.session_next_activity_button) + " →",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                )
            }
        },
    )
}

@Composable
private fun WrongAnswerDialog(
    onNextActivity: () -> Unit,
    onRetry: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onRetry,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp),
        icon = {
            Icon(
                imageVector = Icons.Outlined.Cancel,
                contentDescription = null,
                tint = Color(0xFFF44336),
                modifier = Modifier.size(56.dp),
            )
        },
        title = {
            Text(
                text = stringResource(R.string.session_wrong_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFF44336),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        text = {
            Text(
                text = stringResource(R.string.session_wrong_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Brush.horizontalGradient(listOf(TealDark, TealLight)))
                        .clickable { onNextActivity() },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.session_next_activity_button) + " →",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                    )
                }
                OutlinedButton(
                    onClick = onRetry,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TealPrimary),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Refresh,
                        contentDescription = null,
                        tint = TealPrimary,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.session_retry_button),
                        color = TealPrimary,
                    )
                }
            }
        },
    )
}

@Composable
private fun SessionFinishedContent(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.CheckCircle,
                contentDescription = null,
                tint = TealPrimary,
                modifier = Modifier.size(72.dp),
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.session_finished),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.session_finished_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}
