package com.labirintodosaber.ui.screen.addstudent

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.labirintodosaber.R
import com.labirintodosaber.ui.theme.GradientBottom
import com.labirintodosaber.ui.theme.InputBorder
import com.labirintodosaber.ui.theme.TealLight
import com.labirintodosaber.ui.theme.TealPrimary
import com.labirintodosaber.ui.theme.TextPrimary
import com.labirintodosaber.ui.theme.TextSecondary

@Composable
fun AddStudentScreen(
    onCancelClick: () -> Unit,
    onSaveSuccess: () -> Unit,
    onMenuClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: AddStudentViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            onSaveSuccess()
            viewModel.onAction(AddStudentAction.OnSaveHandled)
        }
    }

    AddStudentContent(
        uiState = uiState,
        onAction = viewModel::onAction,
        onCancelClick = onCancelClick,
        onMenuClick = onMenuClick,
        onProfileClick = onProfileClick,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddStudentContent(
    uiState: AddStudentUiState,
    onAction: (AddStudentAction) -> Unit,
    onCancelClick: () -> Unit,
    onMenuClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.students_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Outlined.Menu, contentDescription = stringResource(R.string.dashboard_menu_desc), tint = TextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = onProfileClick) {
                        Icon(Icons.Outlined.AccountCircle, contentDescription = stringResource(R.string.dashboard_profile_desc), tint = TextPrimary, modifier = Modifier.size(28.dp))
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White),
            )
        },
        containerColor = GradientBottom,
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item { PhotoSection() }

            item {
                FormCard {
                    FormField(
                        label = stringResource(R.string.add_student_full_name_label),
                        required = true,
                    ) {
                        FormTextField(
                            value = uiState.fullName,
                            onValueChange = { onAction(AddStudentAction.OnFullNameChange(it)) },
                            placeholder = stringResource(R.string.add_student_full_name_placeholder),
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            FormField(label = stringResource(R.string.add_student_age_label), required = true) {
                                FormTextField(
                                    value = uiState.age,
                                    onValueChange = { onAction(AddStudentAction.OnAgeChange(it)) },
                                    placeholder = stringResource(R.string.add_student_age_placeholder),
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            FormField(label = stringResource(R.string.add_student_gender_label), required = true) {
                                GenderDropdown(
                                    selected = uiState.gender,
                                    onSelect = { onAction(AddStudentAction.OnGenderChange(it)) },
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    FormField(label = stringResource(R.string.add_student_phone_label), required = true) {
                        FormTextField(
                            value = uiState.phone,
                            onValueChange = { onAction(AddStudentAction.OnPhoneChange(it)) },
                            placeholder = stringResource(R.string.add_student_phone_placeholder),
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    FormField(label = stringResource(R.string.add_student_topics_label), required = true) {
                        FormTextField(
                            value = uiState.learningTopics,
                            onValueChange = { onAction(AddStudentAction.OnLearningTopicsChange(it)) },
                            placeholder = stringResource(R.string.add_student_topics_placeholder),
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = InputBorder)
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = stringResource(R.string.add_student_address_section),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Column(modifier = Modifier.weight(0.4f)) {
                            FormField(label = stringResource(R.string.add_student_cep_label), required = true) {
                                FormTextField(
                                    value = uiState.cep,
                                    onValueChange = { onAction(AddStudentAction.OnCepChange(it)) },
                                    placeholder = stringResource(R.string.add_student_cep_placeholder),
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(0.6f)) {
                            FormField(label = stringResource(R.string.add_student_street_label), required = true) {
                                FormTextField(
                                    value = uiState.street,
                                    onValueChange = { onAction(AddStudentAction.OnStreetChange(it)) },
                                    placeholder = stringResource(R.string.add_student_street_placeholder),
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            FormField(label = stringResource(R.string.add_student_number_label), required = true) {
                                FormTextField(
                                    value = uiState.number,
                                    onValueChange = { onAction(AddStudentAction.OnNumberChange(it)) },
                                    placeholder = stringResource(R.string.add_student_number_placeholder),
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            FormField(label = stringResource(R.string.add_student_complement_label), required = false) {
                                FormTextField(
                                    value = uiState.complement,
                                    onValueChange = { onAction(AddStudentAction.OnComplementChange(it)) },
                                    placeholder = stringResource(R.string.add_student_complement_placeholder),
                                )
                            }
                        }
                    }
                }
            }

            item {
                AdditionalInfoSection(
                    expanded = uiState.showAdditionalInfo,
                    onToggle = { onAction(AddStudentAction.OnToggleAdditionalInfo) },
                )
            }

            uiState.errorMessage?.let { message ->
                item {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedButton(
                        onClick = onCancelClick,
                        modifier = Modifier.weight(0.4f).height(48.dp),
                        shape = RoundedCornerShape(50.dp),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, TextPrimary),
                    ) {
                        Text(
                            text = stringResource(R.string.add_student_cancel),
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                        )
                    }
                    Button(
                        onClick = { onAction(AddStudentAction.OnSaveClick) },
                        enabled = !uiState.isLoading,
                        modifier = Modifier.weight(0.6f).height(48.dp),
                        shape = RoundedCornerShape(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Icon(Icons.Outlined.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stringResource(R.string.add_student_save),
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 12.sp,
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
}

// ── Photo Section ─────────────────────────────────────────────────────────────

@Composable
private fun PhotoSection(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = GradientBottom,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(TealLight),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = null,
                    tint = TealPrimary,
                    modifier = Modifier.size(34.dp),
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Surface(
                    shape = RoundedCornerShape(50.dp),
                    color = Color.Transparent,
                    modifier = Modifier.border(1.5.dp, TextPrimary, RoundedCornerShape(50.dp)),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Outlined.Add, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.add_student_photo_button),
                            style = MaterialTheme.typography.labelMedium,
                            color = TextPrimary,
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.add_student_photo_hint),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                )
            }
        }
    }
}

// ── Additional Info Section ───────────────────────────────────────────────────

@Composable
private fun AdditionalInfoSection(
    expanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 1.dp,
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(TextSecondary),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("+", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.add_student_additional_info_title),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                    )
                    Text(
                        text = stringResource(R.string.add_student_additional_info_hint),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Outlined.KeyboardArrowDown else Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                    contentDescription = null,
                    tint = TextSecondary,
                )
            }
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

@Composable
private fun FormCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = GradientBottom,
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            content()
        }
    }
}

@Composable
private fun FormField(
    label: String,
    required: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(modifier = modifier) {
        Row {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
            )
            if (required) {
                Text(" *", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = Color(0xFFFB2C36))
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        content()
    }
}

@Composable
private fun FormTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(placeholder, style = MaterialTheme.typography.bodySmall, color = TextSecondary.copy(alpha = 0.5f))
        },
        singleLine = true,
        shape = RoundedCornerShape(50.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = TealPrimary,
            unfocusedBorderColor = InputBorder,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
        ),
        modifier = modifier.fillMaxWidth().height(52.dp),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GenderDropdown(
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val options = listOf("Masculino", "Feminino", "Outro")
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            placeholder = {
                Text(stringResource(R.string.add_student_gender_placeholder), style = MaterialTheme.typography.bodySmall, color = TextSecondary.copy(alpha = 0.5f))
            },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            singleLine = true,
            shape = RoundedCornerShape(50.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = TealPrimary,
                unfocusedBorderColor = InputBorder,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    },
                )
            }
        }
    }
}
