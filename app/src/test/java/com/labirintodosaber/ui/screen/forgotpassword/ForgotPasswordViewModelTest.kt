package com.labirintodosaber.ui.screen.forgotpassword

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
class ForgotPasswordViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val authRepository: AuthRepository = mockk()
    private lateinit var viewModel: ForgotPasswordViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = ForgotPasswordViewModel(authRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ── maskedEmail ──────────────────────────────────────────────────────────

    @Test
    fun `maskedEmail masks local part with stars`() {
        viewModel.onAction(ForgotPasswordAction.OnEmailChange("joao@gmail.com"))
        assertEquals("****@gmail.com", viewModel.maskedEmail())
    }

    @Test
    fun `maskedEmail caps mask at 10 stars for long local parts`() {
        viewModel.onAction(ForgotPasswordAction.OnEmailChange("nomemuitorande123@empresa.com"))
        assertEquals("**********@empresa.com", viewModel.maskedEmail())
    }

    @Test
    fun `maskedEmail returns email unchanged when no at sign`() {
        viewModel.onAction(ForgotPasswordAction.OnEmailChange("invalidemail"))
        assertEquals("invalidemail", viewModel.maskedEmail())
    }

    @Test
    fun `maskedEmail returns email unchanged when at sign is at index 0`() {
        viewModel.onAction(ForgotPasswordAction.OnEmailChange("@domain.com"))
        assertEquals("@domain.com", viewModel.maskedEmail())
    }

    @Test
    fun `maskedEmail on empty email returns empty string`() {
        assertEquals("", viewModel.maskedEmail())
    }

    // ── Etapa EMAIL: sendCode ────────────────────────────────────────────────

    @Test
    fun `sendCode with blank email shows error without api call`() {
        viewModel.onAction(ForgotPasswordAction.OnSendCodeClick)

        assertEquals("Informe seu email.", viewModel.uiState.value.errorMessage)
        assertEquals(ForgotPasswordStep.EMAIL, viewModel.uiState.value.step)
    }

    @Test
    fun `sendCode success advances to CODE step and clears error`() = runTest(testDispatcher) {
        coEvery { authRepository.generateToken(any()) } returns ApiResult.Success(Unit)

        viewModel.onAction(ForgotPasswordAction.OnEmailChange("joao@email.com"))
        viewModel.onAction(ForgotPasswordAction.OnSendCodeClick)
        advanceUntilIdle()

        assertEquals(ForgotPasswordStep.CODE, viewModel.uiState.value.step)
        assertNull(viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `sendCode api error shows message and stays on EMAIL step`() = runTest(testDispatcher) {
        coEvery { authRepository.generateToken(any()) } returns
            ApiResult.Error(ApiErrorType.NOT_FOUND, "Email não encontrado.")

        viewModel.onAction(ForgotPasswordAction.OnEmailChange("naoexiste@email.com"))
        viewModel.onAction(ForgotPasswordAction.OnSendCodeClick)
        advanceUntilIdle()

        assertEquals("Email não encontrado.", viewModel.uiState.value.errorMessage)
        assertEquals(ForgotPasswordStep.EMAIL, viewModel.uiState.value.step)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `OnResendCodeClick also triggers sendCode`() = runTest(testDispatcher) {
        coEvery { authRepository.generateToken(any()) } returns ApiResult.Success(Unit)

        viewModel.onAction(ForgotPasswordAction.OnEmailChange("joao@email.com"))
        viewModel.onAction(ForgotPasswordAction.OnResendCodeClick)
        advanceUntilIdle()

        assertEquals(ForgotPasswordStep.CODE, viewModel.uiState.value.step)
    }

    // ── Etapa CODE: filtro e avanço ──────────────────────────────────────────

    @Test
    fun `OnCodeChange filters non-alphanumeric characters`() {
        viewModel.onAction(ForgotPasswordAction.OnCodeChange("abc-123!@#"))
        assertEquals("abc123", viewModel.uiState.value.code)
    }

    @Test
    fun `OnCodeChange caps code at 12 characters`() {
        viewModel.onAction(ForgotPasswordAction.OnCodeChange("ABCDEFGHIJKLMNOP"))
        assertEquals("ABCDEFGHIJKL", viewModel.uiState.value.code)
    }

    @Test
    fun `OnVerifyCodeClick advances to NEW_PASSWORD step without api call`() {
        viewModel.onAction(ForgotPasswordAction.OnVerifyCodeClick)
        assertEquals(ForgotPasswordStep.NEW_PASSWORD, viewModel.uiState.value.step)
    }

    // ── Etapa NEW_PASSWORD: resetPassword ────────────────────────────────────

    @Test
    fun `resetPassword with password shorter than 6 chars shows validation error`() {
        viewModel.onAction(ForgotPasswordAction.OnNewPasswordChange("12345"))
        viewModel.onAction(ForgotPasswordAction.OnConfirmNewPasswordChange("12345"))
        viewModel.onAction(ForgotPasswordAction.OnResetPasswordClick)

        assertEquals("A senha deve ter ao menos 6 caracteres.", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `resetPassword with mismatched confirmation shows validation error`() {
        viewModel.onAction(ForgotPasswordAction.OnNewPasswordChange("senha123"))
        viewModel.onAction(ForgotPasswordAction.OnConfirmNewPasswordChange("outracoisa"))
        viewModel.onAction(ForgotPasswordAction.OnResetPasswordClick)

        assertEquals("As senhas não conferem.", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `resetPassword success sets resetSuccess true`() = runTest(testDispatcher) {
        coEvery { authRepository.updatePassword(any(), any()) } returns ApiResult.Success(Unit)

        viewModel.onAction(ForgotPasswordAction.OnEmailChange("joao@email.com"))
        viewModel.onAction(ForgotPasswordAction.OnNewPasswordChange("novaSenha1"))
        viewModel.onAction(ForgotPasswordAction.OnConfirmNewPasswordChange("novaSenha1"))
        viewModel.onAction(ForgotPasswordAction.OnResetPasswordClick)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.resetSuccess)
        assertNull(viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `resetPassword api error propagates message`() = runTest(testDispatcher) {
        coEvery { authRepository.updatePassword(any(), any()) } returns
            ApiResult.Error(ApiErrorType.BAD_REQUEST, "Token inválido ou expirado.")

        viewModel.onAction(ForgotPasswordAction.OnEmailChange("joao@email.com"))
        viewModel.onAction(ForgotPasswordAction.OnNewPasswordChange("novaSenha1"))
        viewModel.onAction(ForgotPasswordAction.OnConfirmNewPasswordChange("novaSenha1"))
        viewModel.onAction(ForgotPasswordAction.OnResetPasswordClick)
        advanceUntilIdle()

        assertEquals("Token inválido ou expirado.", viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.resetSuccess)
    }

    // ── Toggles de visibilidade de senha ─────────────────────────────────────

    @Test
    fun `toggle new password visibility flips flag`() {
        assertFalse(viewModel.uiState.value.newPasswordVisible)

        viewModel.onAction(ForgotPasswordAction.OnToggleNewPasswordVisibility)
        assertTrue(viewModel.uiState.value.newPasswordVisible)

        viewModel.onAction(ForgotPasswordAction.OnToggleNewPasswordVisibility)
        assertFalse(viewModel.uiState.value.newPasswordVisible)
    }

    @Test
    fun `toggle confirm password visibility flips flag`() {
        assertFalse(viewModel.uiState.value.confirmNewPasswordVisible)

        viewModel.onAction(ForgotPasswordAction.OnToggleConfirmNewPasswordVisibility)
        assertTrue(viewModel.uiState.value.confirmNewPasswordVisible)
    }

    @Test
    fun `OnResetHandled clears resetSuccess flag`() = runTest(testDispatcher) {
        coEvery { authRepository.updatePassword(any(), any()) } returns ApiResult.Success(Unit)

        viewModel.onAction(ForgotPasswordAction.OnEmailChange("a@b.com"))
        viewModel.onAction(ForgotPasswordAction.OnNewPasswordChange("senha123"))
        viewModel.onAction(ForgotPasswordAction.OnConfirmNewPasswordChange("senha123"))
        viewModel.onAction(ForgotPasswordAction.OnResetPasswordClick)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.resetSuccess)

        viewModel.onAction(ForgotPasswordAction.OnResetHandled)
        assertFalse(viewModel.uiState.value.resetSuccess)
    }
}
