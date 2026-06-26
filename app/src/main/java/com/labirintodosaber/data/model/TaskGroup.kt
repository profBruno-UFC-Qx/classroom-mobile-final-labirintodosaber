package com.labirintodosaber.data.model

import kotlinx.serialization.Serializable

@Serializable
data class TaskGroup(
    val id: String,
    val name: String,
    val tasksIds: List<String> = emptyList(),
    val educatorId: String,
    val category: TaskCategory,
)
