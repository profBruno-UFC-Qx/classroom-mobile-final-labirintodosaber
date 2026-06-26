package com.labirintodosaber.ui.screen.sessionrun

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.labirintodosaber.data.model.Task
import com.labirintodosaber.data.remote.ApiResult
import com.labirintodosaber.data.remote.getOrNull
import com.labirintodosaber.data.repository.SessionRepository
import com.labirintodosaber.data.repository.TaskRepository
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
    val imageUrl: String?,
    val hasAudio: Boolean,
    val audioDurationLabel: String?,
    val prompt: String,
    val alternatives: List<SessionAlternative>,
)

enum class AnswerResult { CORRECT, WRONG }

data class SessionRunUiState(
    val studentId: String = "",
    val sessionId: String? = null,
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
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val navigateSessionId: String? = null,
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
    data object OnFinishSession : SessionRunAction
    data object OnRetryLoad : SessionRunAction
}

@HiltViewModel
class SessionRunViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val sessionRepository: SessionRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val studentId: String = checkNotNull(savedStateHandle["studentId"])
    private val taskIds: List<String> = checkNotNull(savedStateHandle.get<String>("contentIds"))
        .split(",").filter { it.isNotEmpty() }
    private val sessionName: String = checkNotNull(savedStateHandle.get<String>("sessionName"))

    private val _uiState = MutableStateFlow(
        SessionRunUiState(studentId = studentId, sessionName = sessionName)
    )
    val uiState: StateFlow<SessionRunUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    /** Segundo (no cronômetro) em que a tarefa atual começou a ser respondida. */
    private var questionStartSeconds: Int = 0

    init {
        startSession()
    }

    fun onAction(action: SessionRunAction) {
        when (action) {
            is SessionRunAction.OnAlternativeSelect -> {
                if (_uiState.value.answerResult == null) {
                    _uiState.update { it.copy(selectedAlternativeId = action.alternativeId, canConfirm = true) }
                }
            }
            SessionRunAction.OnConfirmAnswer -> confirmAnswer()
            SessionRunAction.OnNextActivity -> nextActivity()
            SessionRunAction.OnRetry -> _uiState.update {
                it.copy(selectedAlternativeId = null, canConfirm = false, answerResult = null)
            }
            SessionRunAction.OnToggleAudio -> _uiState.update { it.copy(isAudioPlaying = !it.isAudioPlaying) }
            SessionRunAction.OnToggleTimer -> _uiState.update { it.copy(isTimerRunning = !it.isTimerRunning) }
            SessionRunAction.OnZoomImage -> _uiState.update { it.copy(showImageZoom = true) }
            SessionRunAction.OnDismissZoom -> _uiState.update { it.copy(showImageZoom = false) }
            SessionRunAction.OnFinishSession -> finishSession()
            SessionRunAction.OnRetryLoad -> startSession()
        }
    }

    private fun startSession() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            // Inicia a sessão (obtém o id) e carrega as tarefas escolhidas.
            val startResult = sessionRepository.start(studentId, sessionName)
            if (startResult is ApiResult.Error) {
                _uiState.update { it.copy(isLoading = false, errorMessage = startResult.message) }
                return@launch
            }
            val sessionId = (startResult as ApiResult.Success).data.id

            val allTasks = taskRepository.list().getOrNull()
            if (allTasks == null) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Não foi possível carregar as atividades.") }
                return@launch
            }
            // Preserva a ordem de seleção das tarefas.
            val byId = allTasks.associateBy { it.id }
            val tasks = taskIds.mapNotNull { byId[it] }.map { it.toSessionItem() }
            if (tasks.isEmpty()) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Nenhuma atividade encontrada para esta sessão.") }
                return@launch
            }

            questionStartSeconds = 0
            _uiState.update {
                it.copy(isLoading = false, sessionId = sessionId, tasks = tasks, elapsedSeconds = 0)
            }
            startTimer()
        }
    }

    private fun confirmAnswer() {
        val state = _uiState.value
        val task = state.currentTask ?: return
        val selected = task.alternatives.firstOrNull { it.id == state.selectedAlternativeId } ?: return
        val result = if (selected.isCorrect) AnswerResult.CORRECT else AnswerResult.WRONG
        _uiState.update { it.copy(answerResult = result) }

        val sessionId = state.sessionId ?: return
        val timeToAnswer = (state.elapsedSeconds - questionStartSeconds).coerceAtLeast(0)
        // Envia a resposta à API (sem bloquear o feedback local).
        viewModelScope.launch {
            sessionRepository.answer(
                sessionId = sessionId,
                taskId = task.id,
                selectedAlternativeId = selected.id,
                timeToAnswer = timeToAnswer,
            )
        }
    }

    private fun nextActivity() {
        questionStartSeconds = _uiState.value.elapsedSeconds
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

    private fun finishSession() {
        val sessionId = _uiState.value.sessionId
        viewModelScope.launch {
            if (sessionId != null) {
                sessionRepository.finish(sessionId)
            }
            _uiState.update { it.copy(navigateSessionId = sessionId ?: "") }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
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

private fun Task.toSessionItem() = SessionTaskItem(
    id = id,
    imageUrl = imageFile?.takeIf { it.isNotBlank() },
    hasAudio = !audioFile.isNullOrBlank(),
    audioDurationLabel = if (!audioFile.isNullOrBlank()) "Áudio" else null,
    prompt = prompt,
    alternatives = alternatives.map { SessionAlternative(it.id, it.text, it.isCorrect) },
)
