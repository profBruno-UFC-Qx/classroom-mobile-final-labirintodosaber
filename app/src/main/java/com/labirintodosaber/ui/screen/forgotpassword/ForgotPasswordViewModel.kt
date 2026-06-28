package com.labirintodosaber.ui.screen.forgotpassword

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.labirintodosaber.data.remote.ApiResult
import com.labirintodosaber.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

    fun onAction(action: ForgotPasswordAction) {
        when (action) {
            is ForgotPasswordAction.OnEmailChange -> _uiState.update { it.copy(email = action.email, errorMessage = null) }
            ForgotPasswordAction.OnSendCodeClick -> sendCode()
            is ForgotPasswordAction.OnCodeChange -> _uiState.update {
                // O token de recuperação é alfanumérico (letras e números).
                it.copy(code = action.code.filter { c -> c.isLetterOrDigit() }.take(12))
            }
            // O passo de código é apenas visual: a API não expõe verificação de código.
            ForgotPasswordAction.OnVerifyCodeClick -> _uiState.update { it.copy(step = ForgotPasswordStep.NEW_PASSWORD) }
            ForgotPasswordAction.OnResendCodeClick -> sendCode()
            is ForgotPasswordAction.OnNewPasswordChange -> _uiState.update { it.copy(newPassword = action.password, errorMessage = null) }
            is ForgotPasswordAction.OnConfirmNewPasswordChange -> _uiState.update { it.copy(confirmNewPassword = action.password, errorMessage = null) }
            ForgotPasswordAction.OnToggleNewPasswordVisibility -> _uiState.update { it.copy(newPasswordVisible = !it.newPasswordVisible) }
            ForgotPasswordAction.OnToggleConfirmNewPasswordVisibility -> _uiState.update { it.copy(confirmNewPasswordVisible = !it.confirmNewPasswordVisible) }
            ForgotPasswordAction.OnResetPasswordClick -> resetPassword()
            ForgotPasswordAction.OnResetHandled -> _uiState.update { it.copy(resetSuccess = false) }
        }
    }

    private fun sendCode() {
        val email = _uiState.value.email.trim()
        if (email.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Informe seu email.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = authRepository.generateToken(email)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(isLoading = false, step = ForgotPasswordStep.CODE)
                }
                is ApiResult.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
            }
        }
    }

    private fun resetPassword() {
        val state = _uiState.value
        val validationError = when {
            state.newPassword.length < 6 -> "A senha deve ter ao menos 6 caracteres."
            state.newPassword != state.confirmNewPassword -> "As senhas não conferem."
            else -> null
        }
        if (validationError != null) {
            _uiState.update { it.copy(errorMessage = validationError) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = authRepository.updatePassword(state.email.trim(), state.newPassword)
            when (result) {
                is ApiResult.Success -> _uiState.update { it.copy(isLoading = false, resetSuccess = true) }
                is ApiResult.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
            }
        }
    }

    fun maskedEmail(): String {
        val email = _uiState.value.email
        val atIndex = email.indexOf('@')
        if (atIndex <= 0) return email
        return "*".repeat(minOf(atIndex, 10)) + email.substring(atIndex)
    }
}

enum class ForgotPasswordStep { EMAIL, CODE, NEW_PASSWORD }

data class ForgotPasswordUiState(
    val step: ForgotPasswordStep = ForgotPasswordStep.EMAIL,
    val email: String = "",
    val code: String = "",
    val newPassword: String = "",
    val confirmNewPassword: String = "",
    val newPasswordVisible: Boolean = false,
    val confirmNewPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val resetSuccess: Boolean = false,
)

sealed interface ForgotPasswordAction {
    data class OnEmailChange(val email: String) : ForgotPasswordAction
    data object OnSendCodeClick : ForgotPasswordAction
    data class OnCodeChange(val code: String) : ForgotPasswordAction
    data object OnVerifyCodeClick : ForgotPasswordAction
    data object OnResendCodeClick : ForgotPasswordAction
    data class OnNewPasswordChange(val password: String) : ForgotPasswordAction
    data class OnConfirmNewPasswordChange(val password: String) : ForgotPasswordAction
    data object OnToggleNewPasswordVisibility : ForgotPasswordAction
    data object OnToggleConfirmNewPasswordVisibility : ForgotPasswordAction
    data object OnResetPasswordClick : ForgotPasswordAction
    data object OnResetHandled : ForgotPasswordAction
}
