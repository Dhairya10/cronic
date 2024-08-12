package com.renalize.android.data.api

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.renalize.android.data.model.request.AddPatientRequest
import com.renalize.android.data.model.request.KycVerificationRequest
import com.renalize.android.data.model.request.VerifyClaimBatchRequest
import com.renalize.android.data.model.request.VerifyClaimRequest
import com.renalize.android.data.model.response.BillHistoryResponse
import com.renalize.android.data.model.response.ClaimVerifyResponse
import com.renalize.android.data.model.response.KYCVerificationResponse
import com.renalize.android.data.model.response.PatientDataResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST


interface ApiService {

    @POST("kyc/verify")
    suspend fun verifyKyc(
        @Body request: KycVerificationRequest
    ): KYCVerificationResponse

    @POST("patient/add")
    suspend fun addPatient(
        @Body request: AddPatientRequest
    )

    @GET("bills")
    suspend fun getBillHistory(): BillHistoryResponse

    @POST("claim/verify")
    suspend fun verifyClaim(
        @Body request: VerifyClaimRequest
    ): ClaimVerifyResponse

    @POST("claim/verify-batch")
    suspend fun verifyClaimBatch(
        @Body request: VerifyClaimBatchRequest
    ): ClaimVerifyResponse

    @GET("patient")
    suspend fun getPatientData(): PatientDataResponse

    @GET("hospital")
    suspend fun getHospitalData(): List<HospitalsItem>

}

@Keep
data class HospitalsItem(
    val address: String,

    @SerializedName("contact_number")
    val contactNumber: String,

    @SerializedName("google_maps_link")
    val googleMapsLink: String,

    val name: String,

    @SerializedName("website_link")
    val websiteLink: String
)