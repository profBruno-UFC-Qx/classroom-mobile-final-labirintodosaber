package com.labirintodosaber.data.remote.dto

import com.labirintodosaber.data.model.TaskCategory
import com.labirintodosaber.data.model.TaskType
import kotlinx.serialization.Serializable

/** Alternativa enviada em criação/atualização de tarefa (sem `id`, gerado no servidor). */
@Serializable
data class AlternativeInput(
    val text: String,
    val isCorrect: Boolean,
)

@Serializable
data class TaskUpdateRequest(
    val id: String,
    val category: TaskCategory? = null,
    val type: TaskType? = null,
    val prompt: String? = null,
    val alternatives: List<AlternativeInput>? = null,
    val imageFile: String? = null,
    val audioFile: String? = null,
)
