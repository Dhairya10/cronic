package com.renalize.android.data.model.request


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.renalize.android.data.model.common.KycData

@Keep
data class AddPatientRequest(

    @SerializedName("contact_num")
    val contactNum: String,

    @SerializedName("kyc_data")
    val kycData: KycData,

    @SerializedName("primary_doctor_name")
    val primaryDoctorName : String = "",

    @SerializedName("primary_healthcare_provider")
    val primaryHealthcareProvider : String = "",

    val uhid: String,

    @SerializedName("health_condition")
    val healthCondition : String = "chronic_kidney_disease"
)