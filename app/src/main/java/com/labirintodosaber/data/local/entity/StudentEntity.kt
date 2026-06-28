package com.labirintodosaber.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.labirintodosaber.data.model.EducatorSummary
import com.labirintodosaber.data.model.Gender
import com.labirintodosaber.data.model.Student
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Entity(tableName = "students")
data class StudentEntity(
    @PrimaryKey val id: String,
    val name: String,
    val age: Int,
    val gender: String,
    val zipcode: String,
    val road: String,
    val housenumber: String,
    val phonenumber: String,
    val learningTopics: String,
    val createdAt: String,
    val educatorId: String,
    val photoUrl: String?,
    val educators: String,
)

fun Student.toEntity(json: Json) = StudentEntity(
    id = id,
    name = name,
    age = age,
    gender = gender.name,
    zipcode = zipcode,
    road = road,
    housenumber = housenumber,
    phonenumber = phonenumber,
    learningTopics = json.encodeToString(learningTopics),
    createdAt = createdAt,
    educatorId = educatorId,
    photoUrl = photoUrl,
    educators = json.encodeToString(educators),
)

fun StudentEntity.toDomain(json: Json) = Student(
    id = id,
    name = name,
    age = age,
    gender = Gender.valueOf(gender),
    zipcode = zipcode,
    road = road,
    housenumber = housenumber,
    phonenumber = phonenumber,
    learningTopics = json.decodeFromString(learningTopics),
    createdAt = createdAt,
    educatorId = educatorId,
    photoUrl = photoUrl,
    educators = json.decodeFromString<List<EducatorSummary>>(educators),
)
