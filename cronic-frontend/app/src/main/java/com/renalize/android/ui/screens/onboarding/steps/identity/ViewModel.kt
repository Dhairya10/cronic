package com.renalize.android.ui.screens.onboarding.steps.identity

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.renalize.android.data.repository.KYCDocType
import com.renalize.android.data.repository.PatientRepository
import com.renalize.android.util.NetworkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ViewModel @Inject constructor(
    private val repository: PatientRepository
): ViewModel(){

    private val _uploadState: MutableStateFlow<UploadState> = MutableStateFlow(UploadState.Idle)
    val uploadState: StateFlow<UploadState>
        get() = _uploadState.asStateFlow()

    private val _uiState = MutableStateFlow(IdentityUiState())
    val uiState
        get() = _uiState.asStateFlow()

    fun onAadharCardFrontSelected(uri: Uri){
        _uiState.update{ _uiState.value.copy(aadharCardFrontUri = uri) }
        viewModelScope.launch {
            repository.verifyKyc(KYCDocType.AadharFront,uri).collect{
                when(it){
                    is NetworkResult.Loading -> _uploadState.value = UploadState.Uploading
                    is NetworkResult.Success -> _uploadState.value = UploadState.Uploaded
                    is NetworkResult.Error -> {
                        _uploadState.value =
                            UploadState.Error(it.message ?: "Failed to upload KYC docs")
                        _uiState.update{ _uiState.value.copy(aadharCardFrontUri = null) }
                    }
                }
            }
        }
    }

    fun onAadharCardBackSelected(uri: Uri){
        _uiState.update{ _uiState.value.copy(aadharCardBackUri = uri) }
        viewModelScope.launch {
            repository.verifyKyc(KYCDocType.AadharBack,uri).collect{
                when(it){
                    is NetworkResult.Loading -> _uploadState.value = UploadState.Uploading
                    is NetworkResult.Success -> _uploadState.value = UploadState.Uploaded
                    is NetworkResult.Error -> {
                        _uploadState.value =
                            UploadState.Error(it.message ?: "Failed to upload KYC docs")
                        _uiState.update{ _uiState.value.copy(aadharCardBackUri = null) }
                    }
                }
            }
        }
    }

    fun onPanCardSelected(uri: Uri){
        _uiState.update{ _uiState.value.copy(panCardUri = uri) }
        viewModelScope.launch {
            repository.verifyKyc(KYCDocType.Pan,uri).collect{
                when(it){
                    is NetworkResult.Loading -> _uploadState.value = UploadState.Uploading
                    is NetworkResult.Success -> _uploadState.value = UploadState.Uploaded
                    is NetworkResult.Error -> {
                        _uploadState.value =
                            UploadState.Error(it.message ?: "Failed to upload KYC docs")
                        _uiState.update{ _uiState.value.copy(panCardUri = null) }
                    }
                }
            }
        }
    }

    fun onBankPassbookSelected(uri: Uri){
        _uiState.update{ _uiState.value.copy(bankPassbookUri = uri) }
        viewModelScope.launch {
            repository.verifyKyc(KYCDocType.BankPassbook,uri).collect{
                when(it){
                    is NetworkResult.Loading -> _uploadState.value = UploadState.Uploading
                    is NetworkResult.Success -> _uploadState.value = UploadState.Uploaded
                    is NetworkResult.Error -> {
                        _uploadState.value =
                            UploadState.Error(it.message ?: "Failed to upload KYC docs")
                        _uiState.update{ _uiState.value.copy(bankPassbookUri = null) }
                    }
                }
            }
        }
    }

    fun onUHIDChanged(uhid: String) {
        if(uhid.length > 14) return
        _uiState.update{ _uiState.value.copy(uhid = uhid) }
    }

}

data class IdentityUiState(
    val aadharCardFrontUri: Uri? = null,
    val aadharCardBackUri: Uri? = null,
    val panCardUri: Uri?= null,
    val bankPassbookUri: Uri? = null,
    val uhid: String = ""
)

sealed class UploadState{
    data object Idle: UploadState()
    data object Uploading: UploadState()
    data object Uploaded: UploadState()
    data class Error(val message: String): UploadState()
}