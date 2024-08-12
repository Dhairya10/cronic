package com.renalize.android.ui.screens.patient.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.renalize.android.data.model.response.PatientDataResponse
import com.renalize.android.data.repository.PatientRepository
import com.renalize.android.util.NetworkResult
import com.renalize.android.util.PreferenceManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val patientRepository: PatientRepository,
    private val preferenceManager: PreferenceManager
): ViewModel(){

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState
        get() = _uiState.asStateFlow()


    fun getProfileData() {
        viewModelScope.launch {
            patientRepository.getPatientProfile().collect{
                when(it){
                    is NetworkResult.Loading -> {
                        _uiState.value = ProfileUiState.Loading
                    }
                    is NetworkResult.Success -> {
                        _uiState.value = ProfileUiState.Success(it.data!!)
                    }
                    is NetworkResult.Error -> {
                        _uiState.value = ProfileUiState.Error(it.message!!)
                    }
                }
            }
        }
    }
}

sealed interface ProfileUiState {
    data object Loading: ProfileUiState
    data class Success(val profileData: PatientDataResponse): ProfileUiState
    data class Error(val message: String): ProfileUiState
}
