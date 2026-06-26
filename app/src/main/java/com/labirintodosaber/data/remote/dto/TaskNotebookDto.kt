package com.labirintodosaber.data.remote.dto

import com.labirintodosaber.data.model.TaskCategory
import kotlinx.serialization.Serializable

@Serializable
data class TaskNotebookCreateRequest(
    val tasks: List<String>,
    val category: TaskCategory,
    val description: String,
    val taskGroupsIds: List<String>? = null,
)

@Serializable
data class TaskNotebookUpdateRequest(
    val taskNotebookId: String,
    val category: TaskCategory? = null,
    val description: String? = null,
    val taskGroupsIds: List<String>? = null,
)
