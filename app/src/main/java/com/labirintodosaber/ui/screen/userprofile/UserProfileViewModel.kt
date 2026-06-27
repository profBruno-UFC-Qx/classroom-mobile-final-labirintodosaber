package com.labirintodosaber.ui.screen.userprofile

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class UserProfileUiState(
    val name: String = "Ana Paula Silva",
    val displayName: String = "Dra. Ana Paula",
    val role: String = "Psicopedagoga",
    val especialidade: String = "Alfabetização e Letramento",
    val membroDesde: String = "Janeiro de 2025",
    val email: String = "ana@labirinto.com",
    val phone: String = "(85) 99999-9999",
    val registro: String = "CRP 06/123456",
    val endereco: String = "Rua das Flores, 123 - Quixadá, CE",
    val senhaAtual: String = "",
    val novaSenha: String = "",
    val confirmarSenha: String = "",
    val passwordError: String? = null,
)

sealed interface UserProfileAction {
    data class OnNameChange(val value: String) : UserProfileAction
    data class OnPhoneChange(val value: String) : UserProfileAction
    data class OnEnderecoChange(val value: String) : UserProfileAction
    data class OnSenhaAtualChange(val value: String) : UserProfileAction
    data class OnNovaSenhaChange(val value: String) : UserProfileAction
    data class OnConfirmarSenhaChange(val value: String) : UserProfileAction
    data object OnSaveProfile : UserProfileAction
    data object OnChangePassword : UserProfileAction
}

@HiltViewModel
class UserProfileViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(UserProfileUiState())
    val uiState: StateFlow<UserProfileUiState> = _uiState.asStateFlow()

    fun onAction(action: UserProfileAction) {
        when (action) {
            is UserProfileAction.OnNameChange -> _uiState.update { it.copy(name = action.value) }
            is UserProfileAction.OnPhoneChange -> _uiState.update { it.copy(phone = action.value) }
            is UserProfileAction.OnEnderecoChange -> _uiState.update { it.copy(endereco = action.value) }
            is UserProfileAction.OnSenhaAtualChange -> _uiState.update { it.copy(senhaAtual = action.value, passwordError = null) }
            is UserProfileAction.OnNovaSenhaChange -> _uiState.update { it.copy(novaSenha = action.value, passwordError = null) }
            is UserProfileAction.OnConfirmarSenhaChange -> _uiState.update { it.copy(confirmarSenha = action.value, passwordError = null) }
            UserProfileAction.OnSaveProfile -> Unit
            UserProfileAction.OnChangePassword -> changePassword()
        }
    }

    private fun changePassword() {
        val state = _uiState.value
        when {
            state.novaSenha.isBlank() -> _uiState.update { it.copy(passwordError = "Nova senha não pode ser vazia") }
            state.novaSenha != state.confirmarSenha -> _uiState.update { it.copy(passwordError = "Senhas não coincidem") }
            else -> _uiState.update { it.copy(senhaAtual = "", novaSenha = "", confirmarSenha = "", passwordError = null) }
        }
    }
}
