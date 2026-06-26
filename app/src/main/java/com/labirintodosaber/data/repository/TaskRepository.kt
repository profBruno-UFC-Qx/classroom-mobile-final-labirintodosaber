package com.labirintodosaber.data.repository

import com.labirintodosaber.data.model.Task
import com.labirintodosaber.data.model.TaskCategory
import com.labirintodosaber.data.model.TaskType
import com.labirintodosaber.data.remote.ApiCaller
import com.labirintodosaber.data.remote.ApiResult
import com.labirintodosaber.data.remote.FileUpload
import com.labirintodosaber.data.remote.MultipartFactory
import com.labirintodosaber.data.remote.dto.TaskForm
import com.labirintodosaber.data.remote.dto.TaskUpdateRequest
import com.labirintodosaber.data.remote.service.TaskApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

/** Tarefas (atividades) do educador. */
interface TaskRepository {

    suspend fun create(
        form: TaskForm,
        imageFile: FileUpload? = null,
        audioFile: FileUpload? = null,
    ): ApiResult<Unit>

    suspend fun list(
        id: String? = null,
        category: TaskCategory? = null,
        type: TaskType? = null,
        promptContains: String? = null,
    ): ApiResult<List<Task>>

    suspend fun getById(id: String): ApiResult<Task>

    suspend fun update(request: TaskUpdateRequest): ApiResult<Unit>

    suspend fun delete(id: String): ApiResult<Unit>
}

class TaskRepositoryImpl @Inject constructor(
    private val api: TaskApi,
    private val apiCaller: ApiCaller,
    private val json: Json,
) : TaskRepository {

    override suspend fun create(form: TaskForm, imageFile: FileUpload?, audioFile: FileUpload?) =
        apiCaller.call {
            api.create(
                fields = MultipartFactory.textParts(
                    mapOf(
                        "category" to json.encodeToString(TaskCategory.serializer(), form.category).trim('"'),
                        "type" to json.encodeToString(TaskType.serializer(), form.type).trim('"'),
                        "prompt" to form.prompt,
                        "alternatives" to json.encodeToString(form.alternatives),
                    ),
                ),
                imageFile = imageFile?.let { MultipartFactory.filePart("imageFile", it) },
                audioFile = audioFile?.let { MultipartFactory.filePart("audioFile", it) },
            )
        }

    override suspend fun list(
        id: String?,
        category: TaskCategory?,
        type: TaskType?,
        promptContains: String?,
    ) = apiCaller.call { api.list(id, category, type, promptContains) }

    override suspend fun getById(id: String) = apiCaller.call { api.getById(id) }

    override suspend fun update(request: TaskUpdateRequest) = apiCaller.call { api.update(request) }

    override suspend fun delete(id: String) = apiCaller.call { api.delete(id) }
}
