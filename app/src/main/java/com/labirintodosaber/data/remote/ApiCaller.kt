package com.labirintodosaber.data.remote

import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Envolve chamadas Retrofit em [ApiResult], traduzindo exceções (HTTP, rede,
 * serialização) nos tipos de erro do domínio. Use nas implementações de repositório:
 *
 * ```kotlin
 * suspend fun signIn(...) = apiCaller.call { api.signIn(request) }
 * ```
 */
@Singleton
class ApiCaller @Inject constructor(
    private val json: Json,
) {
    suspend fun <T> call(block: suspend () -> T): ApiResult<T> =
        try {
            ApiResult.Success(block())
        } catch (e: HttpException) {
            toError(e)
        } catch (e: IOException) {
            ApiResult.Error(
                type = ApiErrorType.NETWORK,
                message = "Sem conexão com o servidor. Verifique sua internet e tente novamente.",
            )
        } catch (e: Exception) {
            ApiResult.Error(
                type = ApiErrorType.UNKNOWN,
                message = e.message ?: "Ocorreu um erro inesperado.",
            )
        }

    private fun toError(e: HttpException): ApiResult.Error {
        val code = e.code()
        val body = parseBody(e)
        val type = when (code) {
            400 -> ApiErrorType.BAD_REQUEST
            401 -> ApiErrorType.UNAUTHORIZED
            403 -> ApiErrorType.FORBIDDEN
            404 -> ApiErrorType.NOT_FOUND
            409, 422, 429 -> ApiErrorType.CONFLICT
            in 500..599 -> ApiErrorType.SERVER
            else -> ApiErrorType.UNKNOWN
        }
        val message = body?.message
            ?: body?.error
            ?: defaultMessage(type)
        return ApiResult.Error(
            type = type,
            message = message,
            httpCode = code,
            validationErrors = body?.errors ?: emptyList(),
        )
    }

    private fun parseBody(e: HttpException): ApiErrorBody? {
        val raw = e.response()?.errorBody()?.string()
        if (raw.isNullOrBlank()) return null
        return try {
            json.decodeFromString(ApiErrorBody.serializer(), raw)
        } catch (_: Exception) {
            ApiErrorBody(message = raw)
        }
    }

    private fun defaultMessage(type: ApiErrorType): String = when (type) {
        ApiErrorType.NETWORK -> "Sem conexão com o servidor."
        ApiErrorType.BAD_REQUEST -> "Dados inválidos."
        ApiErrorType.UNAUTHORIZED -> "Sessão expirada. Faça login novamente."
        ApiErrorType.FORBIDDEN -> "Você não tem permissão para esta ação."
        ApiErrorType.NOT_FOUND -> "Recurso não encontrado."
        ApiErrorType.CONFLICT -> "Não foi possível concluir a operação."
        ApiErrorType.SERVER -> "Erro no servidor. Tente novamente mais tarde."
        ApiErrorType.UNKNOWN -> "Ocorreu um erro inesperado."
    }
}
