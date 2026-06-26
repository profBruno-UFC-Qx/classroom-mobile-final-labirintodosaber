package com.labirintodosaber.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class SignInRequest(
    val email: String,
    val password: String,
)

@Serializable
data class AuthTokenResponse(
    val token: String,
)

@Serializable
data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
)

@Serializable
data class RegisterResponse(
    val id: String,
    val name: String,
)

@Serializable
data class UpdatePasswordRequest(
    val email: String,
    val newPassword: String,
)

@Serializable
data class GenerateTokenRequest(
    val educatorEmail: String,
)

@Serializable
data class UpdateEducatorRequest(
    val newName: String? = null,
    val newContact: String? = null,
)
