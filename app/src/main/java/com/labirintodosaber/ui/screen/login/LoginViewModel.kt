package com.labirintodosaber.ui.screen.login

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onAction(action: LoginAction) {
        when (action) {
            is LoginAction.OnEmailChange -> _uiState.update { it.copy(email = action.email) }
            is LoginAction.OnPasswordChange -> _uiState.update { it.copy(password = action.password) }
            LoginAction.OnToggleRememberMe -> _uiState.update { it.copy(rememberMe = !it.rememberMe) }
            LoginAction.OnTogglePasswordVisibility -> _uiState.update { it.copy(passwordVisible = !it.passwordVisible) }
            LoginAction.OnLoginClick -> { /* TODO: implementar autenticação */ }
            LoginAction.OnGoogleLoginClick -> { /* TODO: implementar Google Sign-In */ }
            LoginAction.OnForgotPasswordClick -> { /* TODO: navegar para recuperação de senha */ }
            LoginAction.OnRegisterClick -> { /* TODO: navegar para cadastro */ }
        }
    }
}

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val rememberMe: Boolean = false,
    val passwordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

sealed interface LoginAction {
    data class OnEmailChange(val email: String) : LoginAction
    data class OnPasswordChange(val password: String) : LoginAction
    data object OnToggleRememberMe : LoginAction
    data object OnTogglePasswordVisibility : LoginAction
    data object OnLoginClick : LoginAction
    data object OnGoogleLoginClick : LoginAction
    data object OnForgotPasswordClick : LoginAction
    data object OnRegisterClick : LoginAction
}
