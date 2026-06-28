package com.labirintodosaber.ui.screen.register

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
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onAction(action: RegisterAction) {
        when (action) {
            is RegisterAction.OnFullNameChange -> _uiState.update { it.copy(fullName = action.fullName, errorMessage = null) }
            is RegisterAction.OnEmailChange -> _uiState.update { it.copy(email = action.email, errorMessage = null) }
            is RegisterAction.OnPasswordChange -> _uiState.update { it.copy(password = action.password, errorMessage = null) }
            is RegisterAction.OnConfirmPasswordChange -> _uiState.update { it.copy(confirmPassword = action.confirmPassword, errorMessage = null) }
            RegisterAction.OnTogglePasswordVisibility -> _uiState.update { it.copy(passwordVisible = !it.passwordVisible) }
            RegisterAction.OnToggleConfirmPasswordVisibility -> _uiState.update { it.copy(confirmPasswordVisible = !it.confirmPasswordVisible) }
            RegisterAction.OnRegisterClick -> register()
            RegisterAction.OnRegisterHandled -> _uiState.update { it.copy(registerSuccess = false) }
        }
    }

    private fun register() {
        val state = _uiState.value
        val validationError = validate(state)
        if (validationError != null) {
            _uiState.update { it.copy(errorMessage = validationError) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = authRepository.register(
                name = state.fullName.trim(),
                email = state.email.trim(),
                password = state.password,
            )
            when (result) {
                is ApiResult.Success -> _uiState.update { it.copy(isLoading = false, registerSuccess = true) }
                is ApiResult.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
            }
        }
    }

    private fun validate(state: RegisterUiState): String? = when {
        state.fullName.trim().length < 3 -> "O nome deve ter ao menos 3 caracteres."
        state.email.isBlank() -> "Informe um email."
        state.password.length < 6 -> "A senha deve ter ao menos 6 caracteres."
        state.password != state.confirmPassword -> "As senhas não conferem."
        else -> null
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
    val registerSuccess: Boolean = false,
)

sealed interface RegisterAction {
    data class OnFullNameChange(val fullName: String) : RegisterAction
    data class OnEmailChange(val email: String) : RegisterAction
    data class OnPasswordChange(val password: String) : RegisterAction
    data class OnConfirmPasswordChange(val confirmPassword: String) : RegisterAction
    data object OnTogglePasswordVisibility : RegisterAction
    data object OnToggleConfirmPasswordVisibility : RegisterAction
    data object OnRegisterClick : RegisterAction
    data object OnRegisterHandled : RegisterAction
}
