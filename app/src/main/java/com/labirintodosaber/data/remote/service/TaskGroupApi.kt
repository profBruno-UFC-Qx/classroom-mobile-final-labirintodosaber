package com.labirintodosaber.data.remote.service

import com.labirintodosaber.data.model.TaskGroup
import com.labirintodosaber.data.remote.dto.TaskGroupCreateRequest
import com.labirintodosaber.data.remote.dto.TaskGroupUpdateRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/** Endpoints de `/task-group` (todos exigem Bearer). */
interface TaskGroupApi {

    @POST("task-group/create")
    suspend fun create(@Body request: TaskGroupCreateRequest): TaskGroup

    @GET("task-group/list-by-educator")
    suspend fun listByEducator(): List<TaskGroup>

    @PUT("task-group/update")
    suspend fun update(@Body request: TaskGroupUpdateRequest)

    @DELETE("task-group/delete/{taskGroupId}")
    suspend fun delete(@Path("taskGroupId") taskGroupId: String)
}
