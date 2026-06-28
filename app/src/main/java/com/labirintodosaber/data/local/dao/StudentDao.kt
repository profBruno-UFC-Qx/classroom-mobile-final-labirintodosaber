package com.labirintodosaber.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.labirintodosaber.data.local.entity.StudentEntity

@Dao
interface StudentDao {

    @Query("SELECT * FROM students ORDER BY name ASC")
    suspend fun getAll(): List<StudentEntity>

    @Query("SELECT * FROM students WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): StudentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(student: StudentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(students: List<StudentEntity>)

    @Query("DELETE FROM students")
    suspend fun deleteAll()

    suspend fun replaceAll(students: List<StudentEntity>) {
        deleteAll()
        upsertAll(students)
    }
}
