package com.labirintodosaber.data.repository

import com.labirintodosaber.data.model.NotebookWithGroups
import com.labirintodosaber.data.model.TaskCategory
import com.labirintodosaber.data.model.TaskNotebook
import com.labirintodosaber.data.remote.ApiCaller
import com.labirintodosaber.data.remote.ApiResult
import com.labirintodosaber.data.remote.dto.TaskNotebookCreateRequest
import com.labirintodosaber.data.remote.dto.TaskNotebookUpdateRequest
import com.labirintodosaber.data.remote.service.TaskNotebookApi
import javax.inject.Inject

/** Cadernos de tarefas atribuíveis a sessões. */
interface TaskNotebookRepository {

    suspend fun create(request: TaskNotebookCreateRequest): ApiResult<TaskNotebook>

    suspend fun list(
        id: String? = null,
        educatorId: String? = null,
        category: TaskCategory? = null,
        descriptionContains: String? = null,
    ): ApiResult<List<NotebookWithGroups>>

    suspend fun update(request: TaskNotebookUpdateRequest): ApiResult<Unit>

    suspend fun delete(taskNotebookId: String): ApiResult<Unit>
}

class TaskNotebookRepositoryImpl @Inject constructor(
    private val api: TaskNotebookApi,
    private val apiCaller: ApiCaller,
) : TaskNotebookRepository {

    override suspend fun create(request: TaskNotebookCreateRequest) =
        apiCaller.call { api.create(request) }

    override suspend fun list(
        id: String?,
        educatorId: String?,
        category: TaskCategory?,
        descriptionContains: String?,
    ) = apiCaller.call { api.list(id, educatorId, category, descriptionContains) }

    override suspend fun update(request: TaskNotebookUpdateRequest) =
        apiCaller.call { api.update(request) }

    override suspend fun delete(taskNotebookId: String) = apiCaller.call { api.delete(taskNotebookId) }
}
