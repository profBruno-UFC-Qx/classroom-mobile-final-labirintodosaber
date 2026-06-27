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
    // A API devolve `educators` como array de objetos de educador (não de ids).
    val educators: List<EducatorSummary> = emptyList(),
)

/** Educador vinculado, conforme aninhado na resposta de [Student]. */
@Serializable
data class EducatorSummary(
    val id: String? = null,
    val name: String? = null,
    val email: String? = null,
    val photoUrl: String? = null,
    val contact: String? = null,
)
