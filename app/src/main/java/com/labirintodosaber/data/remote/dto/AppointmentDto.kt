package com.labirintodosaber.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class AppointmentCreateRequest(
    val studentId: String,
    val scheduledAt: String, // ISO datetime
    val observation: String? = null,
)

@Serializable
data class AppointmentUpdateRequest(
    val scheduledAt: String? = null,
    val observation: String? = null, // a API limpa a observação quando recebe null
)
