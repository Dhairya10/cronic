package com.renalize.android.data.repository

import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.renalize.android.data.api.ApiService
import com.renalize.android.data.api.HospitalsItem
import com.renalize.android.data.model.common.BankAccountData
import com.renalize.android.data.model.common.PanData
import com.renalize.android.data.model.request.AddPatientRequest
import com.renalize.android.data.model.request.KycVerificationRequest
import com.renalize.android.data.model.request.VerifyClaimBatchRequest
import com.renalize.android.data.model.request.VerifyClaimRequest
import com.renalize.android.data.model.response.AadharBackData
import com.renalize.android.data.model.response.AadharFrontData
import com.renalize.android.data.model.response.BillHistoryResponse
import com.renalize.android.data.model.response.PatientDataResponse
import com.renalize.android.util.NetworkResult
import com.renalize.android.util.PreferenceManager
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await


class PatientRepository(
    firebaseStorage: FirebaseStorage,
    auth: FirebaseAuth,
    private val apiService: ApiService,
    private val preferenceManager: PreferenceManager
) {

    //firebase storage
    private val storageRef = firebaseStorage.reference
    private val bucketName = storageRef.bucket
    val billsRef = storageRef.child("bills").child(auth.currentUser!!.uid)
    private val patientDocRef = storageRef.child("patient_docs").child(auth.currentUser!!.uid)
    private val kycRef = patientDocRef.child("kyc_docs")

    suspend fun verifyKyc(
        docType: KYCDocType,
        fileUri: Uri,
    ) : Flow<NetworkResult<Unit>>  = flow{
        emit(NetworkResult.Loading())
        try{
            val ref = kycRef.child(docType.attrStr).putFile(fileUri).await()
            val url = "gs://$bucketName${ref.storage.path}"
            val res = apiService.verifyKyc(KycVerificationRequest("image",url))
            when(docType){
                is KYCDocType.AadharFront -> saveAadharFrontData(res.aadharFrontData!!)
                is KYCDocType.AadharBack -> saveAadharBackData(res.aadharBackData!!)
                is KYCDocType.Pan -> savePanData(res.panData!!)
                is KYCDocType.BankPassbook -> saveBankAccountData(res.bankAccountData!!)
            }
            emit(NetworkResult.Success(Unit))
        }catch (e: Exception){
            emit(NetworkResult.Error("Failed to verify KYC"))
        }
    }

    suspend fun addPatientData(addPatientRequest: AddPatientRequest): Flow<NetworkResult<Unit>> = flow {
        emit(NetworkResult.Loading())
        try{
            apiService.addPatient(addPatientRequest)
            emit(NetworkResult.Success(Unit))
        }catch (e: Exception) {
            emit(NetworkResult.Error("Failed to add patient data"))
        }
    }

    fun getPatientBills(): Flow<NetworkResult<BillHistoryResponse>> = flow{
        emit(NetworkResult.Loading())
        try{
            emit(NetworkResult.Success(apiService.getBillHistory()))
        }catch(e:Exception){
            emit(NetworkResult.Error("Failed to get bill history"))
        }
    }

    suspend fun verifyClaim(uri: List<Uri>): Flow<NetworkResult<Unit>>  = flow {
        emit(NetworkResult.Loading())
        try{
            if(uri.size == 1){
                val ref = billsRef.child("${uri.first().lastPathSegment}_${System.currentTimeMillis()}").putFile(uri.first()).await()
                val url = "gs://$bucketName${ref.storage.path}"
                if(apiService.verifyClaim(VerifyClaimRequest(documentType = "image", fileUri = url)).status) emit(NetworkResult.Success(Unit))
                else emit(NetworkResult.Error("Failed to verify claim"))
            }else{
                val urls = uri.map { uploadUri(it) }.awaitAll().map{ "gs://$bucketName${it.storage.path}" }
                if(
                    apiService.verifyClaimBatch(
                        VerifyClaimBatchRequest(
                            urls.map{ VerifyClaimRequest(documentType = "image", fileUri = it) }
                        )
                    ).status
                ){
                    emit(NetworkResult.Success(Unit))
                }else{
                    emit(NetworkResult.Error("Failed to verify claim"))
                }
            }
        }catch (e: Exception){
            Log.d("Verifu", "verifyClaim: ${e.message}")
            emit(NetworkResult.Error("Failed to verify KYC"))
        }
    }

    suspend fun getPatientProfile(): Flow<NetworkResult<PatientDataResponse>> = flow{
        emit(NetworkResult.Loading())
        try{
            emit(NetworkResult.Success(apiService.getPatientData()))
        }catch(e:Exception){
            emit(NetworkResult.Error("Failed to get patient profile"))
        }
    }

    suspend fun getHospitals(): Flow<NetworkResult<List<HospitalsItem>>> = flow{
        emit(NetworkResult.Loading())
        try{
            emit(NetworkResult.Success(apiService.getHospitalData()))
        }catch(e:Exception){
            emit(NetworkResult.Error("Failed to get hospitals"))
        }
    }

    //helper functions
    private suspend fun uploadUri(uri: Uri) = coroutineScope {
        async {
            billsRef.child("${uri.lastPathSegment}_${System.currentTimeMillis()}").putFile(uri).await()
        }
    }

    private fun saveAadharFrontData(aadharFrontData: AadharFrontData){
        preferenceManager.apply {
            putString(PreferenceManager.Keys.AADHAR_NUMBER, aadharFrontData.aadharNumber)
            putString(PreferenceManager.Keys.AADHAR_NAME, aadharFrontData.name)
            putString(PreferenceManager.Keys.DOB, aadharFrontData.dateOfBirth)
            putString(PreferenceManager.Keys.GENDER, aadharFrontData.gender)
        }
    }

    private fun saveAadharBackData(aadharBackData: AadharBackData){
        preferenceManager.apply {
            putString(PreferenceManager.Keys.ADDRESS_STREET, aadharBackData.address.street)
            putString(PreferenceManager.Keys.ADDRESS_CITY, aadharBackData.address.city)
            putString(PreferenceManager.Keys.ADDRESS_STATE, aadharBackData.address.state)
            putString(PreferenceManager.Keys.ADDRESS_PINCODE, aadharBackData.address.pincode)
        }
    }

    private fun savePanData(panData: PanData){
        preferenceManager.apply {
            putString(PreferenceManager.Keys.PAN_NUMBER, panData.panNumber)
            putString(PreferenceManager.Keys.PAN_NAME, panData.name)
        }
    }

    private fun saveBankAccountData(bankAccountData: BankAccountData){
        preferenceManager.apply {
            putString(PreferenceManager.Keys.ACCOUNT_NUMBER, bankAccountData.accountNumber)
            putString(PreferenceManager.Keys.IFSC_CODE, bankAccountData.ifscCode)
            putString(PreferenceManager.Keys.BANK_NAME, bankAccountData.bankName)
            putString(PreferenceManager.Keys.BRANCH_NAME, bankAccountData.branchName)
            putString(PreferenceManager.Keys.ACCOUNT_HOLDER_NAME, bankAccountData.accountHolderName)
        }
    }
}

sealed class KYCDocType(val attrStr: String) {
    data object AadharFront : KYCDocType("aadhar_front_data")
    data object AadharBack : KYCDocType("aadhar_back_data")
    data object Pan : KYCDocType("pan_data")
    data object BankPassbook : KYCDocType("bank_account_data")
}



