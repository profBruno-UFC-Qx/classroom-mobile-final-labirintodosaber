package com.labirintodosaber.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.labirintodosaber.data.local.dao.StudentDao
import com.labirintodosaber.data.local.entity.StudentEntity

@Database(
    entities = [StudentEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun studentDao(): StudentDao
}
