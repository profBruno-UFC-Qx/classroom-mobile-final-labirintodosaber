package com.labirintodosaber.ui.screen.anamnese

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class AnamneseModelItem(
    val name: String,
    val questionCount: Int,
    val lastEdited: String,
)

data class AnamneseUiState(
    val models: List<AnamneseModelItem> = MOCK_MODELS,
)

sealed interface AnamneseAction

private val MOCK_MODELS = listOf(
    AnamneseModelItem("Anamnese Inicial", 24, "15/06/2025"),
    AnamneseModelItem("Avaliação Psicopedagógica", 18, "10/06/2025"),
    AnamneseModelItem("Dificuldades de Aprendizagem", 15, "05/06/2025"),
    AnamneseModelItem("Avaliação Neuropsicológica", 30, "01/06/2025"),
    AnamneseModelItem("Anamnese Familiar", 12, "28/05/2025"),
)

@HiltViewModel
class AnamneseViewModel @Inject constructor() : ViewModel() {
    val uiState: StateFlow<AnamneseUiState> = MutableStateFlow(AnamneseUiState()).asStateFlow()
}
