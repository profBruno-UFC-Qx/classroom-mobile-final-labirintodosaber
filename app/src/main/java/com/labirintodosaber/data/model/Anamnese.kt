package com.labirintodosaber.data.model

import kotlinx.serialization.Serializable

@Serializable
data class AnamneseTemplate(
    val id: String,
    val educatorId: String,
    val title: String,
    val description: String? = null,
    val questions: List<AnamneseQuestion> = emptyList(),
    val createdAt: String,
)

@Serializable
data class AnamneseQuestion(
    val id: String,
    val text: String,
    val type: AnamneseQuestionType,
    val required: Boolean,
    val order: Int,
    val options: List<AnamneseOption> = emptyList(),
)

@Serializable
data class AnamneseOption(
    val id: String,
    val text: String,
)

@Serializable
data class AnamneseResponse(
    val id: String,
    val templateId: String,
    val educatorId: String,
    val studentId: String,
    val answers: List<AnamneseAnswer> = emptyList(),
    val answeredAt: String,
)

@Serializable
data class AnamneseAnswer(
    val questionId: String,
    val questionType: AnamneseQuestionType,
    val textValue: String? = null,
    val selectedOptionId: String? = null,
    val selectedOptionIds: List<String>? = null,
    val fileUrl: String? = null,
)
