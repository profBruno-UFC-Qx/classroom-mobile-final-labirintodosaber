package com.labirintodosaber.ui.screen.sessionrun

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SessionAlternative(
    val id: String,
    val text: String,
    val isCorrect: Boolean,
)

data class SessionTaskItem(
    val id: String,
    val imageRes: Int?,
    val hasAudio: Boolean,
    val audioDurationLabel: String?,
    val prompt: String,
    val alternatives: List<SessionAlternative>,
)

enum class AnswerResult { CORRECT, WRONG }

data class SessionRunUiState(
    val studentId: String = "",
    val sessionName: String = "",
    val tasks: List<SessionTaskItem> = emptyList(),
    val currentTaskIndex: Int = 0,
    val selectedAlternativeId: String? = null,
    val canConfirm: Boolean = false,
    val answerResult: AnswerResult? = null,
    val isAudioPlaying: Boolean = false,
    val elapsedSeconds: Int = 0,
    val isTimerRunning: Boolean = true,
    val showImageZoom: Boolean = false,
)

val SessionRunUiState.currentTask: SessionTaskItem?
    get() = tasks.getOrNull(currentTaskIndex)

val SessionRunUiState.timerLabel: String
    get() {
        val m = elapsedSeconds / 60
        val s = elapsedSeconds % 60
        return "%02d:%02d".format(m, s)
    }

val SessionRunUiState.isLastTask: Boolean
    get() = currentTaskIndex >= tasks.lastIndex

val SessionRunUiState.isFinished: Boolean
    get() = tasks.isNotEmpty() && currentTaskIndex >= tasks.size

sealed interface SessionRunAction {
    data class OnAlternativeSelect(val alternativeId: String) : SessionRunAction
    data object OnConfirmAnswer : SessionRunAction
    data object OnNextActivity : SessionRunAction
    data object OnRetry : SessionRunAction
    data object OnToggleAudio : SessionRunAction
    data object OnToggleTimer : SessionRunAction
    data object OnZoomImage : SessionRunAction
    data object OnDismissZoom : SessionRunAction
}

private val MOCK_TASKS = listOf(
    SessionTaskItem(
        id = "q1",
        imageRes = com.labirintodosaber.R.drawable.logo,
        hasAudio = true,
        audioDurationLabel = "00:02",
        prompt = "Qual animal a figura representa?",
        alternatives = listOf(
            SessionAlternative(id = "a1", text = "GATO",      isCorrect = false),
            SessionAlternative(id = "a2", text = "BORBOLETA", isCorrect = false),
            SessionAlternative(id = "a3", text = "LEÃO",      isCorrect = true),
            SessionAlternative(id = "a4", text = "CAVALO",    isCorrect = false),
        ),
    ),
    SessionTaskItem(
        id = "q2",
        imageRes = null,
        hasAudio = false,
        audioDurationLabel = null,
        prompt = "Qual é a primeira letra da palavra BANANA?",
        alternatives = listOf(
            SessionAlternative(id = "b1", text = "A", isCorrect = false),
            SessionAlternative(id = "b2", text = "B", isCorrect = true),
            SessionAlternative(id = "b3", text = "C", isCorrect = false),
            SessionAlternative(id = "b4", text = "N", isCorrect = false),
        ),
    ),
    SessionTaskItem(
        id = "q3",
        imageRes = null,
        hasAudio = false,
        audioDurationLabel = null,
        prompt = "Quantas vogais tem a palavra ESCOLA?",
        alternatives = listOf(
            SessionAlternative(id = "c1", text = "2", isCorrect = false),
            SessionAlternative(id = "c2", text = "3", isCorrect = true),
            SessionAlternative(id = "c3", text = "4", isCorrect = false),
            SessionAlternative(id = "c4", text = "5", isCorrect = false),
        ),
    ),
)

@HiltViewModel
class SessionRunViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val studentId: String = checkNotNull(savedStateHandle["studentId"])
    private val contentIds: List<String> = checkNotNull(savedStateHandle.get<String>("contentIds"))
        .split(",").filter { it.isNotEmpty() }
    private val sessionName: String = checkNotNull(savedStateHandle.get<String>("sessionName"))

    private val _uiState = MutableStateFlow(
        SessionRunUiState(studentId = studentId, sessionName = sessionName)
    )
    val uiState: StateFlow<SessionRunUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    init {
        loadTasks()
        startTimer()
    }

    fun onAction(action: SessionRunAction) {
        when (action) {
            is SessionRunAction.OnAlternativeSelect -> {
                _uiState.update {
                    it.copy(selectedAlternativeId = action.alternativeId, canConfirm = true)
                }
            }
            SessionRunAction.OnConfirmAnswer -> confirmAnswer()
            SessionRunAction.OnNextActivity -> nextActivity()
            SessionRunAction.OnRetry -> {
                _uiState.update { it.copy(selectedAlternativeId = null, canConfirm = false, answerResult = null) }
            }
            SessionRunAction.OnToggleAudio -> _uiState.update { it.copy(isAudioPlaying = !it.isAudioPlaying) }
            SessionRunAction.OnToggleTimer -> _uiState.update { it.copy(isTimerRunning = !it.isTimerRunning) }
            SessionRunAction.OnZoomImage -> _uiState.update { it.copy(showImageZoom = true) }
            SessionRunAction.OnDismissZoom -> _uiState.update { it.copy(showImageZoom = false) }
        }
    }

    private fun loadTasks() {
        val count = contentIds.size.coerceAtLeast(1)
        val tasks = MOCK_TASKS.take(count)
        _uiState.update { it.copy(tasks = tasks) }
    }

    private fun confirmAnswer() {
        val state = _uiState.value
        val task = state.currentTask ?: return
        val selected = task.alternatives.firstOrNull { it.id == state.selectedAlternativeId } ?: return
        val result = if (selected.isCorrect) AnswerResult.CORRECT else AnswerResult.WRONG
        _uiState.update { it.copy(answerResult = result) }
    }

    private fun nextActivity() {
        _uiState.update {
            it.copy(
                currentTaskIndex = it.currentTaskIndex + 1,
                selectedAlternativeId = null,
                canConfirm = false,
                answerResult = null,
                isAudioPlaying = false,
            )
        }
    }

    private fun startTimer() {
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                val state = _uiState.value
                if (state.isFinished) break
                if (state.isTimerRunning) {
                    _uiState.update { it.copy(elapsedSeconds = it.elapsedSeconds + 1) }
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
