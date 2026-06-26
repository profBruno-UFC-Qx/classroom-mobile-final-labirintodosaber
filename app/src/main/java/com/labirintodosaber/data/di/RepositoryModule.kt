package com.labirintodosaber.data.di

import com.labirintodosaber.data.repository.AnamneseRepository
import com.labirintodosaber.data.repository.AnamneseRepositoryImpl
import com.labirintodosaber.data.repository.AppointmentRepository
import com.labirintodosaber.data.repository.AppointmentRepositoryImpl
import com.labirintodosaber.data.repository.AuthRepository
import com.labirintodosaber.data.repository.AuthRepositoryImpl
import com.labirintodosaber.data.repository.SessionRepository
import com.labirintodosaber.data.repository.SessionRepositoryImpl
import com.labirintodosaber.data.repository.StudentRepository
import com.labirintodosaber.data.repository.StudentRepositoryImpl
import com.labirintodosaber.data.repository.TaskGroupRepository
import com.labirintodosaber.data.repository.TaskGroupRepositoryImpl
import com.labirintodosaber.data.repository.TaskNotebookRepository
import com.labirintodosaber.data.repository.TaskNotebookRepositoryImpl
import com.labirintodosaber.data.repository.TaskRepository
import com.labirintodosaber.data.repository.TaskRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Vincula cada interface de repositório à sua implementação. */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindStudentRepository(impl: StudentRepositoryImpl): StudentRepository

    @Binds
    @Singleton
    abstract fun bindTaskRepository(impl: TaskRepositoryImpl): TaskRepository

    @Binds
    @Singleton
    abstract fun bindTaskGroupRepository(impl: TaskGroupRepositoryImpl): TaskGroupRepository

    @Binds
    @Singleton
    abstract fun bindTaskNotebookRepository(impl: TaskNotebookRepositoryImpl): TaskNotebookRepository

    @Binds
    @Singleton
    abstract fun bindSessionRepository(impl: SessionRepositoryImpl): SessionRepository

    @Binds
    @Singleton
    abstract fun bindAnamneseRepository(impl: AnamneseRepositoryImpl): AnamneseRepository

    @Binds
    @Singleton
    abstract fun bindAppointmentRepository(impl: AppointmentRepositoryImpl): AppointmentRepository
}
