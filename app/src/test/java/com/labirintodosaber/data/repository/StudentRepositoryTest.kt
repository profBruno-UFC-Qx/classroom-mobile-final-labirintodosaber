package com.labirintodosaber.data.repository

import com.labirintodosaber.data.local.dao.StudentDao
import com.labirintodosaber.data.local.entity.StudentEntity
import com.labirintodosaber.data.remote.ApiCaller
import com.labirintodosaber.data.remote.ApiErrorType
import com.labirintodosaber.data.remote.ApiResult
import com.labirintodosaber.data.remote.service.StudentApi
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

class StudentRepositoryTest {

    // Separate scope for app-level background work so it doesn't interfere with runTest
    private val appTestScope = TestScope()

    private val api: StudentApi = mockk()
    private val dao: StudentDao = mockk(relaxed = true)
    private val json = Json { ignoreUnknownKeys = true }
    private val apiCaller = ApiCaller(json)

    private lateinit var repository: StudentRepositoryImpl

    @Before
    fun setUp() {
        repository = StudentRepositoryImpl(
            api = api,
            apiCaller = apiCaller,
            json = json,
            dao = dao,
            appScope = appTestScope,
        )
    }

    @After
    fun tearDown() {
        appTestScope.cancel()
    }

    @Test
    fun `list returns cached students immediately when cache is non-empty`() = runTest {
        val entity = aStudentEntity()
        coEvery { dao.getAll() } returns listOf(entity)

        val result = repository.list()

        assertTrue(result is ApiResult.Success)
        val students = (result as ApiResult.Success).data
        assertEquals(1, students.size)
        assertEquals("Maria Silva", students.first().name)
    }

    @Test
    fun `list returns network error when cache is empty and api throws IOException`() = runTest {
        coEvery { dao.getAll() } returns emptyList()
        coEvery { api.list() } throws IOException("Sem conexão")

        val result = repository.list()

        assertTrue(result is ApiResult.Error)
        assertEquals(ApiErrorType.NETWORK, (result as ApiResult.Error).type)
    }

    private fun aStudentEntity() = StudentEntity(
        id = "s-001",
        name = "Maria Silva",
        age = 10,
        gender = "FEMALE",
        zipcode = "01310-100",
        road = "Av. Paulista",
        housenumber = "1000",
        phonenumber = "11999999999",
        learningTopics = "[]",
        createdAt = "2024-01-01T00:00:00Z",
        educatorId = "edu-001",
        photoUrl = null,
        educators = "[]",
    )
}
