package com.labirintodosaber.data.remote.service

import com.labirintodosaber.data.model.Student
import com.labirintodosaber.data.remote.dto.AssignEducatorRequest
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.PartMap
import retrofit2.http.Path

/** Endpoints de `/student` (todos exigem Bearer). */
interface StudentApi {

    /**
     * Campos escalares vão em [fields] (strings de formulário; `age` como string,
     * `learningTopics` como string JSON). [photo] é opcional (máx. 5 MB).
     */
    @Multipart
    @POST("student/create")
    suspend fun create(
        @PartMap fields: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part photo: MultipartBody.Part? = null,
    ): Student

    @Multipart
    @PUT("student/update/{id}")
    suspend fun update(
        @Path("id") id: String,
        @PartMap fields: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part photo: MultipartBody.Part? = null,
    ): Student

    @POST("student/assign-educator")
    suspend fun assignEducator(@Body request: AssignEducatorRequest): Student

    @GET("student/")
    suspend fun list(): List<Student>
}
