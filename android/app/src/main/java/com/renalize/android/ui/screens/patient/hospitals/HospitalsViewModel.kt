package com.renalize.android.ui.screens.patient.hospitals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.renalize.android.data.api.HospitalsItem
import com.renalize.android.data.repository.PatientRepository
import com.renalize.android.util.NetworkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HospitalsViewModel @Inject constructor(
    private val repository: PatientRepository,
): ViewModel() {

    private val _uiState = MutableStateFlow<HospitalUiState>(HospitalUiState.Loading)
    val uiState
        get() = _uiState

    init{
        getHospitalData()
    }

    private fun getHospitalData() {
        viewModelScope.launch {
            repository.getHospitals().collect{
                when(it){
                    is NetworkResult.Success -> {
                        _uiState.value = HospitalUiState.Success(it.data!!)
                    }
                    is NetworkResult.Error -> {
                        _uiState.value = HospitalUiState.Error(it.message!!)
                    }
                    is NetworkResult.Loading -> {
                        _uiState.value = HospitalUiState.Loading
                    }
                }
            }
        }
    }
}

sealed interface HospitalUiState {
    data object Loading: HospitalUiState
    data class Success(val data: List<HospitalsItem>): HospitalUiState
    data class Error(val message: String): HospitalUiState
}