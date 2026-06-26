package com.labirintodosaber.ui.screen.anamnese

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.labirintodosaber.data.model.AnamneseTemplate
import com.labirintodosaber.data.remote.ApiResult
import com.labirintodosaber.data.repository.AnamneseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class AnamneseModelItem(
    val name: String,
    val questionCount: Int,
    val lastEdited: String,
)

data class AnamneseUiState(
    val models: List<AnamneseModelItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

sealed interface AnamneseAction {
    data object OnRetry : AnamneseAction
}

@HiltViewModel
class AnamneseViewModel @Inject constructor(
    private val anamneseRepository: AnamneseRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnamneseUiState())
    val uiState: StateFlow<AnamneseUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun onAction(action: AnamneseAction) {
        when (action) {
            AnamneseAction.OnRetry -> load()
        }
    }

    private fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = anamneseRepository.listTemplates()) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(isLoading = false, models = result.data.map { t -> t.toItem() })
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = result.message)
                }
            }
        }
    }
}

private fun AnamneseTemplate.toItem() = AnamneseModelItem(
    name = title,
    questionCount = questions.size,
    lastEdited = createdAt.formatDate(),
)

private val DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy")

private fun String.formatDate(): String {
    val instant = runCatching { Instant.parse(this) }.getOrNull()
        ?: runCatching { OffsetDateTime.parse(this).toInstant() }.getOrNull()
        ?: return this
    return instant.atZone(ZoneId.systemDefault()).format(DATE_FORMAT)
}
