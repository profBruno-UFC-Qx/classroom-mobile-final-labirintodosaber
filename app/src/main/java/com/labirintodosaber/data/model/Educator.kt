package com.labirintodosaber.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Educator(
    val id: String,
    val name: String,
    val email: String,
    val photoUrl: String? = null,
    val contact: String? = null,
)

/** Item retornado por `GET /educator/get-last-sessions`. */
@Serializable
data class LastSession(
    val studentName: String? = null,
    val sessionName: String,
)
