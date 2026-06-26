package com.labirintodosaber.data.remote

import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

/**
 * Converte enums anotados com `@SerialName` para seu valor de fio em parâmetros
 * `@Query`/`@Path`. Sem isso, o Retrofit usaria `Enum.name` (ex.: `READING` em vez
 * de `reading`). Atua apenas em enums; demais tipos seguem o tratamento padrão.
 */
class SerialNameConverterFactory(private val json: Json) : Converter.Factory() {

    override fun stringConverter(
        type: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit,
    ): Converter<*, String>? {
        val clazz = type as? Class<*> ?: return null
        if (!clazz.isEnum) return null

        val serializer = json.serializersModule.serializer(type)
        return Converter<Any, String> { value ->
            json.encodeToString(serializer, value).trim('"')
        }
    }
}
