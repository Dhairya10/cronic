package com.renalize.android.data.model.response

import com.google.gson.annotations.SerializedName
import com.renalize.android.data.model.common.KycData

data class PatientDataResponse(
    @SerializedName("patient_data")
    val patientData: PatientData? = null,
)

data class PatientData(
    @SerializedName("patient_id")
    val patientId : String,

    val uhid: String,

    @SerializedName("contact_num")
    val contactNum: String,

    @SerializedName("health_condition")
    val healthCondition: String,

    @SerializedName("primary_doctor_name")
    val primaryDoctorName: String,

    @SerializedName("primary_healthcare_provider")
    val primaryHealthcareProvider: String,

    @SerializedName("kyc_data")
    val kycData: KycData
)