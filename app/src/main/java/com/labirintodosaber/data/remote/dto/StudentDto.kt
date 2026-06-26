package com.labirintodosaber.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class AssignEducatorRequest(
    val studentId: String,
    val newEducatorEmail: String,
)
