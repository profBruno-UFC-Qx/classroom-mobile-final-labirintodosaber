package com.labirintodosaber.data.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.decodeFromJsonElement

@Serializable
data class TaskNotebook(
    val id: String,
    @Serializable(with = FlexibleEducatorSerializer::class)
    val educator: Educator,
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

/**
 * O endpoint de listagem retorna `educator` como UUID string.
 * O endpoint de criação retorna um objeto `Educator` completo.
 * Este serializer aceita ambos os formatos.
 */
internal object FlexibleEducatorSerializer : KSerializer<Educator> {
    override val descriptor = buildClassSerialDescriptor("Educator")

    override fun deserialize(decoder: Decoder): Educator {
        val jsonDecoder = decoder as? JsonDecoder
            ?: throw SerializationException("Only JSON format is supported")
        return when (val element = jsonDecoder.decodeJsonElement()) {
            is JsonPrimitive -> Educator(id = element.content, name = "", email = "")
            is JsonObject -> jsonDecoder.json.decodeFromJsonElement(element)
            else -> throw SerializationException("Unexpected JSON element for educator field")
        }
    }

    override fun serialize(encoder: Encoder, value: Educator) {
        encoder.encodeString(value.id)
    }
}
