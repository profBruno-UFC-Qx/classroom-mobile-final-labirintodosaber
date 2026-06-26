package com.labirintodosaber.data.remote.auth

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Fonte única de verdade do JWT de autenticação.
 *
 * Persiste o token em [DataStore] (sobrevive a reinícios do app) e mantém uma cópia
 * em memória ([cached]) para leitura síncrona dentro do interceptor OkHttp, que não
 * pode suspender.
 */
@Singleton
class AuthTokenStore @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    private val cached = AtomicReference<String?>(null)

    /** Fluxo reativo do token; emite `null` quando não há sessão. */
    val tokenFlow: Flow<String?> = dataStore.data.map { it[TOKEN_KEY] }

    /**
     * Token atual para uso síncrono no interceptor. Na primeira chamada carrega o
     * valor persistido (bloqueante e único); depois serve sempre da memória.
     */
    fun currentToken(): String? = cached.get() ?: runBlocking {
        dataStore.data.first()[TOKEN_KEY].also { cached.set(it) }
    }

    suspend fun saveToken(token: String) {
        cached.set(token)
        dataStore.edit { it[TOKEN_KEY] = token }
    }

    suspend fun clearToken() {
        cached.set(null)
        dataStore.edit { it.remove(TOKEN_KEY) }
    }

    private companion object {
        val TOKEN_KEY = stringPreferencesKey("auth_jwt_token")
    }
}
