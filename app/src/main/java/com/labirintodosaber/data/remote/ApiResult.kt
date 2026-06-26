package com.labirintodosaber.data.remote

/**
 * Resultado de uma chamada à API. Encapsula sucesso ou falha sem lançar exceções,
 * permitindo que os ViewModels tratem erros com `when` exaustivo.
 */
sealed interface ApiResult<out T> {

    data class Success<T>(val data: T) : ApiResult<T>

    /**
     * Falha tratada. [type] classifica o erro para decisões de UI (ex.: deslogar em
     * [ApiErrorType.UNAUTHORIZED]); [message] é a mensagem pronta para exibição.
     */
    data class Error(
        val type: ApiErrorType,
        val message: String,
        val httpCode: Int? = null,
        val validationErrors: List<ValidationError> = emptyList(),
    ) : ApiResult<Nothing>

    val isSuccess: Boolean get() = this is Success
}

/** Categoria de erro derivada do status HTTP / falha de rede. */
enum class ApiErrorType {
    /** Sem conexão, timeout ou host inacessível. */
    NETWORK,

    /** `400` — validação ou erro de negócio. */
    BAD_REQUEST,

    /** `401` — token ausente, inválido ou expirado. */
    UNAUTHORIZED,

    /** `403` — autenticado, mas sem permissão. */
    FORBIDDEN,

    /** `404` — recurso não encontrado. */
    NOT_FOUND,

    /** `409` / `422` / `429`. */
    CONFLICT,

    /** `500` e demais erros inesperados do servidor. */
    SERVER,

    /** Qualquer outra falha não classificada. */
    UNKNOWN,
}

/** Executa [block] apenas em caso de sucesso, preservando o resultado. */
inline fun <T> ApiResult<T>.onSuccess(block: (T) -> Unit): ApiResult<T> {
    if (this is ApiResult.Success) block(data)
    return this
}

/** Executa [block] apenas em caso de erro, preservando o resultado. */
inline fun <T> ApiResult<T>.onError(block: (ApiResult.Error) -> Unit): ApiResult<T> {
    if (this is ApiResult.Error) block(this)
    return this
}

/** Colapsa o resultado em um único valor a partir de cada ramo. */
inline fun <T, R> ApiResult<T>.fold(
    onSuccess: (T) -> R,
    onError: (ApiResult.Error) -> R,
): R = when (this) {
    is ApiResult.Success -> onSuccess(data)
    is ApiResult.Error -> onError(this)
}

/** Valor em caso de sucesso ou `null` em caso de erro. */
fun <T> ApiResult<T>.getOrNull(): T? = (this as? ApiResult.Success)?.data
