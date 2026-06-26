package com.labirintodosaber.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Task(
    val id: String,
    val category: TaskCategory,
    val type: TaskType,
    val prompt: String,
    val alternatives: List<Alternative> = emptyList(),
    val createdAt: String,
    val imageFile: String? = null,
    val audioFile: String? = null,
)

@Serializable
data class Alternative(
    val id: String,
    val text: String,
    val isCorrect: Boolean,
)
