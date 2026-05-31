package com.labirintodosaber.ui.screen.register

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
fun RegisterScreen(
    onBackClick: () -> Unit,
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RegisterViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    RegisterContent(
        uiState = uiState,
        onAction = viewModel::onAction,
        onBackClick = onBackClick,
        onLoginClick = onLoginClick,
        modifier = modifier,
    )
}

@Composable
private fun RegisterContent(
    uiState: RegisterUiState,
    onAction: (RegisterAction) -> Unit,
    onBackClick: () -> Unit,
    onLoginClick: () -> Unit,
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
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // Botão voltar
            Surface(
                onClick = onBackClick,
                modifier = Modifier.padding(start = 20.dp),
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
                        text = stringResource(R.string.register_back_button),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimary,
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            ) {
                RegisterForm(
                    uiState = uiState,
                    onAction = onAction,
                    onLoginClick = onLoginClick,
                )
            }

            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}

@Composable
private fun RegisterForm(
    uiState: RegisterUiState,
    onAction: (RegisterAction) -> Unit,
    onLoginClick: () -> Unit,
) {
    Column(
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp),
    ) {
        Text(
            text = stringResource(R.string.register_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.register_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Nome Completo
        Text(
            text = stringResource(R.string.register_full_name_label),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
            color = TextPrimary,
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = uiState.fullName,
            onValueChange = { onAction(RegisterAction.OnFullNameChange(it)) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = stringResource(R.string.register_full_name_placeholder),
                    color = TextSecondary,
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = stringResource(R.string.register_full_name_icon_desc),
                    tint = TextSecondary,
                )
            },
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

        // Email
        Text(
            text = stringResource(R.string.register_email_label),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
            color = TextPrimary,
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = uiState.email,
            onValueChange = { onAction(RegisterAction.OnEmailChange(it)) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = stringResource(R.string.register_email_placeholder),
                    color = TextSecondary,
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Email,
                    contentDescription = stringResource(R.string.register_email_icon_desc),
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
            text = stringResource(R.string.register_password_label),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
            color = TextPrimary,
        )
        Spacer(modifier = Modifier.height(6.dp))
        PasswordField(
            value = uiState.password,
            visible = uiState.passwordVisible,
            onValueChange = { onAction(RegisterAction.OnPasswordChange(it)) },
            onToggleVisibility = { onAction(RegisterAction.OnTogglePasswordVisibility) },
            iconDesc = stringResource(R.string.register_password_icon_desc),
            toggleDesc = stringResource(R.string.register_toggle_password_desc),
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Confirmar Senha
        Text(
            text = stringResource(R.string.register_confirm_password_label),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
            color = TextPrimary,
        )
        Spacer(modifier = Modifier.height(6.dp))
        PasswordField(
            value = uiState.confirmPassword,
            visible = uiState.confirmPasswordVisible,
            onValueChange = { onAction(RegisterAction.OnConfirmPasswordChange(it)) },
            onToggleVisibility = { onAction(RegisterAction.OnToggleConfirmPasswordVisibility) },
            iconDesc = stringResource(R.string.register_confirm_password_icon_desc),
            toggleDesc = stringResource(R.string.register_toggle_confirm_password_desc),
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Botão Criar minha conta
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
                .clickable { onAction(RegisterAction.OnRegisterClick) },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(R.string.register_button),
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
            HorizontalDivider(modifier = Modifier.weight(1f), color = DividerColor)
            Text(
                text = stringResource(R.string.register_or_divider),
                modifier = Modifier.padding(horizontal = 16.dp),
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
            )
            HorizontalDivider(modifier = Modifier.weight(1f), color = DividerColor)
        }
        Spacer(modifier = Modifier.height(20.dp))

        // Botão Google
        OutlinedButton(
            onClick = { onAction(RegisterAction.OnGoogleRegisterClick) },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, InputBorder),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
            contentPadding = PaddingValues(horizontal = 16.dp),
        ) {
            Text(
                text = "G",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4285F4),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.register_google_button),
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
            )
        }
        Spacer(modifier = Modifier.height(20.dp))

        // Já tem uma conta?
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.register_login_prompt),
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
            )
            Text(
                text = stringResource(R.string.register_login_link),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = TealPrimary,
                modifier = Modifier.clickable { onLoginClick() },
            )
        }
    }
}

@Composable
private fun PasswordField(
    value: String,
    visible: Boolean,
    onValueChange: (String) -> Unit,
    onToggleVisibility: () -> Unit,
    iconDesc: String,
    toggleDesc: String,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text(text = "••••••••", color = TextSecondary) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Outlined.Lock,
                contentDescription = iconDesc,
                tint = TextSecondary,
            )
        },
        trailingIcon = {
            androidx.compose.material3.IconButton(onClick = onToggleVisibility) {
                Icon(
                    imageVector = if (visible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                    contentDescription = toggleDesc,
                    tint = TextSecondary,
                )
            }
        },
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
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
}
