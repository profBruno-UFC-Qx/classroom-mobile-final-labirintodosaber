package com.labirintodosaber.data.repository

import com.labirintodosaber.data.di.ApplicationScope
import com.labirintodosaber.data.local.dao.StudentDao
import com.labirintodosaber.data.local.entity.toDomain
import com.labirintodosaber.data.local.entity.toEntity
import com.labirintodosaber.data.model.Gender
import com.labirintodosaber.data.model.Student
import com.labirintodosaber.data.remote.ApiCaller
import com.labirintodosaber.data.remote.ApiResult
import com.labirintodosaber.data.remote.FileUpload
import com.labirintodosaber.data.remote.MultipartFactory
import com.labirintodosaber.data.remote.dto.AssignEducatorRequest
import com.labirintodosaber.data.remote.dto.StudentForm
import com.labirintodosaber.data.remote.service.StudentApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

interface StudentRepository {

    suspend fun create(form: StudentForm, photo: FileUpload? = null): ApiResult<Student>

    suspend fun update(id: String, form: StudentForm, photo: FileUpload? = null): ApiResult<Student>

    suspend fun assignEducator(studentId: String, newEducatorEmail: String): ApiResult<Student>

    /**
     * Retorna alunos em cache imediatamente (se disponível) e dispara
     * atualização em background. Na primeira chamada (cache vazio) aguarda a API.
     */
    suspend fun list(): ApiResult<List<Student>>
}

class StudentRepositoryImpl @Inject constructor(
    private val api: StudentApi,
    private val apiCaller: ApiCaller,
    private val json: Json,
    private val dao: StudentDao,
    @ApplicationScope private val appScope: CoroutineScope,
) : StudentRepository {

    override suspend fun list(): ApiResult<List<Student>> {
        val cached = dao.getAll()
        return if (cached.isNotEmpty()) {
            appScope.launch { syncFromApi() }
            ApiResult.Success(cached.map { it.toDomain(json) })
        } else {
            syncFromApi()
        }
    }

    private suspend fun syncFromApi(): ApiResult<List<Student>> =
        apiCaller.call { api.list() }.also { result ->
            if (result is ApiResult.Success) {
                dao.replaceAll(result.data.map { it.toEntity(json) })
            }
        }

    override suspend fun create(form: StudentForm, photo: FileUpload?) =
        apiCaller.call {
            api.create(buildFields(form), photo?.let { MultipartFactory.filePart("photo", it) })
        }.also { result ->
            if (result is ApiResult.Success) dao.upsert(result.data.toEntity(json))
        }

    override suspend fun update(id: String, form: StudentForm, photo: FileUpload?) =
        apiCaller.call {
            api.update(id, buildFields(form), photo?.let { MultipartFactory.filePart("photo", it) })
        }.also { result ->
            if (result is ApiResult.Success) dao.upsert(result.data.toEntity(json))
        }

    override suspend fun assignEducator(studentId: String, newEducatorEmail: String) =
        apiCaller.call { api.assignEducator(AssignEducatorRequest(studentId, newEducatorEmail)) }
            .also { result ->
                if (result is ApiResult.Success) dao.upsert(result.data.toEntity(json))
            }

    private fun buildFields(form: StudentForm) = MultipartFactory.textParts(
        mapOf(
            "name" to form.name,
            "age" to form.age?.toString(),
            "gender" to form.gender?.let { json.encodeToString(Gender.serializer(), it).trim('"') },
            "zipcode" to form.zipcode,
            "road" to form.road,
            "housenumber" to form.housenumber,
            "phonenumber" to form.phonenumber,
            "learningTopics" to form.learningTopics?.let { json.encodeToString(it) },
        ),
    )
}
