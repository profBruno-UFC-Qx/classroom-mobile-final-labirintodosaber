package com.labirintodosaber.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.labirintodosaber.data.remote.SerialNameConverterFactory
import com.labirintodosaber.data.remote.interceptor.AuthInterceptor
import com.labirintodosaber.data.remote.service.AnamneseResponseApi
import com.labirintodosaber.data.remote.service.AnamneseTemplateApi
import com.labirintodosaber.data.remote.service.AppointmentApi
import com.labirintodosaber.data.remote.service.EducatorApi
import com.labirintodosaber.data.remote.service.StudentApi
import com.labirintodosaber.data.remote.service.TaskApi
import com.labirintodosaber.data.remote.service.TaskGroupApi
import com.labirintodosaber.data.remote.service.TaskNotebookApi
import com.labirintodosaber.data.remote.service.TaskNotebookSessionApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.create
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Provê o client HTTP reutilizável: [Json], [OkHttpClient], [Retrofit] e as
 * interfaces de serviço. Tudo singleton — uma única instância compartilhada.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /** URL base da API (produção, Vercel). Retrofit exige a barra final. */
    private const val BASE_URL = "https://labirinto-do-saber.vercel.app/"

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true // tolera campos novos/não mapeados da API
        explicitNulls = false    // omite campos nulos no corpo das requisições
        coerceInputValues = true // usa o default quando o valor recebido é null
    }

    @Provides
    @Singleton
    fun provideDataStore(
        @ApplicationContext context: Context,
    ): DataStore<Preferences> = PreferenceDataStoreFactory.create {
        context.preferencesDataStoreFile("auth_prefs")
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient, json: Json): Retrofit {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(SerialNameConverterFactory(json)) // enums em @Query/@Path
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    @Provides
    @Singleton
    fun provideEducatorApi(retrofit: Retrofit): EducatorApi = retrofit.create()

    @Provides
    @Singleton
    fun provideStudentApi(retrofit: Retrofit): StudentApi = retrofit.create()

    @Provides
    @Singleton
    fun provideTaskApi(retrofit: Retrofit): TaskApi = retrofit.create()

    @Provides
    @Singleton
    fun provideTaskGroupApi(retrofit: Retrofit): TaskGroupApi = retrofit.create()

    @Provides
    @Singleton
    fun provideTaskNotebookApi(retrofit: Retrofit): TaskNotebookApi = retrofit.create()

    @Provides
    @Singleton
    fun provideTaskNotebookSessionApi(retrofit: Retrofit): TaskNotebookSessionApi = retrofit.create()

    @Provides
    @Singleton
    fun provideAnamneseTemplateApi(retrofit: Retrofit): AnamneseTemplateApi = retrofit.create()

    @Provides
    @Singleton
    fun provideAnamneseResponseApi(retrofit: Retrofit): AnamneseResponseApi = retrofit.create()

    @Provides
    @Singleton
    fun provideAppointmentApi(retrofit: Retrofit): AppointmentApi = retrofit.create()
}
