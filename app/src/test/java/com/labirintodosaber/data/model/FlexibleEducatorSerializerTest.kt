package com.labirintodosaber.data.model

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class FlexibleEducatorSerializerTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `educator as UUID string creates Educator with blank name and email`() {
        val raw = """{"id":"nb-001","educator":"9682a07c-3aa5-47b2-afc9-f7ec91234567","category":"reading","description":"Test","createdAt":"2024-01-01T00:00:00Z"}"""

        val notebook = json.decodeFromString<TaskNotebook>(raw)

        assertEquals("9682a07c-3aa5-47b2-afc9-f7ec91234567", notebook.educator.id)
        assertEquals("", notebook.educator.name)
        assertEquals("", notebook.educator.email)
    }

    @Test
    fun `educator as full object is deserialized correctly`() {
        val raw = """{"id":"nb-001","educator":{"id":"edu-001","name":"Ana","email":"ana@edu.br"},"category":"reading","description":"Test","createdAt":"2024-01-01T00:00:00Z"}"""

        val notebook = json.decodeFromString<TaskNotebook>(raw)

        assertEquals("edu-001", notebook.educator.id)
        assertEquals("Ana", notebook.educator.name)
        assertEquals("ana@edu.br", notebook.educator.email)
    }

    @Test
    fun `educator as UUID preserves optional photoUrl as null`() {
        val raw = """{"id":"nb-001","educator":"some-uuid","category":"writing","description":"D","createdAt":"2024-01-01T00:00:00Z"}"""

        val notebook = json.decodeFromString<TaskNotebook>(raw)

        assertEquals(null, notebook.educator.photoUrl)
    }
}
