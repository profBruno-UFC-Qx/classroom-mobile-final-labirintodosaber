package com.labirintodosaber.data.repository

import com.labirintodosaber.data.model.Gender
import com.labirintodosaber.data.model.Student
import com.labirintodosaber.data.remote.ApiCaller
import com.labirintodosaber.data.remote.ApiResult
import com.labirintodosaber.data.remote.FileUpload
import com.labirintodosaber.data.remote.MultipartFactory
import com.labirintodosaber.data.remote.dto.AssignEducatorRequest
import com.labirintodosaber.data.remote.dto.StudentForm
import com.labirintodosaber.data.remote.service.StudentApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

/** Alunos vinculados ao educador autenticado. */
interface StudentRepository {

    suspend fun create(form: StudentForm, photo: FileUpload? = null): ApiResult<Student>

    suspend fun update(id: String, form: StudentForm, photo: FileUpload? = null): ApiResult<Student>

    suspend fun assignEducator(studentId: String, newEducatorEmail: String): ApiResult<Student>

    suspend fun list(): ApiResult<List<Student>>
}

class StudentRepositoryImpl @Inject constructor(
    private val api: StudentApi,
    private val apiCaller: ApiCaller,
    private val json: Json,
) : StudentRepository {

    override suspend fun create(form: StudentForm, photo: FileUpload?) =
        apiCaller.call {
            api.create(buildFields(form), photo?.let { MultipartFactory.filePart("photo", it) })
        }

    override suspend fun update(id: String, form: StudentForm, photo: FileUpload?) =
        apiCaller.call {
            api.update(id, buildFields(form), photo?.let { MultipartFactory.filePart("photo", it) })
        }

    override suspend fun assignEducator(studentId: String, newEducatorEmail: String) =
        apiCaller.call { api.assignEducator(AssignEducatorRequest(studentId, newEducatorEmail)) }

    override suspend fun list() = apiCaller.call { api.list() }

    /** Converte o formulário para campos de formulário (escalares como string, arrays como JSON). */
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
