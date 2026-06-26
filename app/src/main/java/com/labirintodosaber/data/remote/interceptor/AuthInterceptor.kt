package com.labirintodosaber.data.remote.interceptor

import com.labirintodosaber.data.remote.auth.AuthTokenStore
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Injeta `Authorization: Bearer <token>` em toda requisição quando há sessão.
 *
 * Requisições marcadas com o header [NO_AUTH_HEADER] (rotas públicas, ex.: sign-in)
 * passam sem o token. Esse header de controle é removido antes do envio.
 */
@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenStore: AuthTokenStore,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()

        if (original.header(NO_AUTH_HEADER) != null) {
            val cleaned = original.newBuilder().removeHeader(NO_AUTH_HEADER).build()
            return chain.proceed(cleaned)
        }

        val token = tokenStore.currentToken()
        val request = if (token.isNullOrBlank()) {
            original
        } else {
            original.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        }
        return chain.proceed(request)
    }

    companion object {
        /** Header de controle: presente => não anexar Bearer. Removido antes do envio. */
        const val NO_AUTH_HEADER = "X-No-Auth"
    }
}
