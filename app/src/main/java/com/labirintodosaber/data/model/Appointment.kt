package com.labirintodosaber.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Appointment(
    val id: String,
    val educatorId: String,
    val studentId: String,
    val scheduledAt: String, // ISO datetime
    val observation: String? = null,
    val status: AppointmentStatus = AppointmentStatus.PENDING,
    val notifiedAt: String? = null,
    val createdAt: String,
)
