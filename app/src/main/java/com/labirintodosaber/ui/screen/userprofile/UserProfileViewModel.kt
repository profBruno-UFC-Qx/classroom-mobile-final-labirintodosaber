package com.labirintodosaber.ui.screen.userprofile

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

data class UserProfileUiState(
    val name: String = "",
    val displayName: String = "",
    val role: String = "Profissional",
    val especialidade: String = "",
    val membroDesde: String = "",
    val email: String = "",
    val phone: String = "",
    val registro: String = "",
    val endereco: String = "",
    val photoUrl: String? = null,
    val senhaAtual: String = "",
    val novaSenha: String = "",
    val confirmarSenha: String = "",
    val passwordError: String? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val savedMessage: String? = null,
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
class UserProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserProfileUiState())
    val uiState: StateFlow<UserProfileUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    /** Recarrega os dados do educador (ex.: após autenticar). */
    fun refresh() = load()

    fun onAction(action: UserProfileAction) {
        when (action) {
            is UserProfileAction.OnNameChange -> _uiState.update { it.copy(name = action.value, savedMessage = null) }
            is UserProfileAction.OnPhoneChange -> _uiState.update { it.copy(phone = action.value, savedMessage = null) }
            is UserProfileAction.OnEnderecoChange -> _uiState.update { it.copy(endereco = action.value) }
            is UserProfileAction.OnSenhaAtualChange -> _uiState.update { it.copy(senhaAtual = action.value, passwordError = null) }
            is UserProfileAction.OnNovaSenhaChange -> _uiState.update { it.copy(novaSenha = action.value, passwordError = null) }
            is UserProfileAction.OnConfirmarSenhaChange -> _uiState.update { it.copy(confirmarSenha = action.value, passwordError = null) }
            UserProfileAction.OnSaveProfile -> saveProfile()
            UserProfileAction.OnChangePassword -> changePassword()
        }
    }

    private fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = authRepository.me()) {
                is ApiResult.Success -> {
                    val educator = result.data
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            name = educator.name,
                            displayName = educator.name,
                            email = educator.email,
                            phone = educator.contact.orEmpty(),
                            photoUrl = educator.photoUrl?.takeIf { url -> url.isNotBlank() },
                        )
                    }
                }
                is ApiResult.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
            }
        }
    }

    private fun saveProfile() {
        val state = _uiState.value
        if (state.name.isBlank()) {
            _uiState.update { it.copy(errorMessage = "O nome não pode ficar vazio.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null, savedMessage = null) }
            val result = authRepository.updateEducator(
                newName = state.name.trim(),
                newContact = state.phone.trim().takeIf { it.isNotEmpty() },
            )
            when (result) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(
                        isSaving = false,
                        name = result.data.name,
                        displayName = result.data.name,
                        phone = result.data.contact.orEmpty(),
                        savedMessage = "Perfil atualizado.",
                    )
                }
                is ApiResult.Error -> _uiState.update { it.copy(isSaving = false, errorMessage = result.message) }
            }
        }
    }

    private fun changePassword() {
        val state = _uiState.value
        val error = when {
            state.novaSenha.length < 6 -> "A nova senha deve ter ao menos 6 caracteres."
            state.novaSenha != state.confirmarSenha -> "As senhas não coincidem."
            else -> null
        }
        if (error != null) {
            _uiState.update { it.copy(passwordError = error) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, passwordError = null, savedMessage = null) }
            when (val result = authRepository.updatePassword(state.email, state.novaSenha)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(isSaving = false, senhaAtual = "", novaSenha = "", confirmarSenha = "", savedMessage = "Senha alterada.")
                }
                is ApiResult.Error -> _uiState.update { it.copy(isSaving = false, passwordError = result.message) }
            }
        }
    }
}
