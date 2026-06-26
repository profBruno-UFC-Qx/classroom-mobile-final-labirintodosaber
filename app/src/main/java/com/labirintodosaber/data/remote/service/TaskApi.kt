package com.labirintodosaber.data.remote.service

import com.labirintodosaber.data.model.Task
import com.labirintodosaber.data.model.TaskCategory
import com.labirintodosaber.data.model.TaskType
import com.labirintodosaber.data.remote.dto.TaskUpdateRequest
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.PartMap
import retrofit2.http.Path
import retrofit2.http.Query

/** Endpoints de `/task` (todos exigem Bearer). */
interface TaskApi {

    /**
     * Campos escalares (`category`, `type`, `prompt`) e `alternatives` (string JSON)
     * vão em [fields]. [imageFile] e [audioFile] são opcionais (máx. 10 MB cada).
     */
    @Multipart
    @POST("task/create")
    suspend fun create(
        @PartMap fields: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part imageFile: MultipartBody.Part? = null,
        @Part audioFile: MultipartBody.Part? = null,
    )

    @GET("task/")
    suspend fun list(
        @Query("id") id: String? = null,
        @Query("category") category: TaskCategory? = null,
        @Query("type") type: TaskType? = null,
        @Query("promptContains") promptContains: String? = null,
    ): List<Task>

    @GET("task/{id}")
    suspend fun getById(@Path("id") id: String): Task

    @PUT("task/update")
    suspend fun update(@Body request: TaskUpdateRequest)

    @DELETE("task/delete/{id}")
    suspend fun delete(@Path("id") id: String)
}
