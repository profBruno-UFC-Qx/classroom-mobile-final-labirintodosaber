package com.labirintodosaber.ui.screen.register

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onAction(action: RegisterAction) {
        when (action) {
            is RegisterAction.OnFullNameChange -> _uiState.update { it.copy(fullName = action.fullName) }
            is RegisterAction.OnEmailChange -> _uiState.update { it.copy(email = action.email) }
            is RegisterAction.OnPasswordChange -> _uiState.update { it.copy(password = action.password) }
            is RegisterAction.OnConfirmPasswordChange -> _uiState.update { it.copy(confirmPassword = action.confirmPassword) }
            RegisterAction.OnTogglePasswordVisibility -> _uiState.update { it.copy(passwordVisible = !it.passwordVisible) }
            RegisterAction.OnToggleConfirmPasswordVisibility -> _uiState.update { it.copy(confirmPasswordVisible = !it.confirmPasswordVisible) }
            RegisterAction.OnRegisterClick -> { /* TODO: implementar cadastro */ }
            RegisterAction.OnGoogleRegisterClick -> { /* TODO: implementar Google Sign-In */ }
        }
    }
}

data class RegisterUiState(
    val fullName: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val passwordVisible: Boolean = false,
    val confirmPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

sealed interface RegisterAction {
    data class OnFullNameChange(val fullName: String) : RegisterAction
    data class OnEmailChange(val email: String) : RegisterAction
    data class OnPasswordChange(val password: String) : RegisterAction
    data class OnConfirmPasswordChange(val confirmPassword: String) : RegisterAction
    data object OnTogglePasswordVisibility : RegisterAction
    data object OnToggleConfirmPasswordVisibility : RegisterAction
    data object OnRegisterClick : RegisterAction
    data object OnGoogleRegisterClick : RegisterAction
}
