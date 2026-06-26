package com.labirintodosaber.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Student(
    val id: String,
    val name: String,
    val age: Int,
    val gender: Gender,
    val zipcode: String,
    val road: String,
    val housenumber: String,
    val phonenumber: String,
    val learningTopics: List<String> = emptyList(),
    val createdAt: String,
    val educatorId: String,
    val photoUrl: String? = null,
    val educators: List<String> = emptyList(),
)
