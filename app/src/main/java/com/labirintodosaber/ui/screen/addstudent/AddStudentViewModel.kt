package com.labirintodosaber.ui.screen.addstudent

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class AddStudentViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(AddStudentUiState())
    val uiState: StateFlow<AddStudentUiState> = _uiState.asStateFlow()

    fun onAction(action: AddStudentAction) {
        when (action) {
            is AddStudentAction.OnFullNameChange -> _uiState.update { it.copy(fullName = action.value) }
            is AddStudentAction.OnAgeChange -> _uiState.update { it.copy(age = action.value) }
            is AddStudentAction.OnGenderChange -> _uiState.update { it.copy(gender = action.value) }
            is AddStudentAction.OnPhoneChange -> _uiState.update { it.copy(phone = action.value) }
            is AddStudentAction.OnCepChange -> _uiState.update { it.copy(cep = action.value) }
            is AddStudentAction.OnStreetChange -> _uiState.update { it.copy(street = action.value) }
            is AddStudentAction.OnNumberChange -> _uiState.update { it.copy(number = action.value) }
            is AddStudentAction.OnComplementChange -> _uiState.update { it.copy(complement = action.value) }
            AddStudentAction.OnToggleAdditionalInfo -> _uiState.update { it.copy(showAdditionalInfo = !it.showAdditionalInfo) }
            AddStudentAction.OnSaveClick -> { /* TODO */ }
        }
    }
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
    val showAdditionalInfo: Boolean = false,
    val isLoading: Boolean = false,
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
    data object OnToggleAdditionalInfo : AddStudentAction
    data object OnSaveClick : AddStudentAction
}
