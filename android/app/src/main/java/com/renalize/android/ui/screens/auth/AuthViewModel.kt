package com.renalize.android.ui.screens.auth

import android.app.Activity
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.renalize.android.data.api.ApiService
import com.renalize.android.util.PreferenceManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val apiService: ApiService,
    private val preferenceManager: PreferenceManager,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private lateinit var verificationID: String
    private val _uiState: MutableStateFlow<PhoneAuthState> = MutableStateFlow(PhoneAuthState())

    val uiState
        get() = _uiState.asStateFlow()

    init {
        _uiState.update { it.copy(phoneNumber = savedStateHandle.get<String>("phone") ?: "") }
    }


    fun signInWithOtp() {
        _uiState.update { it.copy(isLoading = true) }
        val credential = PhoneAuthProvider.getCredential(verificationID, uiState.value.otp)
        signInWithCredential(credential)
    }

    private fun signInWithCredential(credential: PhoneAuthCredential) {
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    viewModelScope.launch {
                        try {
                            val token = firebaseAuth.currentUser!!.getIdToken(true).await()
                            val patientData = apiService.getPatientData().patientData?.also {
                                preferenceManager.putString(
                                    PreferenceManager.Keys.AADHAR_NAME,
                                    it.kycData.aadharData.name
                                )
                            }
                            preferenceManager.apply {
                                putString(PreferenceManager.Keys.USER_TOKEN, token.token!!)
                                putString(
                                    PreferenceManager.Keys.MOBILE_NUMBER,
                                    uiState.value.phoneNumber
                                )
                            }
                            _uiState.update {
                                it.copy(
                                    success = true,
                                    isLoading = false,
                                    newUser = patientData.isNull()
                                )
                            }
                        } catch (e: Exception) {
                            _uiState.update { it.copy(error = e.message, isLoading = false) }
                        }
                    }
                } else {
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        _uiState.update {
                            it.copy(
                                error = "Invalid OTP",
                                isLoading = false
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(error = "Something went wrong", isLoading = false)
                        }
                    }
                }
            }
    }

    private val phoneAuthCallback =
        object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                signInWithCredential(credential)
                _uiState.update { it.copy(isLoading = false, success = true) }
            }

            override fun onVerificationFailed(e: FirebaseException) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }

            override fun onCodeSent(
                verificationID: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                super.onCodeSent(verificationID, token)
                this@AuthViewModel.verificationID = verificationID
                _uiState.update { it.copy(isOtpSent = true, isLoading = false) }
            }
        }

    fun updateEnteredOTP(otp: String) {
        if (otp.length <= 6) _uiState.update { it.copy(otp = otp) }
    }

    fun sendOtp(activity: Activity) {
        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber("+91${_uiState.value.phoneNumber}")
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(phoneAuthCallback)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
        _uiState.update { it.copy(isLoading = true) }
    }
}

data class PhoneAuthState(
    val isOtpSent: Boolean = false,
    val isLoading: Boolean = false,
    val isOtpValid: Boolean = false,
    val phoneNumber: String = "",
    val error: String? = null,
    val success: Boolean = false,
    val otp: String = "",
    val newUser: Boolean = true
)

fun Any?.isNull() = this == null