package com.labirintodosaber.data.repository

import com.labirintodosaber.data.model.SessionReport
import com.labirintodosaber.data.model.StudentAnalysis
import com.labirintodosaber.data.model.StudentAnalysisReport
import com.labirintodosaber.data.model.TaskNotebookSession
import com.labirintodosaber.data.remote.ApiCaller
import com.labirintodosaber.data.remote.ApiResult
import com.labirintodosaber.data.remote.dto.SessionAnswerRequest
import com.labirintodosaber.data.remote.dto.SessionFinishRequest
import com.labirintodosaber.data.remote.dto.SessionObservationRequest
import com.labirintodosaber.data.remote.dto.SessionStartRequest
import com.labirintodosaber.data.remote.service.TaskNotebookSessionApi
import javax.inject.Inject

/** Sessões de resolução de cadernos por alunos, com relatórios e análises. */
interface SessionRepository {

    suspend fun start(studentId: String, name: String): ApiResult<TaskNotebookSession>

    suspend fun answer(
        sessionId: String,
        taskId: String,
        selectedAlternativeId: String,
        timeToAnswer: Int,
    ): ApiResult<TaskNotebookSession>

    suspend fun finish(sessionId: String): ApiResult<TaskNotebookSession>

    suspend fun addObservation(sessionId: String, observation: String): ApiResult<TaskNotebookSession>

    suspend fun listByStudent(studentId: String): ApiResult<List<TaskNotebookSession>>

    suspend fun report(sessionId: String): ApiResult<SessionReport>

    /** `limit` não pode ser combinado com `startDate`/`endDate`. */
    suspend fun analysis(
        studentId: String,
        startDate: String? = null,
        endDate: String? = null,
        limit: Int? = null,
    ): ApiResult<StudentAnalysis>

    suspend fun snapshot(
        studentId: String,
        startDate: String? = null,
        endDate: String? = null,
        limit: Int? = null,
    ): ApiResult<StudentAnalysisReport>

    suspend fun analysisHistory(studentId: String): ApiResult<List<StudentAnalysisReport>>
}

class SessionRepositoryImpl @Inject constructor(
    private val api: TaskNotebookSessionApi,
    private val apiCaller: ApiCaller,
) : SessionRepository {

    override suspend fun start(studentId: String, name: String) =
        apiCaller.call { api.start(SessionStartRequest(studentId, name)) }

    override suspend fun answer(
        sessionId: String,
        taskId: String,
        selectedAlternativeId: String,
        timeToAnswer: Int,
    ) = apiCaller.call {
        api.answer(SessionAnswerRequest(sessionId, taskId, selectedAlternativeId, timeToAnswer))
    }

    override suspend fun finish(sessionId: String) =
        apiCaller.call { api.finish(SessionFinishRequest(sessionId)) }

    override suspend fun addObservation(sessionId: String, observation: String) =
        apiCaller.call { api.observation(SessionObservationRequest(sessionId, observation)) }

    override suspend fun listByStudent(studentId: String) =
        apiCaller.call { api.listByStudent(studentId) }

    override suspend fun report(sessionId: String) = apiCaller.call { api.report(sessionId) }

    override suspend fun analysis(studentId: String, startDate: String?, endDate: String?, limit: Int?) =
        apiCaller.call { api.analysis(studentId, startDate, endDate, limit) }

    override suspend fun snapshot(studentId: String, startDate: String?, endDate: String?, limit: Int?) =
        apiCaller.call { api.snapshot(studentId, startDate, endDate, limit) }

    override suspend fun analysisHistory(studentId: String) =
        apiCaller.call { api.analysisHistory(studentId) }
}
