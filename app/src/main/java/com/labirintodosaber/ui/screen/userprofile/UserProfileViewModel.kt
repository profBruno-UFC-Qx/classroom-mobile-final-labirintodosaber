package com.labirintodosaber.ui.screen.userprofile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.labirintodosaber.data.local.UserPreferencesStore
import com.labirintodosaber.data.remote.ApiResult
import com.labirintodosaber.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UserProfileUiState(
    val name: String = "",
    val email: String = "",
    val contact: String = "",
    val photoUrl: String? = null,
    val isDarkTheme: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

sealed interface UserProfileAction {
    data object OnToggleDarkTheme : UserProfileAction
}

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userPreferencesStore: UserPreferencesStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserProfileUiState())
    val uiState: StateFlow<UserProfileUiState> = _uiState.asStateFlow()

    /** Usado pelo NavGraph para determinar a tela inicial no startup. null = ainda carregando. */
    val isAuthenticated: StateFlow<Boolean?> = authRepository.isAuthenticated
        .stateIn(viewModelScope, SharingStarted.Eagerly, initialValue = null)

    init {
        load()
        observeTheme()
    }

    fun onAction(action: UserProfileAction) {
        when (action) {
            UserProfileAction.OnToggleDarkTheme -> viewModelScope.launch {
                userPreferencesStore.setDarkTheme(!_uiState.value.isDarkTheme)
            }
        }
    }

    fun refresh() = load()

    fun signOut() {
        viewModelScope.launch { authRepository.signOut() }
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

    private fun observeTheme() {
        viewModelScope.launch {
            userPreferencesStore.isDarkTheme.collect { isDark ->
                _uiState.update { it.copy(isDarkTheme = isDark) }
            }
        }
    }
}
