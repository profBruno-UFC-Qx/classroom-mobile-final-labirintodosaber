package com.labirintodosaber.ui.screen.sessionconfigure

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.labirintodosaber.data.remote.getOrNull
import com.labirintodosaber.data.repository.TaskGroupRepository
import com.labirintodosaber.data.repository.TaskNotebookRepository
import com.labirintodosaber.data.repository.TaskRepository
import com.labirintodosaber.ui.screen.activities.displayName
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
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
    val taskIds: List<String>,
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
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
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
    data object OnRetry : SessionConfigureAction
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

@HiltViewModel
class SessionConfigureViewModel @Inject constructor(
    private val taskNotebookRepository: TaskNotebookRepository,
    private val taskGroupRepository: TaskGroupRepository,
    private val taskRepository: TaskRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val studentId: String = checkNotNull(savedStateHandle["studentId"])

    private val _uiState = MutableStateFlow(SessionConfigureUiState(studentId = studentId))
    val uiState: StateFlow<SessionConfigureUiState> = _uiState.asStateFlow()

    private val _events = Channel<SessionConfigureEvent>(Channel.BUFFERED)
    val events: Flow<SessionConfigureEvent> = _events.receiveAsFlow()

    /** Conteúdo completo carregado; filtro/busca operam sobre ele. */
    private var loadedContent: List<SessionContentItem> = emptyList()

    init {
        loadContent()
    }

    fun onAction(action: SessionConfigureAction) {
        when (action) {
            is SessionConfigureAction.OnSessionNameChange -> {
                _uiState.update { it.copy(sessionName = action.name, sessionNameError = false) }
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
            SessionConfigureAction.OnRetry -> loadContent()
            SessionConfigureAction.OnBack -> viewModelScope.launch {
                _events.send(SessionConfigureEvent.NavigateBack)
            }
        }
    }

    private fun loadContent() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val notebooksDeferred = async { taskNotebookRepository.list().getOrNull() }
            val groupsDeferred = async { taskGroupRepository.listByEducator().getOrNull() }
            val tasksDeferred = async { taskRepository.list().getOrNull() }

            val notebooks = notebooksDeferred.await()
            val groups = groupsDeferred.await()
            val tasks = tasksDeferred.await()

            if (notebooks == null || groups == null || tasks == null) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Não foi possível carregar o conteúdo.") }
                return@launch
            }

            val items = buildList {
                notebooks.forEach { nb ->
                    add(
                        SessionContentItem(
                            id = nb.notebook.id,
                            type = ContentType.NOTEBOOK,
                            title = nb.notebook.description,
                            description = "${nb.notebook.tasks.size} atividades",
                            tags = listOf(nb.notebook.category.displayName()),
                            taskIds = nb.notebook.tasks,
                        )
                    )
                }
                groups.forEach { g ->
                    add(
                        SessionContentItem(
                            id = g.id,
                            type = ContentType.GROUP,
                            title = g.name,
                            description = "${g.tasksIds.size} atividades",
                            tags = listOf(g.category.displayName()),
                            taskIds = g.tasksIds,
                        )
                    )
                }
                tasks.forEach { t ->
                    add(
                        SessionContentItem(
                            id = t.id,
                            type = ContentType.ACTIVITY,
                            title = t.prompt,
                            description = "${t.alternatives.size} alternativas",
                            tags = listOf(t.category.displayName()),
                            taskIds = listOf(t.id),
                        )
                    )
                }
            }

            loadedContent = items
            _uiState.update { it.copy(isLoading = false) }
            applyFilter()
        }
    }

    private fun applyFilter() {
        val state = _uiState.value
        var filtered = loadedContent

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
        // Resolve o conteúdo selecionado (cadernos/grupos/atividades) numa lista de task ids.
        val taskIds = loadedContent
            .filter { it.id in state.selectedContentIds }
            .flatMap { it.taskIds }
            .distinct()
        if (taskIds.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "O conteúdo selecionado não tem atividades.") }
            return
        }
        viewModelScope.launch {
            _events.send(
                SessionConfigureEvent.NavigateToRun(
                    studentId = state.studentId,
                    contentIds = taskIds.joinToString(","),
                    sessionName = state.sessionName,
                )
            )
        }
    }
}
