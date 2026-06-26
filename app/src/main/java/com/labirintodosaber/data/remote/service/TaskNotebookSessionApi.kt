package com.labirintodosaber.data.remote.service

import com.labirintodosaber.data.model.SessionReport
import com.labirintodosaber.data.model.StudentAnalysis
import com.labirintodosaber.data.model.StudentAnalysisReport
import com.labirintodosaber.data.model.TaskNotebookSession
import com.labirintodosaber.data.remote.dto.SessionAnswerRequest
import com.labirintodosaber.data.remote.dto.SessionFinishRequest
import com.labirintodosaber.data.remote.dto.SessionObservationRequest
import com.labirintodosaber.data.remote.dto.SessionStartRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/** Endpoints de `/task-notebook-session` (todos exigem Bearer). */
interface TaskNotebookSessionApi {

    @POST("task-notebook-session/start")
    suspend fun start(@Body request: SessionStartRequest): TaskNotebookSession

    @POST("task-notebook-session/answer")
    suspend fun answer(@Body request: SessionAnswerRequest): TaskNotebookSession

    @POST("task-notebook-session/finish")
    suspend fun finish(@Body request: SessionFinishRequest): TaskNotebookSession

    @POST("task-notebook-session/observation")
    suspend fun observation(@Body request: SessionObservationRequest): TaskNotebookSession

    @GET("task-notebook-session/student/{studentId}")
    suspend fun listByStudent(@Path("studentId") studentId: String): List<TaskNotebookSession>

    @GET("task-notebook-session/report/{sessionId}")
    suspend fun report(@Path("sessionId") sessionId: String): SessionReport

    /** `limit` não pode ser combinado com `startDate`/`endDate`. */
    @GET("task-notebook-session/analysis/student/{studentId}")
    suspend fun analysis(
        @Path("studentId") studentId: String,
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null,
        @Query("limit") limit: Int? = null,
    ): StudentAnalysis

    @POST("task-notebook-session/analysis/student/{studentId}/snapshot")
    suspend fun snapshot(
        @Path("studentId") studentId: String,
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null,
        @Query("limit") limit: Int? = null,
    ): StudentAnalysisReport

    @GET("task-notebook-session/analysis/student/{studentId}/history")
    suspend fun analysisHistory(@Path("studentId") studentId: String): List<StudentAnalysisReport>
}
