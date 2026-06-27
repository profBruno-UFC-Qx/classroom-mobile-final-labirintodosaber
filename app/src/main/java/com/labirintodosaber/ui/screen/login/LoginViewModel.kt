package com.labirintodosaber.ui.screen.login

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
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onAction(action: LoginAction) {
        when (action) {
            is LoginAction.OnEmailChange -> _uiState.update { it.copy(email = action.email, errorMessage = null) }
            is LoginAction.OnPasswordChange -> _uiState.update { it.copy(password = action.password, errorMessage = null) }
            LoginAction.OnToggleRememberMe -> _uiState.update { it.copy(rememberMe = !it.rememberMe) }
            LoginAction.OnTogglePasswordVisibility -> _uiState.update { it.copy(passwordVisible = !it.passwordVisible) }
            LoginAction.OnLoginClick -> signIn()
            LoginAction.OnLoginHandled -> _uiState.update { it.copy(loginSuccess = false) }
            LoginAction.OnGoogleLoginClick -> { /* TODO: implementar Google Sign-In */ }
        }
    }

    private fun signIn() {
        val state = _uiState.value
        if (state.email.isBlank() || state.password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Preencha email e senha.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = authRepository.signIn(state.email.trim(), state.password)) {
                is ApiResult.Success -> _uiState.update { it.copy(isLoading = false, loginSuccess = true) }
                is ApiResult.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
            }
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
    val loginSuccess: Boolean = false,
)

sealed interface LoginAction {
    data class OnEmailChange(val email: String) : LoginAction
    data class OnPasswordChange(val password: String) : LoginAction
    data object OnToggleRememberMe : LoginAction
    data object OnTogglePasswordVisibility : LoginAction
    data object OnLoginClick : LoginAction
    data object OnLoginHandled : LoginAction
    data object OnGoogleLoginClick : LoginAction
}
