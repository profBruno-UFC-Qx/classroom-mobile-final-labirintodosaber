package com.labirintodosaber.ui.screen.login

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
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

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onRegisterClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LoginContent(
        uiState = uiState,
        onAction = viewModel::onAction,
        onRegisterClick = onRegisterClick,
        onForgotPasswordClick = onForgotPasswordClick,
        modifier = modifier,
    )
}

@Composable
private fun LoginContent(
    uiState: LoginUiState,
    onAction: (LoginAction) -> Unit,
    onRegisterClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
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
            Spacer(modifier = Modifier.height(52.dp))

            Image(
                painter = painterResource(R.drawable.logo),
                contentDescription = stringResource(R.string.login_logo_desc),
                modifier = Modifier
                    .fillMaxWidth(0.58f)
                    .aspectRatio(2f),
            )

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            ) {
                LoginForm(uiState = uiState, onAction = onAction, onRegisterClick = onRegisterClick, onForgotPasswordClick = onForgotPasswordClick)
            }

            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}

@Composable
private fun LoginForm(
    uiState: LoginUiState,
    onAction: (LoginAction) -> Unit,
    onRegisterClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
) {
    Column(
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp),
    ) {
        Text(
            text = stringResource(R.string.login_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.login_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Email
        Text(
            text = stringResource(R.string.login_email_label),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
            color = TextPrimary,
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = uiState.email,
            onValueChange = { onAction(LoginAction.OnEmailChange(it)) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = stringResource(R.string.login_email_placeholder),
                    color = TextSecondary,
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Email,
                    contentDescription = stringResource(R.string.login_email_icon_desc),
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
        Spacer(modifier = Modifier.height(16.dp))

        // Senha
        Text(
            text = stringResource(R.string.login_password_label),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
            color = TextPrimary,
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = uiState.password,
            onValueChange = { onAction(LoginAction.OnPasswordChange(it)) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = "••••••••",
                    color = TextSecondary,
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Lock,
                    contentDescription = stringResource(R.string.login_password_icon_desc),
                    tint = TextSecondary,
                )
            },
            trailingIcon = {
                IconButton(onClick = { onAction(LoginAction.OnTogglePasswordVisibility) }) {
                    Icon(
                        imageVector = if (uiState.passwordVisible) {
                            Icons.Outlined.VisibilityOff
                        } else {
                            Icons.Outlined.Visibility
                        },
                        contentDescription = stringResource(R.string.login_toggle_password_desc),
                        tint = TextSecondary,
                    )
                }
            },
            visualTransformation = if (uiState.passwordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
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
        Spacer(modifier = Modifier.height(12.dp))

        // Lembre-se + Esqueci senha
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                checked = uiState.rememberMe,
                onCheckedChange = { onAction(LoginAction.OnToggleRememberMe) },
                modifier = Modifier.size(20.dp),
                colors = CheckboxDefaults.colors(checkedColor = TealPrimary),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.login_remember_me),
                style = MaterialTheme.typography.bodySmall,
                color = TextPrimary,
            )
            Spacer(modifier = Modifier.weight(1f))
            TextButton(
                onClick = onForgotPasswordClick,
                contentPadding = PaddingValues(0.dp),
            ) {
                Text(
                    text = stringResource(R.string.login_forgot_password),
                    style = MaterialTheme.typography.bodySmall,
                    color = TealPrimary,
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        // Botão Entrar agora (gradiente)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(TealDark, TealLight),
                    ),
                )
                .clickable { onAction(LoginAction.OnLoginClick) },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(R.string.login_button),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
            )
        }
        Spacer(modifier = Modifier.height(20.dp))

        // Divisor OU
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                color = DividerColor,
            )
            Text(
                text = stringResource(R.string.login_or_divider),
                modifier = Modifier.padding(horizontal = 16.dp),
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
            )
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                color = DividerColor,
            )
        }
        Spacer(modifier = Modifier.height(20.dp))

        // Botão Google
        OutlinedButton(
            onClick = { onAction(LoginAction.OnGoogleLoginClick) },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, InputBorder),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
        ) {
            Text(
                text = "G",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4285F4),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.login_google_button),
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
            )
        }
        Spacer(modifier = Modifier.height(20.dp))

        // Novo por aqui?
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.login_register_prompt),
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
            )
            Text(
                text = stringResource(R.string.login_register_link),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = TealPrimary,
                modifier = Modifier.clickable { onRegisterClick() },
            )
        }
    }
}
