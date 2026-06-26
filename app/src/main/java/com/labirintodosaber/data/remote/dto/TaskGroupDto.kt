package com.labirintodosaber.data.remote.dto

import com.labirintodosaber.data.model.TaskCategory
import kotlinx.serialization.Serializable

@Serializable
data class TaskGroupCreateRequest(
    val name: String,
    val category: TaskCategory,
    val tasksIds: List<String>? = null,
)

@Serializable
data class TaskGroupUpdateRequest(
    val id: String,
    val name: String? = null,
    val tasksIds: List<String>? = null,
    val educatorId: String? = null,
    val category: TaskCategory? = null,
)
