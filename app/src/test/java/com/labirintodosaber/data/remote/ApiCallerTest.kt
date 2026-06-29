package com.labirintodosaber.data.remote

import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class ApiCallerTest {

    private val json = Json { ignoreUnknownKeys = true }
    private val apiCaller = ApiCaller(json)

    private fun httpException(code: Int, bodyJson: String = ""): HttpException {
        val body = bodyJson.toResponseBody("application/json; charset=utf-8".toMediaType())
        return HttpException(Response.error<Unit>(code, body))
    }

    // ── Sucesso ──────────────────────────────────────────────────────────────

    @Test
    fun `successful call returns Success with the block result`() = runTest {
        val result = apiCaller.call { "payload" }
        assertTrue(result is ApiResult.Success)
        assertEquals("payload", (result as ApiResult.Success).data)
    }

    // ── IOException → NETWORK ────────────────────────────────────────────────

    @Test
    fun `IOException maps to NETWORK error`() = runTest {
        val result = apiCaller.call { throw IOException("timeout") }

        assertTrue(result is ApiResult.Error)
        assertEquals(ApiErrorType.NETWORK, (result as ApiResult.Error).type)
    }

    // ── Classificação por código HTTP ────────────────────────────────────────

    @Test
    fun `HTTP 400 maps to BAD_REQUEST`() = runTest {
        val result = apiCaller.call { throw httpException(400) }
        assertEquals(ApiErrorType.BAD_REQUEST, (result as ApiResult.Error).type)
        assertEquals(400, result.httpCode)
    }

    @Test
    fun `HTTP 401 maps to UNAUTHORIZED`() = runTest {
        val result = apiCaller.call { throw httpException(401) }
        assertEquals(ApiErrorType.UNAUTHORIZED, (result as ApiResult.Error).type)
    }

    @Test
    fun `HTTP 403 maps to FORBIDDEN`() = runTest {
        val result = apiCaller.call { throw httpException(403) }
        assertEquals(ApiErrorType.FORBIDDEN, (result as ApiResult.Error).type)
    }

    @Test
    fun `HTTP 404 maps to NOT_FOUND`() = runTest {
        val result = apiCaller.call { throw httpException(404) }
        assertEquals(ApiErrorType.NOT_FOUND, (result as ApiResult.Error).type)
    }

    @Test
    fun `HTTP 409 maps to CONFLICT`() = runTest {
        val result = apiCaller.call { throw httpException(409) }
        assertEquals(ApiErrorType.CONFLICT, (result as ApiResult.Error).type)
    }

    @Test
    fun `HTTP 422 maps to CONFLICT`() = runTest {
        val result = apiCaller.call { throw httpException(422) }
        assertEquals(ApiErrorType.CONFLICT, (result as ApiResult.Error).type)
    }

    @Test
    fun `HTTP 429 maps to CONFLICT`() = runTest {
        val result = apiCaller.call { throw httpException(429) }
        assertEquals(ApiErrorType.CONFLICT, (result as ApiResult.Error).type)
    }

    @Test
    fun `HTTP 500 maps to SERVER`() = runTest {
        val result = apiCaller.call { throw httpException(500) }
        assertEquals(ApiErrorType.SERVER, (result as ApiResult.Error).type)
    }

    @Test
    fun `HTTP 599 maps to SERVER`() = runTest {
        val result = apiCaller.call { throw httpException(599) }
        assertEquals(ApiErrorType.SERVER, (result as ApiResult.Error).type)
    }

    @Test
    fun `unrecognized HTTP code maps to UNKNOWN`() = runTest {
        val result = apiCaller.call { throw httpException(418) }
        assertEquals(ApiErrorType.UNKNOWN, (result as ApiResult.Error).type)
    }

    // ── Mensagem de erro: fallback para mensagem padrão ─────────────────────

    @Test
    fun `empty body uses default message for the error type`() = runTest {
        val result = apiCaller.call { throw httpException(401) } as ApiResult.Error
        assertEquals("Sessão expirada. Faça login novamente.", result.message)
    }

    @Test
    fun `empty body 404 uses default not-found message`() = runTest {
        val result = apiCaller.call { throw httpException(404) } as ApiResult.Error
        assertEquals("Recurso não encontrado.", result.message)
    }

    @Test
    fun `empty body 500 uses default server message`() = runTest {
        val result = apiCaller.call { throw httpException(500) } as ApiResult.Error
        assertEquals("Erro no servidor. Tente novamente mais tarde.", result.message)
    }

    // ── Mensagem de erro: corpo JSON com "message" ───────────────────────────

    @Test
    fun `body with message field uses that message`() = runTest {
        val body = """{"message":"Email já cadastrado."}"""
        val result = apiCaller.call { throw httpException(409, body) } as ApiResult.Error
        assertEquals("Email já cadastrado.", result.message)
    }

    @Test
    fun `body with error field is used as fallback when message is absent`() = runTest {
        val body = """{"error":"Internal server error detail."}"""
        val result = apiCaller.call { throw httpException(500, body) } as ApiResult.Error
        assertEquals("Internal server error detail.", result.message)
    }

    // ── Mensagem de erro: corpo JSON com erros de validação ──────────────────

    @Test
    fun `validation errors are joined into a single message`() = runTest {
        val body = """{"message":"Validation error","errors":[
            {"field":"email","message":"já está em uso"},
            {"field":"name","message":"é obrigatório"}
        ]}"""
        val result = apiCaller.call { throw httpException(400, body) } as ApiResult.Error
        assertEquals("email: já está em uso\nname: é obrigatório", result.message)
        assertEquals(2, result.validationErrors.size)
    }

    @Test
    fun `validation error without field name uses message only`() = runTest {
        val body = """{"errors":[{"message":"valor inválido"}]}"""
        val result = apiCaller.call { throw httpException(400, body) } as ApiResult.Error
        assertEquals("valor inválido", result.message)
    }

    // ── Corpo inválido (não-JSON) ────────────────────────────────────────────

    @Test
    fun `non-JSON body is used as raw message`() = runTest {
        val body = "Bad Request"
        val result = apiCaller.call { throw httpException(400, body) } as ApiResult.Error
        assertEquals("Bad Request", result.message)
    }

    // ── Exception genérica ───────────────────────────────────────────────────

    @Test
    fun `generic exception maps to UNKNOWN error`() = runTest {
        val result = apiCaller.call { throw RuntimeException("inesperado") }
        assertTrue(result is ApiResult.Error)
        assertEquals(ApiErrorType.UNKNOWN, (result as ApiResult.Error).type)
        assertEquals("inesperado", result.message)
    }

    @Test
    fun `generic exception with no message uses fallback text`() = runTest {
        val result = apiCaller.call { throw RuntimeException() }
        assertEquals("Ocorreu um erro inesperado.", (result as ApiResult.Error).message)
    }
}
