package com.labirintodosaber.data.repository

import com.labirintodosaber.data.model.Educator
import com.labirintodosaber.data.model.LastSession
import com.labirintodosaber.data.remote.ApiCaller
import com.labirintodosaber.data.remote.ApiResult
import com.labirintodosaber.data.remote.FileUpload
import com.labirintodosaber.data.remote.MultipartFactory
import com.labirintodosaber.data.remote.auth.AuthTokenStore
import com.labirintodosaber.data.remote.dto.GenerateTokenRequest
import com.labirintodosaber.data.remote.dto.RegisterRequest
import com.labirintodosaber.data.remote.dto.RegisterResponse
import com.labirintodosaber.data.remote.dto.SignInRequest
import com.labirintodosaber.data.remote.dto.UpdateEducatorRequest
import com.labirintodosaber.data.remote.dto.UpdatePasswordRequest
import com.labirintodosaber.data.remote.onSuccess
import com.labirintodosaber.data.remote.service.EducatorApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/** Autenticação e dados do educador. Gerencia o ciclo de vida do token JWT. */
interface AuthRepository {

    /** Emite `true` enquanto houver token persistido. */
    val isAuthenticated: Flow<Boolean>

    /** Autentica e persiste o token em caso de sucesso. */
    suspend fun signIn(email: String, password: String): ApiResult<Unit>

    suspend fun register(name: String, email: String, password: String): ApiResult<RegisterResponse>

    suspend fun updatePassword(email: String, newPassword: String): ApiResult<Unit>

    suspend fun generateToken(educatorEmail: String): ApiResult<Unit>

    suspend fun me(): ApiResult<Educator>

    suspend fun getLastSessions(): ApiResult<List<LastSession>>

    suspend fun updateProfilePicture(photo: FileUpload): ApiResult<Educator>

    suspend fun updateEducator(newName: String? = null, newContact: String? = null): ApiResult<Educator>

    /** Limpa o token local (logout). */
    suspend fun signOut()
}

class AuthRepositoryImpl @Inject constructor(
    private val api: EducatorApi,
    private val apiCaller: ApiCaller,
    private val tokenStore: AuthTokenStore,
) : AuthRepository {

    override val isAuthenticated: Flow<Boolean> =
        tokenStore.tokenFlow.map { !it.isNullOrBlank() }

    override suspend fun signIn(email: String, password: String): ApiResult<Unit> =
        apiCaller.call { api.signIn(SignInRequest(email, password)) }
            .onSuccess { tokenStore.saveToken(it.token) }
            .let { result ->
                when (result) {
                    is ApiResult.Success -> ApiResult.Success(Unit)
                    is ApiResult.Error -> result
                }
            }

    override suspend fun register(name: String, email: String, password: String) =
        apiCaller.call { api.register(RegisterRequest(name, email, password)) }

    override suspend fun updatePassword(email: String, newPassword: String) =
        apiCaller.call { api.updatePassword(UpdatePasswordRequest(email, newPassword)) }

    override suspend fun generateToken(educatorEmail: String) =
        apiCaller.call { api.generateToken(GenerateTokenRequest(educatorEmail)) }

    override suspend fun me() = apiCaller.call { api.me() }

    override suspend fun getLastSessions() = apiCaller.call { api.getLastSessions() }

    override suspend fun updateProfilePicture(photo: FileUpload) =
        apiCaller.call { api.updateProfilePicture(MultipartFactory.filePart("photo", photo)) }

    override suspend fun updateEducator(newName: String?, newContact: String?) =
        apiCaller.call { api.updateEducator(UpdateEducatorRequest(newName, newContact)) }

    override suspend fun signOut() = tokenStore.clearToken()
}
