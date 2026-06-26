package com.labirintodosaber.data.remote.dto

import com.labirintodosaber.data.model.AnamneseQuestionType
import kotlinx.serialization.Serializable

@Serializable
data class OptionInput(
    val text: String,
)

@Serializable
data class QuestionInput(
    val text: String,
    val type: AnamneseQuestionType,
    val required: Boolean,
    val options: List<OptionInput>? = null,
)

@Serializable
data class AnamneseTemplateRequest(
    val title: String,
    val description: String? = null,
    val questions: List<QuestionInput>,
)

/** Atualização de template: todos os campos opcionais. */
@Serializable
data class AnamneseTemplateUpdateRequest(
    val title: String? = null,
    val description: String? = null,
    val questions: List<QuestionInput>? = null,
)

@Serializable
data class AnswerInput(
    val questionId: String,
    val textValue: String? = null,
    val selectedOptionId: String? = null,
    val selectedOptionIds: List<String>? = null,
    val fileUrl: String? = null,
)

@Serializable
data class AnamneseResponseRequest(
    val studentId: String,
    val answers: List<AnswerInput>,
)

@Serializable
data class UploadFileResponse(
    val url: String,
)
