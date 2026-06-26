package com.labirintodosaber.data.remote.service

import com.labirintodosaber.data.model.NotebookWithGroups
import com.labirintodosaber.data.model.TaskCategory
import com.labirintodosaber.data.model.TaskNotebook
import com.labirintodosaber.data.remote.dto.TaskNotebookCreateRequest
import com.labirintodosaber.data.remote.dto.TaskNotebookUpdateRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/** Endpoints de `/task-notebook` (todos exigem Bearer). */
interface TaskNotebookApi {

    @POST("task-notebook/create")
    suspend fun create(@Body request: TaskNotebookCreateRequest): TaskNotebook

    @GET("task-notebook/")
    suspend fun list(
        @Query("id") id: String? = null,
        @Query("educatorId") educatorId: String? = null,
        @Query("category") category: TaskCategory? = null,
        @Query("descriptionContains") descriptionContains: String? = null,
    ): List<NotebookWithGroups>

    @PUT("task-notebook/update")
    suspend fun update(@Body request: TaskNotebookUpdateRequest)

    @DELETE("task-notebook/delete/{taskNotebookId}")
    suspend fun delete(@Path("taskNotebookId") taskNotebookId: String)
}
