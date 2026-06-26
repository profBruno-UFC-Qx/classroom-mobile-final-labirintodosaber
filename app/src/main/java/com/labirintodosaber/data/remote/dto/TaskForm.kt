package com.labirintodosaber.data.remote.dto

import com.labirintodosaber.data.model.TaskCategory
import com.labirintodosaber.data.model.TaskType

/**
 * Campos de criação de tarefa (enviados como `multipart/form-data`).
 * `alternatives` precisa de no mínimo 2 itens, com ao menos um correto.
 */
data class TaskForm(
    val category: TaskCategory,
    val type: TaskType,
    val prompt: String,
    val alternatives: List<AlternativeInput>,
)
