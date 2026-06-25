package com.labirintodosaber.ui.screen.activityanswer

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.labirintodosaber.R
import com.labirintodosaber.ui.theme.TealDark
import com.labirintodosaber.ui.theme.TealLight
import com.labirintodosaber.ui.theme.TealPrimary
import com.labirintodosaber.ui.theme.TextPrimary
import com.labirintodosaber.ui.theme.TextSecondary

private val AlternativeLabels = listOf("A", "B", "C", "D", "E", "F")

@Composable
fun ActivityAnswerScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ActivityAnswerViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ActivityAnswerContent(
        uiState = uiState,
        onBackClick = onBackClick,
        onRetry = viewModel::onRetry,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActivityAnswerContent(
    uiState: ActivityAnswerUiState,
    onBackClick: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedIndex by rememberSaveable { mutableStateOf<Int?>(null) }
    var confirmed by rememberSaveable { mutableStateOf(false) }
    val answered = confirmed && selectedIndex != null

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.categoryLabel.ifBlank { "Atividade" },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = stringResource(R.string.back_button), tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
            )
        },
        containerColor = Color(0xFFF5F5F5),
    ) { padding ->
        when {
            uiState.isLoading -> Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = TealPrimary)
            }

            uiState.errorMessage != null -> Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(uiState.errorMessage, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .background(TealPrimary)
                        .clickable { onRetry() }
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                ) {
                    Text(stringResource(R.string.students_retry), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = Color.White)
                }
            }

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White)
                            .padding(20.dp),
                    ) {
                        Column {
                            CategoryBadge(uiState.categoryLabel)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = uiState.prompt,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary,
                                lineHeight = 24.sp,
                            )
                        }
                    }
                }

                item {
                    Text(
                        text = "Escolha a alternativa correta:",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextSecondary,
                        modifier = Modifier.padding(horizontal = 4.dp),
                    )
                }

                itemsIndexed(uiState.alternatives) { index, alternative ->
                    val isSelected = selectedIndex == index
                    val isCorrect = alternative.isCorrect
                    val showFeedback = answered

                    val borderColor = when {
                        showFeedback && isCorrect -> Color(0xFF4CAF50)
                        showFeedback && isSelected && !isCorrect -> Color(0xFFE53935)
                        isSelected -> TealPrimary
                        else -> Color(0xFFE5E7EB)
                    }
                    val bgColor = when {
                        showFeedback && isCorrect -> Color(0xFFE8F5E9)
                        showFeedback && isSelected && !isCorrect -> Color(0xFFFFEBEE)
                        isSelected -> TealPrimary.copy(alpha = 0.08f)
                        else -> Color.White
                    }

                    AlternativeItem(
                        label = AlternativeLabels.getOrElse(index) { "$index" },
                        text = alternative.text,
                        isSelected = isSelected,
                        borderColor = borderColor,
                        bgColor = bgColor,
                        showCorrectIcon = showFeedback && isCorrect,
                        enabled = !answered,
                        onClick = { if (!answered) selectedIndex = index },
                    )
                }

                if (!answered) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (selectedIndex != null)
                                        Brush.horizontalGradient(listOf(TealDark, TealLight))
                                    else
                                        Brush.horizontalGradient(listOf(Color(0xFFBDBDBD), Color(0xFFBDBDBD)))
                                )
                                .clickable(enabled = selectedIndex != null) { confirmed = true },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("Confirmar Resposta", color = Color.White, fontWeight = FontWeight.SemiBold)
                        }
                    }
                } else {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        val acertou = uiState.alternatives.getOrNull(selectedIndex!!)?.isCorrect == true
                        FeedbackBanner(acertou = acertou)
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Brush.horizontalGradient(listOf(TealDark, TealLight)))
                                .clickable { onBackClick() },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(stringResource(R.string.back_button), color = Color.White, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
private fun AlternativeItem(
    label: String,
    text: String,
    isSelected: Boolean,
    borderColor: Color,
    bgColor: Color,
    showCorrectIcon: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(1.5.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(enabled = enabled) { onClick() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(if (isSelected) TealPrimary else Color(0xFFF0F0F0)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.White else TextSecondary,
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary,
            modifier = Modifier.weight(1f),
        )
        if (showCorrectIcon) {
            Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun FeedbackBanner(acertou: Boolean, modifier: Modifier = Modifier) {
    val bgColor = if (acertou) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
    val textColor = if (acertou) Color(0xFF2E7D32) else Color(0xFFC62828)
    val message = if (acertou) "Parabéns! Resposta correta! 🎉" else "Não foi dessa vez. Tente novamente! 💪"
    Box(
        modifier = modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(bgColor).padding(16.dp),
    ) {
        Text(message, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = textColor)
    }
}

@Composable
private fun CategoryBadge(label: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(TealPrimary.copy(alpha = 0.12f))
            .padding(horizontal = 12.dp, vertical = 4.dp),
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = TealPrimary, fontWeight = FontWeight.SemiBold)
    }
}
