package com.renalize.android.data.model.response


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.renalize.android.data.model.common.Address
import com.renalize.android.data.model.common.BankAccountData
import com.renalize.android.data.model.common.PanData

@Keep
data class KYCVerificationResponse(

    @SerializedName("aadhar_back_data")
    val aadharBackData: AadharBackData? = null,

    @SerializedName("aadhar_front_data")
    val aadharFrontData: AadharFrontData? = null,

    @SerializedName("bank_account_data")
    val bankAccountData: BankAccountData? = null,

    @SerializedName("pan_data")
    val panData: PanData? = null
)

@Keep
data class AadharBackData(
    val address: Address
)

@Keep
data class AadharFrontData(

    @SerializedName("aadhar_number")
    val aadharNumber: String,

    @SerializedName("date_of_birth")
    val dateOfBirth: String,

    val gender: String,

    val name: String
)