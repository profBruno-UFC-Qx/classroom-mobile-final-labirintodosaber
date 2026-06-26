package com.labirintodosaber.data.model

import kotlinx.serialization.Serializable

@Serializable
data class TaskNotebook(
    val id: String,
    val educator: String,
    val tasks: List<String> = emptyList(),
    val category: TaskCategory,
    val description: String,
    val createdAt: String,
    val taskGroupsIds: List<String> = emptyList(),
)

/** Item retornado por `GET /task-notebook/` (caderno + seus grupos resolvidos). */
@Serializable
data class NotebookWithGroups(
    val notebook: TaskNotebook,
    val taskGroups: List<TaskGroup> = emptyList(),
)
