package com.labirintodosaber.data.remote.service

import com.labirintodosaber.data.model.AnamneseResponse
import com.labirintodosaber.data.remote.dto.AnamneseResponseRequest
import com.labirintodosaber.data.remote.dto.UploadFileResponse
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

/** Endpoints de respostas de `/anamnese` (todos exigem Bearer). */
interface AnamneseResponseApi {

    @Multipart
    @POST("anamnese/responses/upload-file")
    suspend fun uploadFile(@Part file: MultipartBody.Part): UploadFileResponse

    @POST("anamnese/templates/{templateId}/responses")
    suspend fun create(
        @Path("templateId") templateId: String,
        @Body request: AnamneseResponseRequest,
    ): AnamneseResponse

    @GET("anamnese/responses/student/{studentId}")
    suspend fun listByStudent(@Path("studentId") studentId: String): List<AnamneseResponse>

    @GET("anamnese/responses/{responseId}")
    suspend fun getById(@Path("responseId") responseId: String): AnamneseResponse
}
