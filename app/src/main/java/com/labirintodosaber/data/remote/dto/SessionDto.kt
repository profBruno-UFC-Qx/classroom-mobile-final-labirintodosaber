package com.labirintodosaber.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class SessionStartRequest(
    val studentId: String,
    val name: String,
)

@Serializable
data class SessionAnswerRequest(
    val sessionId: String,
    val taskId: String,
    val selectedAlternativeId: String,
    val timeToAnswer: Int,
)

@Serializable
data class SessionFinishRequest(
    val sessionId: String,
)

@Serializable
data class SessionObservationRequest(
    val sessionId: String,
    val observation: String,
)
