package com.labirintodosaber.data.remote.service

import com.labirintodosaber.data.model.Educator
import com.labirintodosaber.data.model.LastSession
import com.labirintodosaber.data.remote.dto.AuthTokenResponse
import com.labirintodosaber.data.remote.dto.GenerateTokenRequest
import com.labirintodosaber.data.remote.dto.RegisterRequest
import com.labirintodosaber.data.remote.dto.RegisterResponse
import com.labirintodosaber.data.remote.dto.SignInRequest
import com.labirintodosaber.data.remote.dto.UpdateEducatorRequest
import com.labirintodosaber.data.remote.dto.UpdatePasswordRequest
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part

/** Endpoints de `/educator` (proteção por rota). */
interface EducatorApi {

    @Headers("X-No-Auth: true")
    @POST("educator/sign-in")
    suspend fun signIn(@Body request: SignInRequest): AuthTokenResponse

    @Headers("X-No-Auth: true")
    @POST("educator/register")
    suspend fun register(@Body request: RegisterRequest): RegisterResponse

    @Headers("X-No-Auth: true")
    @POST("educator/update-password")
    suspend fun updatePassword(@Body request: UpdatePasswordRequest)

    @Headers("X-No-Auth: true")
    @PUT("educator/generate-token")
    suspend fun generateToken(@Body request: GenerateTokenRequest)

    @GET("educator/me")
    suspend fun me(): Educator

    @GET("educator/get-last-sessions")
    suspend fun getLastSessions(): List<LastSession>

    @Multipart
    @PUT("educator/update-profile-picture")
    suspend fun updateProfilePicture(@Part photo: MultipartBody.Part): Educator

    @PUT("educator/update-educator")
    suspend fun updateEducator(@Body request: UpdateEducatorRequest): Educator
}
