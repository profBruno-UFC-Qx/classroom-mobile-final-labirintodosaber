package com.labirintodosaber.data.remote.service

import com.labirintodosaber.data.model.AnamneseTemplate
import com.labirintodosaber.data.remote.dto.AnamneseTemplateRequest
import com.labirintodosaber.data.remote.dto.AnamneseTemplateUpdateRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/** Endpoints de `/anamnese/templates` (todos exigem Bearer). */
interface AnamneseTemplateApi {

    @POST("anamnese/templates/")
    suspend fun create(@Body request: AnamneseTemplateRequest): AnamneseTemplate

    @GET("anamnese/templates/")
    suspend fun list(): List<AnamneseTemplate>

    @GET("anamnese/templates/{templateId}")
    suspend fun getById(@Path("templateId") templateId: String): AnamneseTemplate

    @PUT("anamnese/templates/{templateId}")
    suspend fun update(
        @Path("templateId") templateId: String,
        @Body request: AnamneseTemplateUpdateRequest,
    ): AnamneseTemplate

    @DELETE("anamnese/templates/{templateId}")
    suspend fun delete(@Path("templateId") templateId: String)
}
