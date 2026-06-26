package com.labirintodosaber.data.remote.dto

import com.labirintodosaber.data.model.Gender

/**
 * Campos de criação/atualização de aluno (enviados como `multipart/form-data`).
 * Em criação preencha todos os obrigatórios; em atualização deixe `null` o que não muda.
 */
data class StudentForm(
    val name: String? = null,
    val age: Int? = null,
    val gender: Gender? = null,
    val zipcode: String? = null,
    val road: String? = null,
    val housenumber: String? = null,
    val phonenumber: String? = null,
    val learningTopics: List<String>? = null,
)
