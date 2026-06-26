package com.labirintodosaber.data.repository

import com.labirintodosaber.data.model.TaskCategory
import com.labirintodosaber.data.model.TaskGroup
import com.labirintodosaber.data.remote.ApiCaller
import com.labirintodosaber.data.remote.ApiResult
import com.labirintodosaber.data.remote.dto.TaskGroupCreateRequest
import com.labirintodosaber.data.remote.dto.TaskGroupUpdateRequest
import com.labirintodosaber.data.remote.service.TaskGroupApi
import javax.inject.Inject

/** Grupos de tarefas por educador. */
interface TaskGroupRepository {

    suspend fun create(
        name: String,
        category: TaskCategory,
        tasksIds: List<String>? = null,
    ): ApiResult<TaskGroup>

    suspend fun listByEducator(): ApiResult<List<TaskGroup>>

    suspend fun update(request: TaskGroupUpdateRequest): ApiResult<Unit>

    suspend fun delete(taskGroupId: String): ApiResult<Unit>
}

class TaskGroupRepositoryImpl @Inject constructor(
    private val api: TaskGroupApi,
    private val apiCaller: ApiCaller,
) : TaskGroupRepository {

    override suspend fun create(name: String, category: TaskCategory, tasksIds: List<String>?) =
        apiCaller.call { api.create(TaskGroupCreateRequest(name, category, tasksIds)) }

    override suspend fun listByEducator() = apiCaller.call { api.listByEducator() }

    override suspend fun update(request: TaskGroupUpdateRequest) =
        apiCaller.call { api.update(request) }

    override suspend fun delete(taskGroupId: String) = apiCaller.call { api.delete(taskGroupId) }
}
