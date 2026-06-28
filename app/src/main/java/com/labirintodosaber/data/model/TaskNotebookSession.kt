package com.labirintodosaber.data.model

import kotlinx.serialization.Serializable

@Serializable
data class TaskNotebookSession(
    val id: String,
    val studentId: String,
    val educatorId: String,
    val name: String,
    val startedAt: String,
    val finishedAt: String? = null, // ausente = sessão ativa
    val answers: List<SessionAnswer> = emptyList(),
    val observation: String? = null,
)

@Serializable
data class SessionAnswer(
    val taskId: String,
    val selectedAlternativeId: String,
    val isCorrect: Boolean,
    val timeToAnswer: Int,
    val answeredAt: String,
)

/** Resposta de `GET /task-notebook-session/report/:sessionId`. */
@Serializable
data class SessionReport(
    val sessionName: String,
    val totalTimeSession: Double? = null,
    val totalQuestions: Int,
    val averageTimePerQuestion: Double,
    val averageCorrectTime: Double? = null,
    val averageIncorrectTime: Double? = null,
    val percentageByCategory: Map<String, Double?> = emptyMap(),
    val percentageByType: Map<String, Double?> = emptyMap(),
    val observation: String? = null,
)

/** Análise por categoria usada tanto em [StudentAnalysis] quanto em [StudentAnalysisReport]. */
@Serializable
data class CategoryAnalysis(
    val category: TaskCategory,
    val total: Int,
    val correct: Int,
    val accuracy: Double,
)

/** Total agregado de uma análise. */
@Serializable
data class AnalysisTotal(
    val total: Int,
    val correct: Int,
    val accuracy: Double,
)

/** Resposta de `GET /task-notebook-session/analysis/student/:studentId`. */
@Serializable
data class StudentAnalysis(
    val categories: Map<String, CategoryAnalysis> = emptyMap(),
    val total: AnalysisTotal,
    val sessions: List<TaskNotebookSession> = emptyList(),
)

/** Snapshot persistido de análise (endpoints `/snapshot` e `/history`). */
@Serializable
data class StudentAnalysisReport(
    val studentId: String,
    val startDate: String? = null,
    val endDate: String? = null,
    val limit: Int? = null,
    val sessionIds: List<String> = emptyList(),
    val categories: List<CategoryAnalysis> = emptyList(),
    val totalQuestions: Int,
    val totalCorrect: Int,
    val accuracy: Double,
)
