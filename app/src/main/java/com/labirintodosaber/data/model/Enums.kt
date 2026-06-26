package com.labirintodosaber.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Gênero do aluno. */
@Serializable
enum class Gender {
    @SerialName("male")
    MALE,

    @SerialName("female")
    FEMALE,
}

/** Categoria de tarefa / caderno. */
@Serializable
enum class TaskCategory {
    @SerialName("reading")
    READING,

    @SerialName("writing")
    WRITING,

    @SerialName("vocabulary")
    VOCABULARY,

    @SerialName("comprehension")
    COMPREHENSION,
}

/** Tipo de tarefa. */
@Serializable
enum class TaskType {
    @SerialName("multipleChoice")
    MULTIPLE_CHOICE,

    @SerialName("multipleChoiceWithMedia")
    MULTIPLE_CHOICE_WITH_MEDIA,
}

/** Tipo de pergunta de anamnese. */
@Serializable
enum class AnamneseQuestionType {
    @SerialName("Descriptive")
    DESCRIPTIVE,

    @SerialName("MultipleChoice")
    MULTIPLE_CHOICE,

    @SerialName("Checkbox")
    CHECKBOX,

    @SerialName("FileUpload")
    FILE_UPLOAD,
}

/** Status de agendamento. */
@Serializable
enum class AppointmentStatus {
    @SerialName("PENDING")
    PENDING,

    @SerialName("COMPLETED")
    COMPLETED,

    @SerialName("CANCELLED")
    CANCELLED,
}
