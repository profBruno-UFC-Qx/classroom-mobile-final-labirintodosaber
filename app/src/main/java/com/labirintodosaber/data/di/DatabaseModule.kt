package com.labirintodosaber.data.di

import android.content.Context
import androidx.room.Room
import com.labirintodosaber.data.local.AppDatabase
import com.labirintodosaber.data.local.dao.StudentDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "labirinto_db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideStudentDao(db: AppDatabase): StudentDao = db.studentDao()
}
