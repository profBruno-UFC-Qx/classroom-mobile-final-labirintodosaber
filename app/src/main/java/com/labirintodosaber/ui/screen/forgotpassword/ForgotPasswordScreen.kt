package com.labirintodosaber.ui.screen.forgotpassword

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.labirintodosaber.R
import com.labirintodosaber.ui.theme.DividerColor
import com.labirintodosaber.ui.theme.GradientBottom
import com.labirintodosaber.ui.theme.GradientMid
import com.labirintodosaber.ui.theme.GradientTop
import com.labirintodosaber.ui.theme.InputBackground
import com.labirintodosaber.ui.theme.InputBorder
import com.labirintodosaber.ui.theme.TealDark
import com.labirintodosaber.ui.theme.TealLight
import com.labirintodosaber.ui.theme.TealPrimary
import com.labirintodosaber.ui.theme.TextPrimary
import com.labirintodosaber.ui.theme.TextSecondary
import com.labirintodosaber.ui.theme.WarmButtonEnd
import com.labirintodosaber.ui.theme.WarmButtonStart

@Composable
fun ForgotPasswordScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ForgotPasswordViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.resetSuccess) {
        if (uiState.resetSuccess) {
            onBackClick()
            viewModel.onAction(ForgotPasswordAction.OnResetHandled)
        }
    }

    ForgotPasswordContent(
        uiState = uiState,
        maskedEmail = viewModel::maskedEmail,
        onAction = viewModel::onAction,
        onBackClick = onBackClick,
        modifier = modifier,
    )
}

@Composable
private fun ForgotPasswordContent(
    uiState: ForgotPasswordUiState,
    maskedEmail: () -> String,
    onAction: (ForgotPasswordAction) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(GradientTop, GradientMid, GradientBottom),
                ),
            ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // Back button
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                BackPill(onClick = onBackClick)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Etapa chip
            EtapaChip(currentStep = uiState.step.ordinal + 1)

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.forgot_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.padding(horizontal = 20.dp),
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.forgot_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                modifier = Modifier.padding(horizontal = 20.dp),
            )
            Spacer(modifier = Modifier.height(20.dp))

            // Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            ) {
                when (uiState.step) {
                    ForgotPasswordStep.EMAIL -> Step1EmailContent(
                        uiState = uiState,
                        onAction = onAction,
                    )
                    ForgotPasswordStep.CODE -> Step2CodeContent(
                        uiState = uiState,
                        maskedEmail = maskedEmail,
                        onAction = onAction,
                    )
                    ForgotPasswordStep.NEW_PASSWORD -> Step3PasswordContent(
                        uiState = uiState,
                        onAction = onAction,
                    )
                }
            }

            uiState.errorMessage?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            StepIndicator(
                currentStep = uiState.step.ordinal + 1,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 40.dp),
            )

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

// ── Step 1: Email ────────────────────────────────────────────────────────────

