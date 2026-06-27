package com.labirintodosaber.ui.screen.sessionconfigure

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.ceil

enum class ContentType { NOTEBOOK, GROUP, ACTIVITY }

enum class ContentFilter { ALL, NOTEBOOKS, GROUPS, ACTIVITIES }

data class SessionContentItem(
    val id: String,
    val type: ContentType,
    val title: String,
    val description: String,
    val tags: List<String>,
)

data class SessionConfigureUiState(
    val studentId: String = "",
    val sessionName: String = "",
    val sessionNameError: Boolean = false,
    val contentQuery: String = "",
    val activeFilter: ContentFilter = ContentFilter.ALL,
    val allContent: List<SessionContentItem> = emptyList(),
    val currentPage: Int = 0,
    val totalPages: Int = 1,
    val selectedContentIds: Set<String> = emptySet(),
    val canStart: Boolean = false,
)

val SessionConfigureUiState.visibleContent: List<SessionContentItem>
    get() {
        val from = currentPage * PAGE_SIZE
        val to = minOf(from + PAGE_SIZE, allContent.size)
        return if (from >= allContent.size) emptyList() else allContent.subList(from, to)
    }

sealed interface SessionConfigureAction {
    data class OnSessionNameChange(val name: String) : SessionConfigureAction
    data class OnContentQueryChange(val query: String) : SessionConfigureAction
    data class OnFilterChange(val filter: ContentFilter) : SessionConfigureAction
    data class OnContentSelect(val id: String) : SessionConfigureAction
    data class OnPageChange(val page: Int) : SessionConfigureAction
    data object OnStartSession : SessionConfigureAction
    data object OnBack : SessionConfigureAction
}

sealed interface SessionConfigureEvent {
    data object NavigateBack : SessionConfigureEvent
    data class NavigateToRun(
        val studentId: String,
        val contentIds: String,
        val sessionName: String,
    ) : SessionConfigureEvent
}

private const val PAGE_SIZE = 3

private val MOCK_CONTENT_ITEMS = listOf(
    SessionContentItem(
        id = "nb1", type = ContentType.NOTEBOOK,
        title = "Alfabetização Divertida",
        description = "Atividades de reconhecimento de letras e sons",
        tags = listOf("Vocabulário", "Leitura"),
    ),
    SessionContentItem(
        id = "nb2", type = ContentType.NOTEBOOK,
        title = "Histórias Ilustradas",
        description = "Compreensão de histórias curtas com imagens",
        tags = listOf("Leitura", "Compreensão"),
    ),
    SessionContentItem(
        id = "g1", type = ContentType.GROUP,
        title = "Matemática Básica",
        description = "Exercícios de soma e subtração",
        tags = listOf("Cálculo"),
    ),
    SessionContentItem(
        id = "g2", type = ContentType.GROUP,
        title = "Vogais e Consoantes",
        description = "Identificação de vogais em palavras",
        tags = listOf("Vocabulário"),
    ),
    SessionContentItem(
        id = "t1", type = ContentType.ACTIVITY,
        title = "Qual animal a figura representa?",
        description = "Múltipla escolha com imagem",
        tags = listOf("Vocabulário"),
    ),
    SessionContentItem(
        id = "t2", type = ContentType.ACTIVITY,
        title = "Complete a palavra",
        description = "Preencha a letra que falta",
        tags = listOf("Escrita"),
    ),
    SessionContentItem(
        id = "t3", type = ContentType.ACTIVITY,
        title = "Quantos objetos há?",
        description = "Contagem visual de objetos",
        tags = listOf("Cálculo"),
    ),
)

@HiltViewModel
class SessionConfigureViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val studentId: String = checkNotNull(savedStateHandle["studentId"])

    private val _uiState = MutableStateFlow(SessionConfigureUiState(studentId = studentId))
    val uiState: StateFlow<SessionConfigureUiState> = _uiState.asStateFlow()

    private val _events = Channel<SessionConfigureEvent>(Channel.BUFFERED)
    val events: Flow<SessionConfigureEvent> = _events.receiveAsFlow()

    init {
        loadContent()
    }

    fun onAction(action: SessionConfigureAction) {
        when (action) {
            is SessionConfigureAction.OnSessionNameChange -> {
                _uiState.update { it.copy(sessionName = action.name, sessionNameError = false) }
                checkCanStart()
            }
            is SessionConfigureAction.OnContentQueryChange -> {
                _uiState.update { it.copy(contentQuery = action.query) }
                applyFilter()
            }
            is SessionConfigureAction.OnFilterChange -> {
                _uiState.update { it.copy(activeFilter = action.filter) }
                applyFilter()
            }
            is SessionConfigureAction.OnContentSelect -> {
                val current = _uiState.value.selectedContentIds
                val updated = if (action.id in current) current - action.id else current + action.id
                _uiState.update { it.copy(selectedContentIds = updated) }
                checkCanStart()
            }
            is SessionConfigureAction.OnPageChange -> _uiState.update { it.copy(currentPage = action.page) }
            SessionConfigureAction.OnStartSession -> startSession()
            SessionConfigureAction.OnBack -> viewModelScope.launch {
                _events.send(SessionConfigureEvent.NavigateBack)
            }
        }
    }

    private fun loadContent() {
        val total = ceil(MOCK_CONTENT_ITEMS.size / PAGE_SIZE.toDouble()).toInt()
        _uiState.update { it.copy(allContent = MOCK_CONTENT_ITEMS, totalPages = maxOf(1, total)) }
    }

    private fun applyFilter() {
        val state = _uiState.value
        var filtered = MOCK_CONTENT_ITEMS

        filtered = when (state.activeFilter) {
            ContentFilter.ALL -> filtered
            ContentFilter.NOTEBOOKS -> filtered.filter { it.type == ContentType.NOTEBOOK }
            ContentFilter.GROUPS -> filtered.filter { it.type == ContentType.GROUP }
            ContentFilter.ACTIVITIES -> filtered.filter { it.type == ContentType.ACTIVITY }
        }

        if (state.contentQuery.isNotBlank()) {
            filtered = filtered.filter { it.title.contains(state.contentQuery, ignoreCase = true) }
        }

        val total = ceil(filtered.size / PAGE_SIZE.toDouble()).toInt()
        _uiState.update { it.copy(allContent = filtered, currentPage = 0, totalPages = maxOf(1, total)) }
    }

    private fun checkCanStart() {
        _uiState.update { it.copy(canStart = it.selectedContentIds.isNotEmpty()) }
    }

    private fun startSession() {
        val state = _uiState.value
        if (state.sessionName.isBlank()) {
            _uiState.update { it.copy(sessionNameError = true) }
            return
        }
        if (state.selectedContentIds.isEmpty()) return
        viewModelScope.launch {
            _events.send(
                SessionConfigureEvent.NavigateToRun(
                    studentId = state.studentId,
                    contentIds = state.selectedContentIds.joinToString(","),
                    sessionName = state.sessionName,
                )
            )
        }
    }
}
