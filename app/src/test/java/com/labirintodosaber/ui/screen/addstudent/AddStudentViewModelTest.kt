package com.labirintodosaber.ui.screen.addstudent

import com.labirintodosaber.data.model.Gender
import com.labirintodosaber.data.model.Student
import com.labirintodosaber.data.remote.ApiErrorType
import com.labirintodosaber.data.remote.ApiResult
import com.labirintodosaber.data.repository.StudentRepository
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
class AddStudentViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val studentRepository: StudentRepository = mockk()
    private lateinit var viewModel: AddStudentViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = AddStudentViewModel(studentRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun fillValidForm() {
        viewModel.onAction(AddStudentAction.OnFullNameChange("João Silva"))
        viewModel.onAction(AddStudentAction.OnAgeChange("10"))
        viewModel.onAction(AddStudentAction.OnGenderChange("Masculino"))
        viewModel.onAction(AddStudentAction.OnPhoneChange("1234567"))
        viewModel.onAction(AddStudentAction.OnCepChange("12345"))
        viewModel.onAction(AddStudentAction.OnStreetChange("Rua das Flores"))
        viewModel.onAction(AddStudentAction.OnNumberChange("42"))
        viewModel.onAction(AddStudentAction.OnLearningTopicsChange("Leitura, Escrita"))
    }

    // ── Validações ───────────────────────────────────────────────────────────

    @Test
    fun `blank full name shows validation error`() {
        viewModel.onAction(AddStudentAction.OnAgeChange("10"))
        viewModel.onAction(AddStudentAction.OnGenderChange("Masculino"))
        viewModel.onAction(AddStudentAction.OnPhoneChange("1234567"))
        viewModel.onAction(AddStudentAction.OnCepChange("12345"))
        viewModel.onAction(AddStudentAction.OnStreetChange("Rua A"))
        viewModel.onAction(AddStudentAction.OnNumberChange("1"))
        viewModel.onAction(AddStudentAction.OnLearningTopicsChange("Leitura"))
        viewModel.onAction(AddStudentAction.OnSaveClick)

        assertEquals("Informe o nome do aluno.", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `empty age field shows validation error`() {
        viewModel.onAction(AddStudentAction.OnFullNameChange("Maria"))
        // age remains ""
        viewModel.onAction(AddStudentAction.OnGenderChange("Feminino"))
        viewModel.onAction(AddStudentAction.OnPhoneChange("1234567"))
        viewModel.onAction(AddStudentAction.OnCepChange("12345"))
        viewModel.onAction(AddStudentAction.OnStreetChange("Rua B"))
        viewModel.onAction(AddStudentAction.OnNumberChange("2"))
        viewModel.onAction(AddStudentAction.OnLearningTopicsChange("Escrita"))
        viewModel.onAction(AddStudentAction.OnSaveClick)

        assertEquals("A idade deve estar entre 1 e 50.", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `age zero shows validation error`() {
        viewModel.onAction(AddStudentAction.OnFullNameChange("Maria"))
        viewModel.onAction(AddStudentAction.OnAgeChange("0"))
        viewModel.onAction(AddStudentAction.OnGenderChange("Feminino"))
        viewModel.onAction(AddStudentAction.OnPhoneChange("1234567"))
        viewModel.onAction(AddStudentAction.OnCepChange("12345"))
        viewModel.onAction(AddStudentAction.OnStreetChange("Rua B"))
        viewModel.onAction(AddStudentAction.OnNumberChange("2"))
        viewModel.onAction(AddStudentAction.OnLearningTopicsChange("Escrita"))
        viewModel.onAction(AddStudentAction.OnSaveClick)

        assertEquals("A idade deve estar entre 1 e 50.", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `age 51 shows validation error`() {
        viewModel.onAction(AddStudentAction.OnFullNameChange("Maria"))
        viewModel.onAction(AddStudentAction.OnAgeChange("51"))
        viewModel.onAction(AddStudentAction.OnGenderChange("Feminino"))
        viewModel.onAction(AddStudentAction.OnPhoneChange("1234567"))
        viewModel.onAction(AddStudentAction.OnCepChange("12345"))
        viewModel.onAction(AddStudentAction.OnStreetChange("Rua B"))
        viewModel.onAction(AddStudentAction.OnNumberChange("2"))
        viewModel.onAction(AddStudentAction.OnLearningTopicsChange("Escrita"))
        viewModel.onAction(AddStudentAction.OnSaveClick)

        assertEquals("A idade deve estar entre 1 e 50.", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `invalid gender shows validation error`() {
        viewModel.onAction(AddStudentAction.OnFullNameChange("Pedro"))
        viewModel.onAction(AddStudentAction.OnAgeChange("15"))
        viewModel.onAction(AddStudentAction.OnGenderChange("Outro")) // não mapeado
        viewModel.onAction(AddStudentAction.OnPhoneChange("1234567"))
        viewModel.onAction(AddStudentAction.OnCepChange("12345"))
        viewModel.onAction(AddStudentAction.OnStreetChange("Rua C"))
        viewModel.onAction(AddStudentAction.OnNumberChange("3"))
        viewModel.onAction(AddStudentAction.OnLearningTopicsChange("Vocabulário"))
        viewModel.onAction(AddStudentAction.OnSaveClick)

        assertEquals("Selecione o gênero (Masculino ou Feminino).", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `phone with 6 digits shows validation error`() {
        viewModel.onAction(AddStudentAction.OnFullNameChange("Ana"))
        viewModel.onAction(AddStudentAction.OnAgeChange("12"))
        viewModel.onAction(AddStudentAction.OnGenderChange("Feminino"))
        viewModel.onAction(AddStudentAction.OnPhoneChange("123456")) // 6 dígitos
        viewModel.onAction(AddStudentAction.OnCepChange("12345"))
        viewModel.onAction(AddStudentAction.OnStreetChange("Rua D"))
        viewModel.onAction(AddStudentAction.OnNumberChange("4"))
        viewModel.onAction(AddStudentAction.OnLearningTopicsChange("Leitura"))
        viewModel.onAction(AddStudentAction.OnSaveClick)

        assertEquals("Informe um telefone válido (mín. 7 dígitos).", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `cep with 4 digits shows validation error`() {
        viewModel.onAction(AddStudentAction.OnFullNameChange("Carlos"))
        viewModel.onAction(AddStudentAction.OnAgeChange("20"))
        viewModel.onAction(AddStudentAction.OnGenderChange("Masculino"))
        viewModel.onAction(AddStudentAction.OnPhoneChange("1234567"))
        viewModel.onAction(AddStudentAction.OnCepChange("1234")) // 4 dígitos
        viewModel.onAction(AddStudentAction.OnStreetChange("Rua E"))
        viewModel.onAction(AddStudentAction.OnNumberChange("5"))
        viewModel.onAction(AddStudentAction.OnLearningTopicsChange("Compreensão"))
        viewModel.onAction(AddStudentAction.OnSaveClick)

        assertEquals("Informe um CEP válido (mín. 5 dígitos).", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `blank street shows validation error`() {
        viewModel.onAction(AddStudentAction.OnFullNameChange("Lucas"))
        viewModel.onAction(AddStudentAction.OnAgeChange("8"))
        viewModel.onAction(AddStudentAction.OnGenderChange("Masculino"))
        viewModel.onAction(AddStudentAction.OnPhoneChange("1234567"))
        viewModel.onAction(AddStudentAction.OnCepChange("12345"))
        // street stays blank
        viewModel.onAction(AddStudentAction.OnNumberChange("6"))
        viewModel.onAction(AddStudentAction.OnLearningTopicsChange("Escrita"))
        viewModel.onAction(AddStudentAction.OnSaveClick)

        assertEquals("Informe a rua.", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `blank number shows validation error`() {
        viewModel.onAction(AddStudentAction.OnFullNameChange("Bia"))
        viewModel.onAction(AddStudentAction.OnAgeChange("9"))
        viewModel.onAction(AddStudentAction.OnGenderChange("Feminino"))
        viewModel.onAction(AddStudentAction.OnPhoneChange("1234567"))
        viewModel.onAction(AddStudentAction.OnCepChange("12345"))
        viewModel.onAction(AddStudentAction.OnStreetChange("Rua F"))
        // number stays blank
        viewModel.onAction(AddStudentAction.OnLearningTopicsChange("Leitura"))
        viewModel.onAction(AddStudentAction.OnSaveClick)

        assertEquals("Informe o número.", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `blank learning topics shows validation error`() {
        viewModel.onAction(AddStudentAction.OnFullNameChange("Tiago"))
        viewModel.onAction(AddStudentAction.OnAgeChange("11"))
        viewModel.onAction(AddStudentAction.OnGenderChange("Masculino"))
        viewModel.onAction(AddStudentAction.OnPhoneChange("1234567"))
        viewModel.onAction(AddStudentAction.OnCepChange("12345"))
        viewModel.onAction(AddStudentAction.OnStreetChange("Rua G"))
        viewModel.onAction(AddStudentAction.OnNumberChange("7"))
        // topics stays blank
        viewModel.onAction(AddStudentAction.OnSaveClick)

        assertEquals("Informe ao menos um tema de aprendizagem.", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `topic with only commas and spaces shows topics-empty error`() {
        viewModel.onAction(AddStudentAction.OnFullNameChange("Tiago"))
        viewModel.onAction(AddStudentAction.OnAgeChange("11"))
        viewModel.onAction(AddStudentAction.OnGenderChange("Masculino"))
        viewModel.onAction(AddStudentAction.OnPhoneChange("1234567"))
        viewModel.onAction(AddStudentAction.OnCepChange("12345"))
        viewModel.onAction(AddStudentAction.OnStreetChange("Rua G"))
        viewModel.onAction(AddStudentAction.OnNumberChange("7"))
        viewModel.onAction(AddStudentAction.OnLearningTopicsChange("  ,  , "))
        viewModel.onAction(AddStudentAction.OnSaveClick)

        assertEquals("Informe ao menos um tema de aprendizagem.", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `topic longer than 50 chars shows validation error`() {
        viewModel.onAction(AddStudentAction.OnFullNameChange("Sofia"))
        viewModel.onAction(AddStudentAction.OnAgeChange("7"))
        viewModel.onAction(AddStudentAction.OnGenderChange("Feminino"))
        viewModel.onAction(AddStudentAction.OnPhoneChange("1234567"))
        viewModel.onAction(AddStudentAction.OnCepChange("12345"))
        viewModel.onAction(AddStudentAction.OnStreetChange("Rua H"))
        viewModel.onAction(AddStudentAction.OnNumberChange("8"))
        viewModel.onAction(AddStudentAction.OnLearningTopicsChange("a".repeat(51)))
        viewModel.onAction(AddStudentAction.OnSaveClick)

        assertEquals("Cada tema deve ter no máximo 50 caracteres.", viewModel.uiState.value.errorMessage)
    }

    // ── Fluxo de sucesso e erro de API ───────────────────────────────────────

    private fun aStudent() = Student(
        id = "s1", name = "João Silva", age = 10,
        gender = Gender.MALE, zipcode = "12345", road = "Rua A",
        housenumber = "1", phonenumber = "1234567",
        createdAt = "2024-01-01T00:00:00Z", educatorId = "e1",
    )

    @Test
    fun `successful save sets saveSuccess true and clears loading`() = runTest(testDispatcher) {
        coEvery { studentRepository.create(any(), any()) } returns ApiResult.Success(aStudent())

        fillValidForm()
        viewModel.onAction(AddStudentAction.OnSaveClick)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.saveSuccess)
        assertNull(viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `api error propagates message and clears loading`() = runTest(testDispatcher) {
        coEvery { studentRepository.create(any(), any()) } returns
            ApiResult.Error(type = ApiErrorType.CONFLICT, message = "Email já cadastrado.")

        fillValidForm()
        viewModel.onAction(AddStudentAction.OnSaveClick)
        advanceUntilIdle()

        assertEquals("Email já cadastrado.", viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.saveSuccess)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    // ── Filtros de input ─────────────────────────────────────────────────────

    @Test
    fun `OnAgeChange strips non-digit characters`() {
        viewModel.onAction(AddStudentAction.OnAgeChange("1a2b"))
        assertEquals("12", viewModel.uiState.value.age)
    }

    @Test
    fun `OnPhoneChange strips non-digit characters`() {
        viewModel.onAction(AddStudentAction.OnPhoneChange("(11) 9 8765-4321"))
        assertEquals("11987654321", viewModel.uiState.value.phone)
    }

    @Test
    fun `OnCepChange strips non-digit characters and caps at 8`() {
        viewModel.onAction(AddStudentAction.OnCepChange("01310-100"))
        assertEquals("01310100", viewModel.uiState.value.cep)
    }

    // ── Comportamentos auxiliares ────────────────────────────────────────────

    @Test
    fun `typing after validation error clears errorMessage`() {
        viewModel.onAction(AddStudentAction.OnSaveClick)
        assertEquals("Informe o nome do aluno.", viewModel.uiState.value.errorMessage)

        viewModel.onAction(AddStudentAction.OnFullNameChange("Novo Nome"))
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `OnToggleAdditionalInfo flips showAdditionalInfo flag`() {
        assertFalse(viewModel.uiState.value.showAdditionalInfo)

        viewModel.onAction(AddStudentAction.OnToggleAdditionalInfo)
        assertTrue(viewModel.uiState.value.showAdditionalInfo)

        viewModel.onAction(AddStudentAction.OnToggleAdditionalInfo)
        assertFalse(viewModel.uiState.value.showAdditionalInfo)
    }
}