@Composable
private fun Step1EmailContent(
    uiState: ForgotPasswordUiState,
    onAction: (ForgotPasswordAction) -> Unit,
) {
    Column(
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Icon banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(InputBackground, RoundedCornerShape(12.dp))
                .padding(vertical = 20.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Outlined.Email,
                    contentDescription = null,
                    tint = TealPrimary,
                    modifier = Modifier.size(40.dp),
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.forgot_step1_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
        }
        Spacer(modifier = Modifier.height(20.dp))

        // Email field
        Text(
            text = stringResource(R.string.forgot_email_label),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
            color = TextPrimary,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = uiState.email,
            onValueChange = { onAction(ForgotPasswordAction.OnEmailChange(it)) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(text = stringResource(R.string.login_email_placeholder), color = TextSecondary)
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Email,
                    contentDescription = stringResource(R.string.forgot_email_icon_desc),
                    tint = TextSecondary,
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = TealPrimary,
                unfocusedBorderColor = InputBorder,
                focusedContainerColor = InputBackground,
                unfocusedContainerColor = InputBackground,
            ),
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Botão Enviar (warm gradient)
        WarmGradientButton(
            label = stringResource(R.string.forgot_send_button),
            onClick = { onAction(ForgotPasswordAction.OnSendCodeClick) },
        )
    }
}

// ── Step 2: OTP ──────────────────────────────────────────────────────────────

@Composable
private fun Step2CodeContent(
    uiState: ForgotPasswordUiState,
    maskedEmail: () -> String,
    onAction: (ForgotPasswordAction) -> Unit,
) {
    Column(
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Icon banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(InputBackground, RoundedCornerShape(12.dp))
                .padding(vertical = 20.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Outlined.Key,
                    contentDescription = null,
                    tint = TealPrimary,
                    modifier = Modifier.size(40.dp),
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.forgot_step2_sent_to),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
                Text(
                    text = maskedEmail(),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        // OTP input
        OtpInput(
            code = uiState.code,
            onCodeChange = { onAction(ForgotPasswordAction.OnCodeChange(it)) },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Botão Verificar (warm gradient)
        WarmGradientButton(
            label = stringResource(R.string.forgot_verify_button),
            onClick = { onAction(ForgotPasswordAction.OnVerifyCodeClick) },
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Reenviar link
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.forgot_resend_prompt),
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
            )
            Text(
                text = stringResource(R.string.forgot_resend_link),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = TealPrimary,
                modifier = Modifier.clickable { onAction(ForgotPasswordAction.OnResendCodeClick) },
            )
        }
    }
}

// ── Step 3: Nova Senha ───────────────────────────────────────────────────────

@Composable
private fun Step3PasswordContent(
    uiState: ForgotPasswordUiState,
    onAction: (ForgotPasswordAction) -> Unit,
) {
    Column(
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Success banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(InputBackground, RoundedCornerShape(12.dp))
                .padding(vertical = 20.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    tint = TealPrimary,
                    modifier = Modifier.size(40.dp),
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.forgot_step3_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
        }
        Spacer(modifier = Modifier.height(20.dp))

        // Nova Senha
        Text(
            text = stringResource(R.string.forgot_new_password_label),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
            color = TextPrimary,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = uiState.newPassword,
            onValueChange = { onAction(ForgotPasswordAction.OnNewPasswordChange(it)) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("••••••••", color = TextSecondary) },
            leadingIcon = {
                Icon(Icons.Outlined.Lock, contentDescription = stringResource(R.string.forgot_password_icon_desc), tint = TextSecondary)
            },
            trailingIcon = {
                IconButton(onClick = { onAction(ForgotPasswordAction.OnToggleNewPasswordVisibility) }) {
                    Icon(
                        imageVector = if (uiState.newPasswordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                        contentDescription = stringResource(R.string.forgot_toggle_password_desc),
                        tint = TextSecondary,
                    )
                }
            },
            visualTransformation = if (uiState.newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = TealPrimary,
                unfocusedBorderColor = InputBorder,
                focusedContainerColor = InputBackground,
                unfocusedContainerColor = InputBackground,
            ),
        )
        Text(
            text = stringResource(R.string.forgot_new_password_hint),
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Confirmar Nova Senha
        Text(
            text = stringResource(R.string.forgot_confirm_new_password_label),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
            color = TextPrimary,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = uiState.confirmNewPassword,
            onValueChange = { onAction(ForgotPasswordAction.OnConfirmNewPasswordChange(it)) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("••••••••", color = TextSecondary) },
            leadingIcon = {
                Icon(Icons.Outlined.Lock, contentDescription = stringResource(R.string.forgot_password_icon_desc), tint = TextSecondary)
            },
            trailingIcon = {
                IconButton(onClick = { onAction(ForgotPasswordAction.OnToggleConfirmNewPasswordVisibility) }) {
                    Icon(
                        imageVector = if (uiState.confirmNewPasswordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                        contentDescription = stringResource(R.string.forgot_toggle_password_desc),
                        tint = TextSecondary,
                    )
                }
            },
            visualTransformation = if (uiState.confirmNewPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = TealPrimary,
                unfocusedBorderColor = InputBorder,
                focusedContainerColor = InputBackground,
                unfocusedContainerColor = InputBackground,
            ),
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Botão Redefinir (teal gradient — etapa final)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Brush.horizontalGradient(colors = listOf(TealDark, TealLight)))
                .clickable { onAction(ForgotPasswordAction.OnResetPasswordClick) },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(R.string.forgot_reset_button),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
            )
        }
    }
}

// ── Componentes compartilhados ────────────────────────────────────────────────

@Composable
private fun BackPill(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50.dp),
        color = Color.White,
        shadowElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
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
                text = stringResource(R.string.forgot_back_button),
                style = MaterialTheme.typography.bodySmall,
                color = TextPrimary,
            )
        }
    }
}

@Composable
private fun EtapaChip(currentStep: Int, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = Color.White.copy(alpha = 0.9f),
        border = BorderStroke(1.dp, DividerColor),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.forgot_step_label),
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
            )
            Text(
                text = "$currentStep/3",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = TealPrimary,
            )
        }
    }
}

@Composable
private fun WarmGradientButton(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Brush.horizontalGradient(colors = listOf(WarmButtonStart, WarmButtonEnd)))
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary,
        )
    }
}

@Composable
private fun OtpInput(
    code: String,
    onCodeChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        // Boxes visuais
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            repeat(6) { index ->
                val isCurrent = index == code.length
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                        .background(InputBackground, RoundedCornerShape(8.dp))
                        .then(
                            Modifier.clip(RoundedCornerShape(8.dp))
                        )
                        .padding(1.dp)
                        .background(
                            if (isCurrent) TealPrimary.copy(alpha = 0.12f) else Color.Transparent,
                            RoundedCornerShape(7.dp),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    // Border drawn separately
                    androidx.compose.foundation.Canvas(modifier = Modifier.matchParentSize()) {
                        val strokeWidth = if (isCurrent) 1.5.dp.toPx() else 1.dp.toPx()
                        val color = if (isCurrent) TealPrimary else InputBorder
                        drawRoundRect(
                            color = color,
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx()),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(strokeWidth),
                        )
                    }
                    Text(
                        text = code.getOrNull(index)?.toString() ?: "",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                    )
                }
            }
        }

        // Campo invisível que captura o teclado
        BasicTextField(
            value = code,
            onValueChange = { new ->
                onCodeChange(new.filter { it.isDigit() }.take(6))
            },
            modifier = Modifier
                .matchParentSize()
                .alpha(0f),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        )
    }
}

@Composable
private fun StepIndicator(
    currentStep: Int,
    modifier: Modifier = Modifier,
) {
    val stepLabels = listOf(
        stringResource(R.string.forgot_step_email),
        stringResource(R.string.forgot_step_code),
        stringResource(R.string.forgot_step_password),
    )

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            repeat(3) { index ->
                val stepNum = index + 1
                val isActive = stepNum == currentStep
                val isDone = stepNum < currentStep

                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(
                            color = if (isActive || isDone) TealPrimary else Color(0xFFE5E7EB),
                            shape = CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stepNum.toString(),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isActive || isDone) Color.White else TextSecondary,
                    )
                }

                if (index < 2) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(2.dp)
                            .background(if (stepNum < currentStep) TealPrimary else Color(0xFFE5E7EB)),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            stepLabels.forEachIndexed { index, label ->
                Text(
                    text = label,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (index + 1 <= currentStep) TealPrimary else TextSecondary,
                )
            }
        }
    }
}
