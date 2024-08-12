package com.renalize.android.ui.screens.onboarding.steps.personal_details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.renalize.android.data.model.common.AadharData
import com.renalize.android.data.model.common.Address
import com.renalize.android.data.model.common.BankAccountData
import com.renalize.android.data.model.common.KycData
import com.renalize.android.data.model.common.PanData
import com.renalize.android.data.model.request.AddPatientRequest
import com.renalize.android.data.repository.PatientRepository
import com.renalize.android.util.DateFormatter
import com.renalize.android.util.PreferenceManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ViewModel @Inject constructor(
    private val preferenceManager: PreferenceManager,
    private val repository: PatientRepository
) : ViewModel() {

    private val _registerState = MutableStateFlow<RegisterState>(RegisterState.Idle)
    val registerState
        get() = _registerState.asStateFlow()

    private val _uiState = MutableStateFlow(
        UiState(
            name = preferenceManager.getString(PreferenceManager.Keys.PAN_NAME),
            gender = Gender.valueOf("male".capitalize()),
            dob = preferenceManager.getString(PreferenceManager.Keys.DOB),
            street = preferenceManager.getString(PreferenceManager.Keys.ADDRESS_STREET),
            city = preferenceManager.getString(PreferenceManager.Keys.ADDRESS_CITY),
            pinCode = preferenceManager.getString(PreferenceManager.Keys.ADDRESS_PINCODE),
            state = preferenceManager.getString(PreferenceManager.Keys.ADDRESS_STATE),
            accountNum = preferenceManager.getString(PreferenceManager.Keys.AADHAR_NUMBER),
            accountName = preferenceManager.getString(PreferenceManager.Keys.ACCOUNT_HOLDER_NAME),
            bankName = preferenceManager.getString(PreferenceManager.Keys.BANK_NAME),
            bankBranch = preferenceManager.getString(PreferenceManager.Keys.BRANCH_NAME),
            ifscCode = preferenceManager.getString(PreferenceManager.Keys.IFSC_CODE),
        )
    )
    val uiState
        get() = _uiState


    fun updateName(name: String) {
        _uiState.value = _uiState.value.copy(name = name)
        validateForm()
    }

    fun toggleDatePicker() {
        _uiState.update {
            it.copy(showDatePicker = !it.showDatePicker)
        }
    }

    fun updateStreet(street: String) {
        _uiState.update {
            it.copy(street = street)
        }
        validateForm()
    }

    fun updatePinCode(pinCode: String) {
        _uiState.update {
            it.copy(pinCode = pinCode)
        }
        validateForm()
    }

    fun updateCity(city: String) {
        _uiState.update {
            it.copy(city = city)
        }
        validateForm()
    }

    fun updateAccountNum(accountNum: String) {
        _uiState.update {
            it.copy(accountNum = accountNum)
        }
        validateForm()
    }

    fun updateAccountName(accountName: String) {
        _uiState.update {
            it.copy(accountName = accountName)
        }
        validateForm()
    }

    fun updateBankName(bankName: String) {
        _uiState.update {
            it.copy(bankName = bankName)
        }
        validateForm()
    }

    fun updateBankBranch(bankBranch: String) {
        _uiState.update {
            it.copy(bankBranch = bankBranch)
        }
        validateForm()
    }

    fun updateIfscCode(ifscCode: String) {
        _uiState.update {
            it.copy(ifscCode = ifscCode)
        }
        validateForm()
    }

    fun selectDate(selectedDateMillis: Long) {
        _uiState.update {
            it.copy(
                selectedDateMillis = selectedDateMillis,
                showDatePicker = false,
                dob = DateFormatter.instance.format(selectedDateMillis),
            )
        }
        validateForm()
    }

    fun updateIncomeOption(option: IncomeOptions) {
        _uiState.update {
            it.copy(selectedIncomeOption = option)
        }
        validateForm()
    }

    fun updateGender(gender: Gender){
        _uiState.update {
            it.copy(gender = gender)
        }
        validateForm()
    }

    fun updateState(state: String){
        _uiState.update {
            it.copy(state = state)
        }
        validateForm()
    }

    fun addPatient() {
        viewModelScope.launch {
            repository.addPatientData(
                AddPatientRequest(
                    contactNum = "+91${preferenceManager.getString(PreferenceManager.Keys.MOBILE_NUMBER)}",
                    kycData = KycData(
                        aadharData = AadharData(
                            aadharNumber = preferenceManager.getString(PreferenceManager.Keys.AADHAR_NUMBER),
                            address = Address(
                                city = preferenceManager.getString(PreferenceManager.Keys.ADDRESS_CITY),
                                state = preferenceManager.getString(PreferenceManager.Keys.ADDRESS_STATE),
                                street = preferenceManager.getString(PreferenceManager.Keys.ADDRESS_STREET),
                                pincode = preferenceManager.getString(PreferenceManager.Keys.ADDRESS_PINCODE)
                            ),
                            name = preferenceManager.getString(PreferenceManager.Keys.AADHAR_NAME),
                            dob = preferenceManager.getString(PreferenceManager.Keys.DOB),
                            gender = preferenceManager.getString(PreferenceManager.Keys.GENDER),
                        ),
                        panData = PanData(
                            panNumber = preferenceManager.getString(PreferenceManager.Keys.PAN_NUMBER),
                            name = preferenceManager.getString(PreferenceManager.Keys.PAN_NAME)
                        ),
                        incomeLevel = _uiState.value.selectedIncomeOption!!.value,
                        bankAccountData = BankAccountData(
                            accountNumber = _uiState.value.accountNum,
                            accountHolderName = _uiState.value.accountName,
                            bankName = _uiState.value.bankName,
                            branchName = _uiState.value.bankBranch,
                            ifscCode = _uiState.value.ifscCode
                        )
                    ),
                    uhid = preferenceManager.getString(PreferenceManager.Keys.UHID)
                )
            ).collect {
                when (it) {
                    is com.renalize.android.util.NetworkResult.Success -> {
                        _registerState.value = RegisterState.Success
                    }
                    is com.renalize.android.util.NetworkResult.Error -> {
                        _registerState.value = RegisterState.Error(it.message?: "Something went wrong")
                    }
                    is com.renalize.android.util.NetworkResult.Loading -> {
                        _registerState.value = RegisterState.Loading
                    }
                }
            }
        }
    }

    private fun validateForm(){
        _uiState.value = _uiState.value.copy(isFormValid =  uiState.value.name.isNotEmpty() &&
                uiState.value.dob.isNotEmpty() &&
                uiState.value.state.isNotEmpty() &&
                uiState.value.street.isNotEmpty() &&
                uiState.value.pinCode.isNotEmpty() &&
                uiState.value.city.isNotEmpty() &&
                uiState.value.accountNum.isNotEmpty() &&
                uiState.value.accountName.isNotEmpty() &&
                uiState.value.bankName.isNotEmpty() &&
                uiState.value.bankBranch.isNotEmpty() &&
                uiState.value.ifscCode.isNotEmpty() &&
                uiState.value.selectedIncomeOption != null
        )

    }
}

data class UiState(
    val name: String,
    val gender : Gender = Gender.Male,
    val dob: String,
    val selectedDateMillis: Long = 0,
    val street: String,
    val pinCode: String,
    val city: String,
    val accountNum: String,
    val accountName: String,
    val bankName: String,
    val bankBranch: String,
    val ifscCode: String,
    val showDatePicker: Boolean = false,
    val selectedIncomeOption: IncomeOptions? = null,
    val isFormValid: Boolean = false,
    val state: String,
)

sealed class RegisterState {
    data object Idle : RegisterState()
    data object Loading : RegisterState()
    data object Success : RegisterState()
    data class Error(val message: String) : RegisterState()
}