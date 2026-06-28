package com.labirintodosaber.data.remote.service

import com.labirintodosaber.data.model.Appointment
import com.labirintodosaber.data.remote.dto.AppointmentCreateRequest
import com.labirintodosaber.data.remote.dto.AppointmentUpdateRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/**
 * Endpoints de `/appointment` voltados ao app (todos exigem Bearer).
 *
 * Os endpoints internos `notify` (chave de job) e `watchdog` (público) não são
 * expostos aqui por serem infraestrutura agendada, não fluxos do educador.
 */
interface AppointmentApi {

    // create/update não desserializam o corpo: a resposta da API não segue o mesmo
    // formato de Appointment (vem envelopada), então só interessa o sucesso da chamada.
    @POST("appointment/")
    suspend fun create(@Body request: AppointmentCreateRequest)

    @GET("appointment/")
    suspend fun list(): List<Appointment>

    @GET("appointment/{id}")
    suspend fun getById(@Path("id") id: String): Appointment

    @PUT("appointment/{id}")
    suspend fun update(
        @Path("id") id: String,
        @Body request: AppointmentUpdateRequest,
    )

    @DELETE("appointment/{id}")
    suspend fun delete(@Path("id") id: String)
}
