package com.labirintodosaber.data.repository

import com.labirintodosaber.data.model.Appointment
import com.labirintodosaber.data.remote.ApiCaller
import com.labirintodosaber.data.remote.ApiResult
import com.labirintodosaber.data.remote.dto.AppointmentCreateRequest
import com.labirintodosaber.data.remote.dto.AppointmentUpdateRequest
import com.labirintodosaber.data.remote.service.AppointmentApi
import javax.inject.Inject

/** Agendamentos entre educador e aluno. */
interface AppointmentRepository {

    suspend fun create(
        studentId: String,
        scheduledAt: String,
        observation: String? = null,
    ): ApiResult<Unit>

    suspend fun list(): ApiResult<List<Appointment>>

    suspend fun getById(id: String): ApiResult<Appointment>

    suspend fun update(id: String, request: AppointmentUpdateRequest): ApiResult<Unit>

    suspend fun delete(id: String): ApiResult<Unit>
}

class AppointmentRepositoryImpl @Inject constructor(
    private val api: AppointmentApi,
    private val apiCaller: ApiCaller,
) : AppointmentRepository {

    override suspend fun create(studentId: String, scheduledAt: String, observation: String?) =
        apiCaller.call { api.create(AppointmentCreateRequest(studentId, scheduledAt, observation)) }

    override suspend fun list() = apiCaller.call { api.list() }

    override suspend fun getById(id: String) = apiCaller.call { api.getById(id) }

    override suspend fun update(id: String, request: AppointmentUpdateRequest) =
        apiCaller.call { api.update(id, request) }

    override suspend fun delete(id: String) = apiCaller.call { api.delete(id) }
}
