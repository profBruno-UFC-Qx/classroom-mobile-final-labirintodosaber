package com.labirintodosaber.ui.screen.sessionreport

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class CategoryAccuracyItem(
    val label: String,
    val percent: Int,
    val colorHex: Long,
)

data class ActivityTypeAccuracyItem(
    val label: String,
    val count: Int,
    val percent: Int,
)

data class SessionReportUiState(
    val studentName: String = "João Pedro Silva",
    val date: String = "27/03/2026",
    val professional: String = "Maria Silva",
    val totalQuestions: Int = 10,
    val totalTime: String = "25:30",
    val avgResponseTime: String = "2:15",
    val avgCorrectTime: String = "1:45",
    val avgWrongTime: String = "3:20",
    val categoryAccuracy: List<CategoryAccuracyItem> = emptyList(),
    val typeAccuracy: List<ActivityTypeAccuracyItem> = emptyList(),
    val descriptiveReport: String = "",
)

sealed interface SessionReportAction {
    data class OnDescriptiveReportChange(val text: String) : SessionReportAction
}

@HiltViewModel
class SessionReportViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(
        SessionReportUiState(
            categoryAccuracy = listOf(
                CategoryAccuracyItem("Leitura",      85, 0xFF2563EB),
                CategoryAccuracyItem("Escrita",      70, 0xFFEA580C),
                CategoryAccuracyItem("Vocabulário",  90, 0xFF7C3AED),
                CategoryAccuracyItem("Compreensão",  75, 0xFF16A34A),
            ),
            typeAccuracy = listOf(
                ActivityTypeAccuracyItem("Imagem + Áudio",  8,  88),
                ActivityTypeAccuracyItem("Somente Imagem",  4,  75),
                ActivityTypeAccuracyItem("Somente Áudio",   5,  80),
                ActivityTypeAccuracyItem("Somente Texto",   3,  70),
            ),
        )
    )
    val uiState: StateFlow<SessionReportUiState> = _uiState.asStateFlow()

    fun onAction(action: SessionReportAction) {
        when (action) {
            is SessionReportAction.OnDescriptiveReportChange ->
                _uiState.update { it.copy(descriptiveReport = action.text) }
        }
    }
}
