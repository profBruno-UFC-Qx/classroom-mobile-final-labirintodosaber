package com.labirintodosaber.data.remote

import kotlinx.serialization.Serializable

/**
 * Corpo de erro padrão da API. Cobre os três formatos documentados:
 * - `{ "message": "<msg>" }`
 * - `{ "message": "Validation error", "errors": [ { "path": ..., "message": ... } ] }`
 * - `{ "message": "An unexpected error occurred", "error": "<detalhe>" }`
 */
@Serializable
data class ApiErrorBody(
    val message: String? = null,
    val error: String? = null,
    val errors: List<ValidationError> = emptyList(),
)

@Serializable
data class ValidationError(
    val path: String? = null,
    val message: String? = null,
)
