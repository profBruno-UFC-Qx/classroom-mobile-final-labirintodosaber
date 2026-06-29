package com.labirintodosaber.ui.screen.login

import com.labirintodosaber.data.remote.ApiErrorType
import com.labirintodosaber.data.remote.ApiResult
import com.labirintodosaber.data.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val authRepository: AuthRepository = mockk()
    private lateinit var viewModel: LoginViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = LoginViewModel(authRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `clicking login with blank email shows validation error without calling api`() {
        viewModel.onAction(LoginAction.OnEmailChange(""))
        viewModel.onAction(LoginAction.OnPasswordChange("senha123"))
        viewModel.onAction(LoginAction.OnLoginClick)

        assertEquals("Preencha email e senha.", viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.loginSuccess)
    }

    @Test
    fun `clicking login with blank password shows validation error`() {
        viewModel.onAction(LoginAction.OnEmailChange("user@email.com"))
        viewModel.onAction(LoginAction.OnPasswordChange(""))
        viewModel.onAction(LoginAction.OnLoginClick)

        assertEquals("Preencha email e senha.", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `successful login sets loginSuccess to true`() = runTest(testDispatcher) {
        coEvery { authRepository.signIn(any(), any()) } returns ApiResult.Success(Unit)

        viewModel.onAction(LoginAction.OnEmailChange("user@email.com"))
        viewModel.onAction(LoginAction.OnPasswordChange("senha123"))
        viewModel.onAction(LoginAction.OnLoginClick)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.loginSuccess)
        assertNull(viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `api error propagates message to uiState`() = runTest(testDispatcher) {
        coEvery { authRepository.signIn(any(), any()) } returns
            ApiResult.Error(type = ApiErrorType.UNAUTHORIZED, message = "Credenciais inválidas.")

        viewModel.onAction(LoginAction.OnEmailChange("user@email.com"))
        viewModel.onAction(LoginAction.OnPasswordChange("senhaerrada"))
        viewModel.onAction(LoginAction.OnLoginClick)
        advanceUntilIdle()

        assertEquals("Credenciais inválidas.", viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.loginSuccess)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `toggle remember me flips the flag`() {
        assertFalse(viewModel.uiState.value.rememberMe)

        viewModel.onAction(LoginAction.OnToggleRememberMe)
        assertTrue(viewModel.uiState.value.rememberMe)

        viewModel.onAction(LoginAction.OnToggleRememberMe)
        assertFalse(viewModel.uiState.value.rememberMe)
    }

    @Test
    fun `toggle password visibility flips the flag`() {
        assertFalse(viewModel.uiState.value.passwordVisible)

        viewModel.onAction(LoginAction.OnTogglePasswordVisibility)
        assertTrue(viewModel.uiState.value.passwordVisible)
    }

    @Test
    fun `typing email clears any existing error message`() {
        viewModel.onAction(LoginAction.OnEmailChange(""))
        viewModel.onAction(LoginAction.OnPasswordChange("x"))
        viewModel.onAction(LoginAction.OnLoginClick)
        assertEquals("Preencha email e senha.", viewModel.uiState.value.errorMessage)

        viewModel.onAction(LoginAction.OnEmailChange("novo@email.com"))
        assertNull(viewModel.uiState.value.errorMessage)
    }
}
