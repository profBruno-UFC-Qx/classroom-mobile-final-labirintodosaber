package com.labirintodosaber.data.repository

import com.labirintodosaber.data.model.AnamneseResponse
import com.labirintodosaber.data.model.AnamneseTemplate
import com.labirintodosaber.data.remote.ApiCaller
import com.labirintodosaber.data.remote.ApiResult
import com.labirintodosaber.data.remote.FileUpload
import com.labirintodosaber.data.remote.MultipartFactory
import com.labirintodosaber.data.remote.dto.AnamneseResponseRequest
import com.labirintodosaber.data.remote.dto.AnamneseTemplateRequest
import com.labirintodosaber.data.remote.dto.AnamneseTemplateUpdateRequest
import com.labirintodosaber.data.remote.service.AnamneseResponseApi
import com.labirintodosaber.data.remote.service.AnamneseTemplateApi
import javax.inject.Inject

/** Templates de anamnese e respostas vinculadas a alunos. */
interface AnamneseRepository {

    // Templates
    suspend fun createTemplate(request: AnamneseTemplateRequest): ApiResult<AnamneseTemplate>

    suspend fun listTemplates(): ApiResult<List<AnamneseTemplate>>

    suspend fun getTemplate(templateId: String): ApiResult<AnamneseTemplate>

    suspend fun updateTemplate(
        templateId: String,
        request: AnamneseTemplateUpdateRequest,
    ): ApiResult<AnamneseTemplate>

    suspend fun deleteTemplate(templateId: String): ApiResult<Unit>

    // Respostas
    suspend fun uploadFile(file: FileUpload): ApiResult<String>

    suspend fun createResponse(
        templateId: String,
        request: AnamneseResponseRequest,
    ): ApiResult<AnamneseResponse>

    suspend fun listResponsesByStudent(studentId: String): ApiResult<List<AnamneseResponse>>

    suspend fun getResponse(responseId: String): ApiResult<AnamneseResponse>
}

class AnamneseRepositoryImpl @Inject constructor(
    private val templateApi: AnamneseTemplateApi,
    private val responseApi: AnamneseResponseApi,
    private val apiCaller: ApiCaller,
) : AnamneseRepository {

    override suspend fun createTemplate(request: AnamneseTemplateRequest) =
        apiCaller.call { templateApi.create(request) }

    override suspend fun listTemplates() = apiCaller.call { templateApi.list() }

    override suspend fun getTemplate(templateId: String) =
        apiCaller.call { templateApi.getById(templateId) }

    override suspend fun updateTemplate(templateId: String, request: AnamneseTemplateUpdateRequest) =
        apiCaller.call { templateApi.update(templateId, request) }

    override suspend fun deleteTemplate(templateId: String) =
        apiCaller.call { templateApi.delete(templateId) }

    override suspend fun uploadFile(file: FileUpload): ApiResult<String> {
        val result = apiCaller.call { responseApi.uploadFile(MultipartFactory.filePart("file", file)) }
        return when (result) {
            is ApiResult.Success -> ApiResult.Success(result.data.url)
            is ApiResult.Error -> result
        }
    }

    override suspend fun createResponse(templateId: String, request: AnamneseResponseRequest) =
        apiCaller.call { responseApi.create(templateId, request) }

    override suspend fun listResponsesByStudent(studentId: String) =
        apiCaller.call { responseApi.listByStudent(studentId) }

    override suspend fun getResponse(responseId: String) =
        apiCaller.call { responseApi.getById(responseId) }
}
