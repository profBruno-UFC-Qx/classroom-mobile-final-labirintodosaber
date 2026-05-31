package com.labirintodosaber.ui.screen.forgotpassword

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

    fun onAction(action: ForgotPasswordAction) {
        when (action) {
            is ForgotPasswordAction.OnEmailChange -> _uiState.update { it.copy(email = action.email) }
            ForgotPasswordAction.OnSendCodeClick -> _uiState.update { it.copy(step = ForgotPasswordStep.CODE) }
            is ForgotPasswordAction.OnCodeChange -> _uiState.update {
                it.copy(code = action.code.filter { c -> c.isDigit() }.take(6))
            }
            ForgotPasswordAction.OnVerifyCodeClick -> _uiState.update { it.copy(step = ForgotPasswordStep.NEW_PASSWORD) }
            ForgotPasswordAction.OnResendCodeClick -> { /* TODO: reenviar código */ }
            is ForgotPasswordAction.OnNewPasswordChange -> _uiState.update { it.copy(newPassword = action.password) }
            is ForgotPasswordAction.OnConfirmNewPasswordChange -> _uiState.update { it.copy(confirmNewPassword = action.password) }
            ForgotPasswordAction.OnToggleNewPasswordVisibility -> _uiState.update { it.copy(newPasswordVisible = !it.newPasswordVisible) }
            ForgotPasswordAction.OnToggleConfirmNewPasswordVisibility -> _uiState.update { it.copy(confirmNewPasswordVisible = !it.confirmNewPasswordVisible) }
            ForgotPasswordAction.OnResetPasswordClick -> { /* TODO: redefinir senha */ }
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
}
