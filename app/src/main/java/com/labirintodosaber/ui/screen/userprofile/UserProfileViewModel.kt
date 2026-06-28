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

/** Tela "Meu Perfil" é apenas de visualização — exibe os dados do educador logado. */
data class UserProfileUiState(
    val name: String = "",
    val email: String = "",
    val contact: String = "",
    val photoUrl: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

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
                            email = educator.email,
                            contact = educator.contact.orEmpty(),
                            photoUrl = educator.photoUrl?.takeIf { url -> url.isNotBlank() },
                        )
                    }
                }
                is ApiResult.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
            }
        }
    }
}
