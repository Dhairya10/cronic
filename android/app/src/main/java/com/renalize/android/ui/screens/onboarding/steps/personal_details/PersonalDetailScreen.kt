package com.renalize.android.ui.screens.onboarding.steps.personal_details

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.renalize.android.ui.components.LoadingScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalDetailScreen(
    onProceed: () -> Unit,
    modifier: Modifier = Modifier
) {

    val ctx = LocalContext.current
    val viewmodel = hiltViewModel<ViewModel>()
    val registerState by viewmodel.registerState.collectAsState()
    val uiState by viewmodel.uiState.collectAsState()

    if (registerState is RegisterState.Loading) {
        LoadingScreen()
        return
    }

    if (registerState is RegisterState.Success) {
        LaunchedEffect(Unit) {
            onProceed()
        }
    }

    if (registerState is RegisterState.Error) {
        LaunchedEffect(Unit){
            Toast.makeText(
                ctx,
                (registerState as RegisterState.Error).message,
                Toast.LENGTH_SHORT
            ).show()
        }
    }


    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .padding(top = 20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Confirm your personal details", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = uiState.name,
                onValueChange = { viewmodel.updateName(it) },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth(),
            )

            GenderSelector(
                selected = uiState.gender,
                onGenderChange = { viewmodel.updateGender(it) },
                modifier = Modifier
                    .fillMaxWidth()
            )

            OutlinedCard(
                onClick = {
                    viewmodel.toggleDatePicker()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    if (uiState.dob == "") "Date of Birth" else uiState.dob,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Current Address", fontSize = 18.sp, fontWeight = FontWeight.Bold)

            OutlinedTextField(
                value = uiState.street,
                onValueChange = { viewmodel.updateStreet(it) },
                label = { Text("street") },
                modifier = Modifier.fillMaxWidth(),
            )

            Row {
                OutlinedTextField(
                    value = uiState.city,
                    onValueChange = { viewmodel.updateCity(it) },
                    label = { Text("City") },
                    modifier = Modifier.weight(1f),
                )
                Spacer(modifier = Modifier.width(10.dp))
                OutlinedTextField(
                    value = uiState.state,
                    onValueChange = { viewmodel.updateState(it) },
                    label = { Text("State") },
                    modifier = Modifier.weight(1f),
                )
            }

            OutlinedTextField(
                value = uiState.pinCode,
                onValueChange = { viewmodel.updatePinCode(it) },
                label = { Text("Pincode") },
                modifier = Modifier.width(120.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("What’s your Annual Income", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            IncomeOptions.entries.forEach {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            viewmodel.updateIncomeOption(it)
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (uiState.selectedIncomeOption == it),
                        onClick = { viewmodel.updateIncomeOption(it) }
                    )
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text(it.str, fontSize = 16.sp)
                }
            }
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Account details", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = uiState.accountNum,
                onValueChange = { viewmodel.updateAccountNum(it) },
                label = { Text("Account Number") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            OutlinedTextField(
                value = uiState.accountName,
                onValueChange = { viewmodel.updateAccountName(it) },
                label = { Text("Account Name") },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = uiState.bankName,
                onValueChange = { viewmodel.updateBankName(it) },
                label = { Text("Bank name") },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = uiState.bankBranch,
                onValueChange = { viewmodel.updateBankBranch(it) },
                label = { Text("Bank Branch") },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = uiState.ifscCode,
                onValueChange = { viewmodel.updateIfscCode(it) },
                label = { Text("IFSC code") },
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Button(
            onClick = { viewmodel.addPatient() },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.small,
            enabled = uiState.isFormValid
        ) {
            Text("Continue")
        }
    }

    if (uiState.showDatePicker) {
        val datePickerState = rememberDatePickerState()
        val confirmEnabled = remember {
            derivedStateOf { datePickerState.selectedDateMillis != null && datePickerState.selectedDateMillis!! <= System.currentTimeMillis() }
        }
        DatePickerDialog(
            onDismissRequest = {
                viewmodel.toggleDatePicker()
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewmodel.selectDate(datePickerState.selectedDateMillis!!)
                    },
                    enabled = confirmEnabled.value
                ) {
                    Text("OK")
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
@Preview(showBackground = true)
fun PersonalDetailScreenPreview() {
    PersonalDetailScreen({})
}

@Composable
fun GenderSelector(
    selected: Gender,
    onGenderChange: (Gender) -> Unit,
    modifier: Modifier = Modifier
) {

    val selectedColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
    val unselectedColor = MaterialTheme.colorScheme.surface

    OutlinedCard(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Gender.entries.forEach {
                Row(
                    modifier = Modifier
                        .clickable {
                            onGenderChange(it)
                        }
                        .background(
                            if (selected == it) selectedColor else unselectedColor
                        )
                        .weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ){
                    if (it == selected) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Selected",
                            modifier = Modifier
                                .size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        it.name,
                        modifier = Modifier
                            .padding(vertical = 16.dp),
                        textAlign = TextAlign.Center,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

enum class Gender {
    Male, Female, Other
}

enum class IncomeOptions(val str: String, val value: String){
    LessThan2L("Less than 2 Lakh per annum", "less_than_2"),
    Between2LTo5L("Between 2 to 5 lakh per annum", "2_to_5"),
    Between5LTo10L("Between 5 to 10 lakh per annum", "5_to_10"),
    MoreThan10L("More than 10 lakh per annum", "10_plus")
}