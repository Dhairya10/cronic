package com.renalize.android.ui.screens.patient.passbook

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.renalize.android.data.model.response.Bill
import com.renalize.android.data.repository.PatientRepository
import com.renalize.android.util.NetworkResult
import com.renalize.android.util.PreferenceManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PassbookViewModel @Inject constructor(
    private val patientRepository: PatientRepository,
    private val preferenceManager: PreferenceManager
): ViewModel(){

    var _uiState = MutableStateFlow<UIState>(UIState.Loading)
    val uiState
        get() = _uiState.asStateFlow()

    val name = preferenceManager.getString(PreferenceManager.Keys.AADHAR_NAME).split(" ")[0]

    val sampleList = listOf(
        Bill(
            amount = 2000,
            billId = "123",
            reasoning = "Fever",
            date = "12/12/2021",
            status = "pending",
            type = "pharmacy",
            patientId = "123",
        ),
        Bill(
            amount = 2000,
            billId = "123",
            reasoning = "claim rejected due to insufficient documents",
            date = "12/12/2021",
            status = "rejected",
            type = "pharmacy",
            patientId = "123",
        ),
        Bill(
            amount = 2000,
            billId = "123",
            reasoning = "Fever",
            date = "12/12/2021",
            status = "verified",
            type = "pharmacy",
            patientId = "123",
        )
    )
    val emptyList = emptyList<Bill>()

    var bills = mutableStateOf(emptyList)
        private set

    fun getBills(){
        viewModelScope.launch{
            patientRepository.getPatientBills().collect {
                when(it){
                    is NetworkResult.Success -> {
                        bills.value = it.data?.bills ?: emptyList()
                        _uiState.value = UIState.Success
                    }

                    is NetworkResult.Error -> {
                        _uiState.value = UIState.Error
                    }

                    is NetworkResult.Loading -> {
                        _uiState.value = UIState.Loading
                    }

                }
            }
        }
    }

    sealed class UIState{
        data object Loading: UIState()
        data object Error: UIState()
        data object Success: UIState()
    }
}

