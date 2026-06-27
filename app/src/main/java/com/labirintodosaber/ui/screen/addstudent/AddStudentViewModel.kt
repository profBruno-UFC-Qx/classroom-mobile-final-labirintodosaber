package com.labirintodosaber.ui.screen.addstudent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.labirintodosaber.data.model.Gender
import com.labirintodosaber.data.remote.ApiResult
import com.labirintodosaber.data.remote.dto.StudentForm
import com.labirintodosaber.data.repository.StudentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddStudentViewModel @Inject constructor(
    private val studentRepository: StudentRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddStudentUiState())
    val uiState: StateFlow<AddStudentUiState> = _uiState.asStateFlow()

    fun onAction(action: AddStudentAction) {
        when (action) {
            is AddStudentAction.OnFullNameChange -> _uiState.update { it.copy(fullName = action.value.take(100), errorMessage = null) }
            is AddStudentAction.OnAgeChange -> _uiState.update { it.copy(age = action.value.filter { c -> c.isDigit() }.take(2), errorMessage = null) }
            is AddStudentAction.OnGenderChange -> _uiState.update { it.copy(gender = action.value, errorMessage = null) }
            is AddStudentAction.OnPhoneChange -> _uiState.update { it.copy(phone = action.value.filter { c -> c.isDigit() }.take(15), errorMessage = null) }
            is AddStudentAction.OnCepChange -> _uiState.update { it.copy(cep = action.value.filter { c -> c.isDigit() }.take(8), errorMessage = null) }
            is AddStudentAction.OnStreetChange -> _uiState.update { it.copy(street = action.value.take(100), errorMessage = null) }
            is AddStudentAction.OnNumberChange -> _uiState.update { it.copy(number = action.value.take(10), errorMessage = null) }
            is AddStudentAction.OnComplementChange -> _uiState.update { it.copy(complement = action.value) }
            is AddStudentAction.OnLearningTopicsChange -> _uiState.update { it.copy(learningTopics = action.value, errorMessage = null) }
            AddStudentAction.OnToggleAdditionalInfo -> _uiState.update { it.copy(showAdditionalInfo = !it.showAdditionalInfo) }
            AddStudentAction.OnSaveClick -> save()
            AddStudentAction.OnSaveHandled -> _uiState.update { it.copy(saveSuccess = false) }
        }
    }

    private fun save() {
        val state = _uiState.value
        val gender = state.gender.toGender()
        val topics = state.learningTopics.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        val ageValue = state.age.toIntOrNull()

        val validationError = when {
            state.fullName.isBlank() -> "Informe o nome do aluno."
            ageValue == null || ageValue !in 1..50 -> "A idade deve estar entre 1 e 50."
            gender == null -> "Selecione o gênero (Masculino ou Feminino)."
            state.phone.length < 7 -> "Informe um telefone válido (mín. 7 dígitos)."
            state.cep.length < 5 -> "Informe um CEP válido (mín. 5 dígitos)."
            state.street.isBlank() -> "Informe a rua."
            state.number.isBlank() -> "Informe o número."
            topics.isEmpty() -> "Informe ao menos um tema de aprendizagem."
            topics.any { it.length > 50 } -> "Cada tema deve ter no máximo 50 caracteres."
            else -> null
        }
        if (validationError != null) {
            _uiState.update { it.copy(errorMessage = validationError) }
            return
        }

        val form = StudentForm(
            name = state.fullName.trim(),
            age = checkNotNull(ageValue), // garantido não-nulo pela validação acima
            gender = gender,
            zipcode = state.cep.trim(),
            road = state.street.trim(),
            housenumber = state.number.trim(),
            phonenumber = state.phone.trim(),
            learningTopics = topics,
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = studentRepository.create(form)) {
                is ApiResult.Success -> _uiState.update { it.copy(isLoading = false, saveSuccess = true) }
                is ApiResult.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
            }
        }
    }
}

private fun String.toGender(): Gender? = when (this) {
    "Masculino" -> Gender.MALE
    "Feminino" -> Gender.FEMALE
    else -> null
}

data class AddStudentUiState(
    val fullName: String = "",
    val age: String = "",
    val gender: String = "",
    val phone: String = "",
    val cep: String = "",
    val street: String = "",
    val number: String = "",
    val complement: String = "",
    val learningTopics: String = "",
    val showAdditionalInfo: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val saveSuccess: Boolean = false,
)

sealed interface AddStudentAction {
    data class OnFullNameChange(val value: String) : AddStudentAction
    data class OnAgeChange(val value: String) : AddStudentAction
    data class OnGenderChange(val value: String) : AddStudentAction
    data class OnPhoneChange(val value: String) : AddStudentAction
    data class OnCepChange(val value: String) : AddStudentAction
    data class OnStreetChange(val value: String) : AddStudentAction
    data class OnNumberChange(val value: String) : AddStudentAction
    data class OnComplementChange(val value: String) : AddStudentAction
    data class OnLearningTopicsChange(val value: String) : AddStudentAction
    data object OnToggleAdditionalInfo : AddStudentAction
    data object OnSaveClick : AddStudentAction
    data object OnSaveHandled : AddStudentAction
}
