package com.renalize.android.data.model.common


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class KycData(
    @SerializedName("aadhar_data")
    val aadharData: AadharData,

    @SerializedName("bank_account_data")
    val bankAccountData: BankAccountData,

    @SerializedName("income_level")
    val incomeLevel: String,

    @SerializedName("pan_data")
    val panData: PanData
)