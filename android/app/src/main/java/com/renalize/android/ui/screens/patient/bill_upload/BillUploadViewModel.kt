package com.renalize.android.ui.screens.patient.bill_upload

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.renalize.android.data.repository.PatientRepository
import com.renalize.android.util.NetworkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BillUploadViewModel @Inject constructor(
    private val patientRepository: PatientRepository
): ViewModel(){

    private val _uiState = MutableStateFlow<BillUploadUiState>(BillUploadUiState.Idle)
    val uiState
        get() = _uiState.asStateFlow()

    fun uploadBill(uri: List<Uri>) {
        viewModelScope.launch {
            patientRepository.verifyClaim(uri).collect {
                when(it){
                    is NetworkResult.Success -> {
                        _uiState.value = BillUploadUiState.Success
                    }
                    is NetworkResult.Error -> {
                        _uiState.value = BillUploadUiState.Error
                    }
                    is NetworkResult.Loading -> {
                        _uiState.value = BillUploadUiState.Uploading
                    }
                }
            }
        }
    }

    fun resetState(){
        _uiState.value = BillUploadUiState.Idle
    }

}

sealed interface BillUploadUiState{

    data object Idle: BillUploadUiState
    data object Uploading: BillUploadUiState
    data object Success: BillUploadUiState
    data object Error: BillUploadUiState

}